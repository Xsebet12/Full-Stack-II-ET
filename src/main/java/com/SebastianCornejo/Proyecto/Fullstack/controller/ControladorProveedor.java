package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ContactoProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProveedor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import org.springframework.web.bind.annotation.RequestPart;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Operaciones para gestionar proveedores y sus contactos")
@SecurityRequirement(name = "bearerAuth")
public class ControladorProveedor {

    private final ServicioProveedor servicioProveedor;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ControladorProveedor.class);

    public ControladorProveedor(ServicioProveedor servicioProveedor) {
        this.servicioProveedor = servicioProveedor;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Devuelve la lista de proveedores. Se puede filtrar con `habilitado=true|false` (alias `activo`). Si `all=true`, devuelve todos (sólo ADMIN). Por defecto, sólo activos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente")
    })
    public List<Proveedor> listar(@RequestParam(value = "all", required = false) Boolean all,
                                  @RequestParam(value = "habilitado", required = false) Boolean habilitado,
                                  @RequestParam(value = "activo", required = false) Boolean activoParam,
                                  Authentication authentication) {
        if (authentication == null) {
            try {
                authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            } catch (Exception ignored) {}
        }
        if (authentication == null) {
            log.debug("listar proveedores - authentication=null");
        } else {
            log.debug("listar proveedores - auth name={} authorities={}", authentication.getName(), authentication.getAuthorities());
        }
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        // Si viene filtro explícito (habilitado/activo), usarlo; si se pide inactivos requiere ADMIN
        Boolean activo = (habilitado != null) ? habilitado : activoParam;
        if (activo != null) {
            if (Boolean.FALSE.equals(activo) && !isAdmin) {
                throw new AccessDeniedException("Acceso denegado");
            }
            return servicioProveedor.findAllPorEstado(activo);
        }

        // Compatibilidad: ?all=true => todos (sólo ADMIN); por defecto sólo activos
        if (Boolean.TRUE.equals(all)) {
            if (!isAdmin) throw new AccessDeniedException("Acceso denegado");
            return servicioProveedor.findAll();
        }
        return servicioProveedor.findAllActivos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor", description = "Devuelve un proveedor por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public Proveedor obtener(@PathVariable Long id) {
        return servicioProveedor.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear proveedor", description = "Crea un nuevo proveedor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Proveedor creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proveedor.class), examples = @ExampleObject(value = "{\"id\":1,\"companyName\":\"ACME\",\"serviceType\":\"Servicios\"}"))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Proveedor> crear(@RequestBody(
        description = "Proveedor a crear",
        required = true,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proveedor.class), examples = @ExampleObject(value = "{\"companyName\":\"ACME\",\"serviceType\":\"Servicios\",\"url\":\"https://acme.example\",\"phone\":\"+56912345678\"}"))
    ) @org.springframework.web.bind.annotation.RequestBody Proveedor proveedor) {
    Proveedor creado = servicioProveedor.create(proveedor);
    return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza los datos de un proveedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public Proveedor actualizar(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody Proveedor proveedor) {
        return servicioProveedor.update(id, proveedor);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // --- Manejo de imágenes (LOGO) - Eliminación física ---
        // 1) Antes de eliminar el proveedor en BD, capturamos la URL del logo actual.
        // 2) Eliminamos el registro en BD.
        // 3) Si la URL del logo apunta a nuestro storage local (/images/**),
        //    extraemos el nombre de archivo y borramos el archivo físico del disco.
        //    Esto evita dejar archivos huérfanos en "uploads/images".
        //    Nota: la carpeta base se resuelve con getImagesDir() (ver método al final).
        // -------------------------------------------------------------------------
        // Capturar logo actual antes de eliminar en BD
        Proveedor before = servicioProveedor.findById(id);
        String oldLogo = before != null ? before.getLogoUrl() : null;
        servicioProveedor.delete(id);
        // Intentar borrar archivo físico del logo si es nuestro
        if (oldLogo != null && !oldLogo.isBlank()) {
            try {
                String filename = extraerNombreArchivo(oldLogo);
                if (filename != null) {
                    Path imagesDir = getImagesDir();
                    Files.deleteIfExists(imagesDir.resolve(filename));
                }
            } catch (Exception ignored) {}
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Activa/Desactiva el proveedor")
    public ResponseEntity<Proveedor> cambiarEstado(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody(required = false) Boolean activoBody,
            @RequestParam(value = "activo", required = false) Boolean activoParam) {
        // Aceptar tanto cuerpo JSON boolean como query param ?activo=true para mayor compatibilidad
        Boolean activo = (activoBody != null) ? activoBody : activoParam;
        Proveedor actualizado = servicioProveedor.setEstado(id, activo);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/contactos")
    @Operation(summary = "Agregar contacto", description = "Agrega un contacto a un proveedor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contacto agregado y proveedor devuelto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proveedor.class), examples = @ExampleObject(value = "{\"id\":1,\"companyName\":\"ACME\",\"contacts\":[{\"id\":10,\"name\":\"Juan\",\"phone\":\"123\"}]}"))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public Proveedor agregarContacto(@PathVariable Long id, @RequestBody(
        description = "Contacto a agregar",
        required = true,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContactoProveedor.class), examples = @ExampleObject(value = "{\"name\":\"Juan\",\"phone\":\"123\",\"email\":\"a@b.com\",\"role\":\"Gerente\"}"))
    ) @org.springframework.web.bind.annotation.RequestBody ContactoProveedor contacto) {
    return servicioProveedor.addContact(id, contacto);
    }

    @PutMapping("/{id}/contactos/{contactId}")
    @Operation(summary = "Actualizar contacto", description = "Actualiza un contacto de un proveedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Proveedor o contacto no encontrado")
    })
    public Proveedor actualizarContacto(@PathVariable Long id, @PathVariable Long contactId, @org.springframework.web.bind.annotation.RequestBody ContactoProveedor contacto) {
        return servicioProveedor.updateContact(id, contactId, contacto);
    }

    @DeleteMapping("/{id}/contactos/{contactId}")
    @Operation(summary = "Eliminar contacto", description = "Elimina un contacto de un proveedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto eliminado y proveedor devuelto"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Proveedor o contacto no encontrado")
    })
    public Proveedor eliminarContacto(@PathVariable Long id, @PathVariable Long contactId) {
        return servicioProveedor.removeContact(id, contactId);
    }

    @PatchMapping("/{id}/contactos/{contactId}/principal")
    @Operation(summary = "Marcar contacto principal", description = "Marca un contacto como principal y desmarca los demás")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto principal establecido"),
            @ApiResponse(responseCode = "404", description = "Proveedor o contacto no encontrado")
    })
    public Proveedor establecerContactoPrincipal(@PathVariable Long id, @PathVariable Long contactId) {
        return servicioProveedor.setPrincipal(id, contactId);
    }

    @PostMapping(path = "/{id}/logo", consumes = {"multipart/form-data"})
    @Operation(summary = "Subir logo", description = "Sube y actualiza la URL del logo del proveedor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logo actualizado y proveedor devuelto"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Proveedor> subirLogo(@PathVariable Long id,
                           @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new PeticionInvalidaException("Archivo de imagen vacío");
        }
        // --- Manejo de imágenes (LOGO) - Subida y reemplazo ---
        // Flujo resumido:
        // 1) Tomamos el logo anterior (si existe) para poder borrarlo al final.
        // 2) Generamos un nombre de archivo único (UUID + extensión original).
        // 3) Guardamos el archivo en el directorio de imágenes local (getImagesDir()).
        // 4) Construimos la URL pública como APP_BASE_URL + "/images/" + filename.
        // 5) Actualizamos el proveedor con la nueva URL.
        // 6) Si existía un logo anterior alojado en nuestro /images, lo eliminamos físicamente
        //    para no acumular archivos huérfanos.
        // -----------------------------------------------------------------------
        // Obtener logo anterior para eliminar luego del update
        Proveedor before = servicioProveedor.findById(id);
        String oldLogo = before != null ? before.getLogoUrl() : null;
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + ext;
        Path imagesDir = getImagesDir();
        try {
            // Crear la carpeta en caso de que no exista y guardar el archivo subido
            Files.createDirectories(imagesDir);
            Path target = imagesDir.resolve(filename);
            java.io.File dest = target.toFile();
            file.transferTo(Objects.requireNonNull(dest));
        } catch (Exception e) {
            throw new IOException("Error saving uploaded file: " + original, e);
        }
        // APP_BASE_URL define el host público para construir la URL accesible desde el frontend
        // Ej: APP_BASE_URL=http://localhost:8080  =>  http://localhost:8080/images/<archivo>
        String baseUrl = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:8080");
        String publicUrl = baseUrl + "/images/" + filename;
        Proveedor updated = servicioProveedor.updateLogo(id, publicUrl);
        // Eliminar archivo previo si existe y pertenece a nuestro /images (storage local)
        if (oldLogo != null && !oldLogo.isBlank()) {
            try {
                String oldName = extraerNombreArchivo(oldLogo);
                if (oldName != null && !oldName.equals(filename)) {
                    Files.deleteIfExists(imagesDir.resolve(oldName));
                }
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/contactos")
    @Operation(summary = "Listar contactos", description = "Devuelve los contactos de un proveedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de contactos devuelta"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public List<ContactoProveedor> listarContactos(@PathVariable Long id) {
        return servicioProveedor.listarContactos(id);
    }

    // Resuelve el directorio físico donde se guardan las imágenes/ logos en disco.
    // Prioriza la variable de entorno UPLOADS_DIR (conviene fuera del proyecto para despliegues),
    // y si no está definida usa la carpeta local "./uploads/images" relativa al working dir.
    private Path getImagesDir() {
        String uploads = System.getenv("UPLOADS_DIR");
        Path imagesDir;
        if (uploads != null && !uploads.isBlank()) {
            imagesDir = Paths.get(uploads).resolve("images");
        } else {
            imagesDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");
        }
        return imagesDir;
    }
    // Dada una URL pública tipo http(s)://host/images/<archivo>, extrae sólo el nombre del archivo
    // para poder operar en el filesystem local (borrar, etc.). Si la URL no contiene "/images/",
    // devuelve null para evitar intentar borrar archivos que no son nuestros.
    private String extraerNombreArchivo(String url) {
        if (url == null) return null;
        int idx = url.indexOf("/images/");
        if (idx >= 0) {
            return url.substring(idx + "/images/".length());
        }
        return null;
    }
}

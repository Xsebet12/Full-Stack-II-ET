package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioImagenProducto;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ImagenProducto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Objects;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
// note: avoid importing io.swagger RequestBody to prevent conflict with Spring's @RequestBody

@RestController
@RequestMapping("/api/productos")
@SecurityRequirement(name = "bearerAuth")
public class ControladorProducto {

    /**
     * Controlador REST para operaciones sobre productos.
     *
    * Todas las rutas están bajo /api/productos. Los métodos manejan CRUD básico
    * y la gestión de imágenes asociadas a los productos.
    *
    * Resumen del manejo de imágenes:
    * - Dónde se guardan: en el filesystem, directorio "uploads/images" relativo al working dir,
    *   o bien en "${UPLOADS_DIR}/images" si se define la variable de entorno UPLOADS_DIR.
    * - Cómo se nombran: al subir, se genera un nombre único con UUID + extensión del archivo original.
    * - URL pública: se construye como APP_BASE_URL + "/images/" + nombreArchivo. WebConfig expone /images/**
    *   apuntando a la carpeta física anterior, permitiendo servirlas como recursos estáticos.
    * - Eliminación física: al borrar una imagen (o un producto completo), se extrae el nombre de
    *   archivo desde la URL (si pertenece a /images/**) y se borra con Files.deleteIfExists(...).
     */

    private final ServicioProducto servicioProducto;
    private final RepositorioImagenProducto repoImagenes;
    private static final Logger log = LoggerFactory.getLogger(ControladorProducto.class);

    public ControladorProducto(ServicioProducto servicioProducto, RepositorioImagenProducto repoImagenes) {
        this.servicioProducto = servicioProducto;
        this.repoImagenes = repoImagenes;
    }

    // Nuevo: listar productos con stock crítico (stock < umbral, por defecto 5)
    @GetMapping("/stock-critico")
    @Operation(summary = "Listar stock crítico", description = "Devuelve productos con stock por debajo del umbral (por defecto 5)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class)))
    })
    public List<Producto> listarStockCritico(@RequestParam(value = "umbral", required = false) Integer umbral,
                                             @RequestParam(value = "habilitado", required = false) Boolean habilitado,
                                             @RequestParam(value = "all", required = false) Boolean all,
                                             Authentication authentication) {
        int limit = (umbral == null || umbral < 0) ? 5 : umbral;
        if (authentication == null) {
            try {
                authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            } catch (Exception ignored) {}
        }
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        List<Producto> base;
        if (habilitado != null) {
            if (Boolean.FALSE.equals(habilitado) && !isAdmin) {
                throw new AccessDeniedException("Acceso denegado");
            }
            base = servicioProducto.findAllByHabilitado(habilitado);
        } else if (Boolean.TRUE.equals(all)) {
            if (!isAdmin) throw new AccessDeniedException("Acceso denegado");
            base = servicioProducto.findAll();
        } else {
            base = servicioProducto.findAllEnabled();
        }
        return base.stream()
                .filter(p -> p.getStock() != null && p.getStock() < limit)
                .toList();
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(value = "all", required = false) Boolean all,
                                 @RequestParam(value = "habilitado", required = false) Boolean habilitado,
                                 @RequestParam(value = "q", required = false) String q,
                                 @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                 Authentication authentication) {
        if (authentication == null) {
            try {
                authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            } catch (Exception ignored) {}
        }
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        // 1) Obtener lista base según permisos y filtros de habilitado/all previos
        List<Producto> base;
        if (habilitado != null) {
            if (Boolean.FALSE.equals(habilitado) && !isAdmin) {
                throw new AccessDeniedException("Acceso denegado");
            }
            base = servicioProducto.findAllByHabilitado(habilitado);
        } else if (Boolean.TRUE.equals(all)) {
            if (!isAdmin) throw new AccessDeniedException("Acceso denegado");
            base = servicioProducto.findAll();
        } else {
            base = servicioProducto.findAllEnabled();
        }

        // 2) Filtros adicionales: búsqueda por nombre (q) y filtro por categoría (categoriaId)
        if (q != null && !q.isBlank()) {
            String term = q.toLowerCase();
            base = base.stream()
                    .filter(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(term))
                    .toList();
        }
        if (categoriaId != null) {
            base = base.stream()
                    .filter(p -> p.getCategoria() != null && categoriaId.equals(p.getCategoria().getId()))
                    .toList();
        }
        return base;
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        // Obtiene un producto por id (lanza RecursoNoEncontradoException si no existe)
        return servicioProducto.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un producto sin imágenes")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class), examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"Mouse\",\"precio\":19.99}"))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Producto a crear", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class), examples = @ExampleObject(value = "{\"nombre\":\"Mouse\",\"precio\":19.99,\"categoria\":{\"id\":1}}")))
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        // Crea un producto simple (sin imágenes). Valida categoría en el servicio.
        Producto creado = servicioProducto.create(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Nuevo: crear producto con imágenes en una sola petición multipart
    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Crear producto con imágenes", description = "Crea un producto y sube imágenes en una sola petición multipart")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado con imágenes",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
        public ResponseEntity<Producto> crearConImagenes(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Multipart request with 'product' part (JSON) and 'files' parts (binary)", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestPart("product") Producto producto,
            @RequestPart(value = "files", required = false) List<MultipartFile> archivos) throws IOException {
    List<String> urlsImagenes = new ArrayList<>();
    log.info("crearConImagenes llamado - nombre='{}' archivosCount={}", producto != null ? producto.getNombre() : "<null>", archivos == null ? 0 : archivos.size());
        // Procesar cada archivo enviado en la petición multipart
        if (archivos != null) {
            for (MultipartFile archivo : archivos) {
                // Ignorar partes vacías
                if (archivo.isEmpty()) {
                    log.warn("Omitiendo parte de archivo vacía");
                    continue;
                }
                // Determinar extensión a partir del nombre original (si existe)
                String original = archivo.getOriginalFilename();
                String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
                String nombreArchivo = UUID.randomUUID() + ext;
                Path dirImagenes = getImagesDir();
                try {
                    // Crear directorio si no existe y guardar el archivo en disco
                    log.info("Guardando archivo subido '{}' en directorio '{}'", original, dirImagenes.toAbsolutePath());
                    Files.createDirectories(dirImagenes);
                    Path target = dirImagenes.resolve(nombreArchivo);
                    java.io.File dest = target.toFile();
                    archivo.transferTo(Objects.requireNonNull(dest));
                    // Construir URL pública usando APP_BASE_URL (o localhost por defecto):
                    // Ej: http://localhost:8080/images/<nombreArchivo>
                    String urlBase = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:8080");
                    String urlPublica = urlBase + "/images/" + nombreArchivo;
                    urlsImagenes.add(urlPublica);
                } catch (Exception e) {
                    // Registrar el error y propagar como IOException para que el cliente reciba 500
                    log.error("Error guardando archivo subido '{}' -> {}", original, e.toString(), e);
                    throw new IOException("Error guardando archivo subido: " + original, e);
                }
            }
        }
        Producto creado = (urlsImagenes.isEmpty() ? servicioProducto.create(producto) : servicioProducto.createWithImages(producto, urlsImagenes));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Producto a actualizar", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class)))
        public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        // Actualiza datos básicos del producto (nombre, descripción, stock, precio, categoría)
        return servicioProducto.update(id, producto);
    }

    // Actualiza imagen del producto subiendo archivo. Guarda en /images y registra URL en 'img' y en colección de imágenes
    @PostMapping(path = "/{id}/image", consumes = {"multipart/form-data"})
    @Operation(summary = "Subir imagen de producto", description = "Sube una imagen para un producto y devuelve el producto actualizado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagen subida y producto devuelto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Producto> subirImagen(@PathVariable Long id,
                           @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new PeticionInvalidaException("Archivo de imagen vacío");
        }
            // Nota: este endpoint sube un único archivo y lo asocia como imagen principal
            // del producto (campo 'img') además de añadirlo a la colección de imágenes.
            // El archivo se guarda en el directorio resuelto por getImagesDir() y se expone
            // públicamente bajo /images/** por configuración de WebConfig.
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String nombreArchivo = UUID.randomUUID() + ext;
        Path dirImagenes = getImagesDir();
        try {
            // Guardar el archivo en el directorio de imágenes
            log.info("subirImagen - guardando '{}' en {}", original, dirImagenes.toAbsolutePath());
            Files.createDirectories(dirImagenes);
            Path target = dirImagenes.resolve(nombreArchivo);
            java.io.File dest = target.toFile();
            file.transferTo(Objects.requireNonNull(dest));
        } catch (Exception e) {
            log.error("Error guardando archivo '{}' -> {}", original, e.toString(), e);
            throw new IOException("Error guardando archivo: " + original, e);
        }
        // Construir URL pública en base a APP_BASE_URL (por defecto http://localhost:8080)
        String urlBase = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:8080");
        String urlPublica = urlBase + "/images/" + nombreArchivo;
        Producto actualizado = servicioProducto.addImage(id, urlPublica);
        return ResponseEntity.ok(actualizado);
    }

    // Nuevo: subir múltiples imágenes para un producto existente
    @PostMapping(path = "/{id}/images", consumes = {"multipart/form-data"})
    @Operation(summary = "Subir múltiples imágenes", description = "Sube varias imágenes y las asocia a un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imágenes subidas y producto devuelto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Producto> subirImagenes(@PathVariable Long id,
                        @RequestPart("files") List<MultipartFile> archivos) throws java.io.IOException {
        if (archivos == null || archivos.isEmpty()) {
            throw new PeticionInvalidaException("Debe enviar al menos un archivo");
        }
    Producto ultimo = null;
        // Procesar lote de archivos y asociarlos al producto
    for (MultipartFile archivo : archivos) {
            if (archivo.isEmpty()) continue; // saltar partes vacías
            String original = archivo.getOriginalFilename();
            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String nombreArchivo = UUID.randomUUID() + ext;
            Path dirImagenes = getImagesDir();
            try {
                log.info("subirImagenes - guardando '{}' en {}", original, dirImagenes.toAbsolutePath());
                Files.createDirectories(dirImagenes);
                Path target = dirImagenes.resolve(nombreArchivo);
                archivo.transferTo(Objects.requireNonNull(target.toFile()));
                // Construir URL pública en base a APP_BASE_URL
                String urlBase = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:8080");
                String urlPublica = urlBase + "/images/" + nombreArchivo;
                // Añadir la imagen al producto mediante el servicio (persistencia)
                ultimo = servicioProducto.addImage(id, urlPublica);
            } catch (Exception e) {
                log.error("Error guardando archivo '{}' -> {}", original, e.toString(), e);
                throw new IOException("Error guardando archivo: " + original, e);
            }
        }
        return ResponseEntity.ok(ultimo);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Eliminar imagen", description = "Elimina una imagen específica del producto y devuelve el producto actualizado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagen eliminada y producto devuelto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto o imagen no encontrada")
    })
    public ResponseEntity<Producto> eliminarImagen(@PathVariable Long id, @PathVariable Long imageId) {
        // Eliminar imagen:
        // 1) Obtenemos la URL de la imagen a partir del repositorio (evita perezosos/LAZY).
        // 2) Eliminamos la relación en la base de datos vía servicio.
        // 3) Si la URL pertenece a /images/**, extraemos el nombre del archivo y borramos el físico en disco.
        String urlToDelete = repoImagenes.findById(java.util.Objects.requireNonNull(imageId)).map(ImagenProducto::getUrl).orElse(null);
        Producto actualizado = servicioProducto.removeImage(id, imageId);
        if (urlToDelete != null && !urlToDelete.isBlank()) {
            try {
                String filename = extraerNombreArchivo(urlToDelete);
                if (filename != null) {
                    Path imagesDir = getImagesDir();
                    Files.deleteIfExists(imagesDir.resolve(filename));
                    log.info("Archivo de imagen eliminado: {}", filename);
                }
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo físico para imagen {}: {}", urlToDelete, e.toString());
            }
        }
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/imagen-principal")
    @Operation(summary = "Establecer imagen principal", description = "Marca como principal una imagen existente del producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagen principal actualizada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto o imagen no encontrada")
    })
    public ResponseEntity<Producto> establecerImagenPrincipal(@PathVariable Long id, @RequestParam("imageId") Long imageId) {
        Producto actualizado = servicioProducto.setMainImage(id, imageId);
        return ResponseEntity.ok(actualizado);
    }

    // Helper para determinar el directorio donde se guardan las imágenes en disco.
    // Usa la variable de entorno UPLOADS_DIR si está presente, o por defecto './uploads/images' dentro del working dir.
    private Path getImagesDir() {
        String uploads = System.getenv("UPLOADS_DIR");
        Path dirImagenes;
        if (uploads != null && !uploads.isBlank()) {
            dirImagenes = Paths.get(uploads).resolve("images");
        } else {
            dirImagenes = Paths.get(System.getProperty("user.dir"), "uploads", "images");
        }
        return dirImagenes;
    }

    // Extrae el nombre de archivo desde una URL pública tipo http(s)://host/images/filename.ext
    // Devuelve null si la URL no pertenece a /images/** para evitar borrar archivos que no gestionamos.
    private String extraerNombreArchivo(String url) {
        if (url == null) return null;
        int idx = url.indexOf("/images/");
        if (idx >= 0) {
            return url.substring(idx + "/images/".length());
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // Recolectar URLs de imágenes antes de eliminar en BD usando repositorio (evita LAZY)
        // Luego de eliminar el producto en BD, intentaremos borrar en disco todos los archivos cuya
        // URL apunte a /images/**. Esto mantiene el storage "uploads/images" limpio de huérfanos.
        Producto before = servicioProducto.findById(id);
        List<String> urls = new ArrayList<>();
        if (before.getImagen() != null) urls.add(before.getImagen());
        for (ImagenProducto ip : repoImagenes.findAllByProductoId(id)) {
            if (ip.getUrl() != null) urls.add(ip.getUrl());
        }
        // Eliminar en BD
        servicioProducto.delete(id);
        // Intentar borrar archivos físicos asociados
        for (String url : urls) {
            try {
                String filename = extraerNombreArchivo(url);
                if (filename != null) {
                    Path imagesDir = getImagesDir();
                    Files.deleteIfExists(imagesDir.resolve(filename));
                    log.info("Archivo de imagen eliminado: {}", filename);
                }
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo físico para imagen {}: {}", url, e.toString());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/habilitado")
    @Operation(summary = "Cambiar habilitado", description = "Activa/Desactiva el producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nuevo estado habilitado", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class), examples = @ExampleObject(value = "true")))
        public ResponseEntity<Producto> cambiarHabilitado(@PathVariable Long id, @RequestBody Boolean habilitado) {
        Producto actualizado = servicioProducto.setHabilitado(id, habilitado);
        return ResponseEntity.ok(actualizado);
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudActualizacionUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudCambioContrasena;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ControladorUsuario {

    private final RepositorioUsuario repositorioUsuario;
    private final ServicioUsuario servicioUsuario;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioEmpleado repositorioEmpleado;

    public ControladorUsuario(RepositorioUsuario repositorioUsuario, ServicioUsuario servicioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Listado de usuarios con filtro opcional ?habilitado=true|false (solo ADMIN)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaUsuario.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public List<RespuestaUsuario> listar(@RequestParam(value = "habilitado", required = false) Boolean habilitado,
                                         @RequestParam(value = "tipo", required = false) String tipo) {
        List<Usuario> all = repositorioUsuario.findAll();
        if (habilitado != null) {
            all = all.stream().filter(u -> habilitado.equals(u.getHabilitado())).collect(java.util.stream.Collectors.toList());
        }
        return all.stream().map(this::mapUsuario).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario", description = "Datos del usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil obtenido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaUsuario.class)))
    })
    public ResponseEntity<RespuestaUsuario> perfil(@AuthenticationPrincipal UserDetails principal) {
        Usuario user = repositorioUsuario.findByCorreo(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(mapUsuario(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por id", description = "Devuelve un usuario (sólo ADMIN)")
    public RespuestaUsuario obtener(@PathVariable Long id) {
        return servicioUsuario.findById(id);
    }

    @GetMapping("/check")
    @Operation(summary = "Verificar unicidad", description = "Devuelve {taken:true|false} si email, rut o celular existen")
    public java.util.Map<String, Boolean> check(@RequestParam(value = "email", required = false) String email,
                                               @RequestParam(value = "rut", required = false) String rut,
                                               @RequestParam(value = "celular", required = false) String celular) {
        boolean taken = false;
        if (email != null && !email.isBlank()) {
            taken = repositorioUsuario.existsByCorreo(email.trim());
        } else if (rut != null && !rut.isBlank()) {
            taken = repositorioUsuario.existsByRut(rut.trim());
        } else if (celular != null && !celular.isBlank()) {
            taken = repositorioEmpleado != null && repositorioEmpleado.existsByCelular(celular.trim());
        }
        return java.util.Map.of("taken", taken);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza datos del usuario (sólo ADMIN)")
    public RespuestaUsuario actualizar(@PathVariable Long id, @RequestBody SolicitudActualizacionUsuario request) {
        return servicioUsuario.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deshabilitar usuario", description = "Deshabilita (soft delete) al usuario (sólo ADMIN)")
    public ResponseEntity<Void> deshabilitar(@PathVariable Long id) {
        servicioUsuario.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado habilitado", description = "Activa/Desactiva el usuario (sólo ADMIN)")
    public ResponseEntity<RespuestaUsuario> cambiarEstado(@PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody(required = false) Boolean habilitadoBody,
            @RequestParam(value = "habilitado", required = false) Boolean habilitadoParam) {
        Boolean habilitado = (habilitadoBody != null) ? habilitadoBody : habilitadoParam;
        RespuestaUsuario updated = servicioUsuario.setHabilitado(id, habilitado);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails principal,
                                               @Valid @RequestBody SolicitudCambioContrasena body) {
        servicioUsuario.cambiarContrasena(principal.getUsername(), body.getAntigua(), body.getNueva());
        return ResponseEntity.noContent().build();
    }

    private RespuestaUsuario mapUsuario(Usuario saved) {
        RespuestaUsuario.RespuestaUsuarioBuilder<?, ?> b = RespuestaUsuario.builder()
                .id(saved.getId())
                .nombres(saved.getNombres())
                .apellidos(saved.getApellidos())
                .rut(saved.getRut())
                .dv(saved.getDv())
                .correo(saved.getCorreo())
                .rol(saved.getRol())
                .direccion(saved.getDireccion())
                .comuna(saved.getComuna() != null ? saved.getComuna().getNomComuna() : null)
                .region(saved.getComuna() != null && saved.getComuna().getRegion() != null ? saved.getComuna().getRegion().getNomRegion() : null)
                .comunaId(saved.getComuna() != null ? saved.getComuna().getIdComuna() : null)
                .regionId(saved.getComuna() != null && saved.getComuna().getRegion() != null ? saved.getComuna().getRegion().getIdRegion() : null)
                .enabled(saved.getHabilitado())
                .createdAt(saved.getCreadoEn());
        try {
            if (saved instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente c) {
                b.tipoCliente(c.getTipoCliente())
                 .puntosFidelizacion(c.getPuntosFidelizacion())
                 .recibirPromos(c.getRecibirPromos())
                 .direccionEntrega(c.getDireccionEntrega())
                 .preferenciasComunicacion(c.getPreferenciasComunicacion())
                 .limiteCredito(c.getLimiteCredito())
                 .frecuenciaCompra(c.getFrecuenciaCompra());
            }
        } catch (RuntimeException ignored) {}
        return b.build();
    }
}

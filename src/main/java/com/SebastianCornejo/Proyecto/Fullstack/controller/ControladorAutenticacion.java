package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudAutenticacion;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaAutenticacion;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudRegistro;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.security.ProveedorTokenJwt;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.UsuarioNoEncontradoException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudRecuperacionContrasena;

@RestController
@RequestMapping("/api/autenticacion")
@Tag(name = "Autenticación", description = "Endpoints de autenticación y registro de usuarios")
public class ControladorAutenticacion {

    private final AuthenticationManager authenticationManager;
    private final ProveedorTokenJwt proveedorToken;
    private final ServicioUsuario servicioUsuario;

    public ControladorAutenticacion(AuthenticationManager authenticationManager, ProveedorTokenJwt proveedorToken, ServicioUsuario servicioUsuario) {
        this.authenticationManager = authenticationManager;
        this.proveedorToken = proveedorToken;
        this.servicioUsuario = servicioUsuario;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario",
        description = "Autentica por correo y contrasena y devuelve un JWT")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales de inicio de sesión", required = true,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolicitudAutenticacion.class), examples = @ExampleObject(value = "{\"correo\":\"a@b.com\",\"contrasena\":\"secret\"}")))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaAutenticacion.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<RespuestaAutenticacion> iniciarSesion(@Valid @RequestBody SolicitudAutenticacion request) {
        // Comprobar existencia del usuario para devolver 404 si no existe (más útil en desarrollo)
        if (!servicioUsuario.existsByCorreo(request.getCorreo())) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
            );
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = proveedorToken.generarToken(principal);
            long expiresIn = 3600_000;
            return ResponseEntity.ok(new RespuestaAutenticacion(token, "Bearer", expiresIn));
        } catch (BadCredentialsException ex) {
            throw ex; // será manejado por RestExceptionHandler
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario",
        description = "Registra un nuevo usuario (por defecto CLIENT) y devuelve datos del usuario")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de registro", required = true,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolicitudRegistro.class), examples = @ExampleObject(value = "{\"nombres\":\"Juan\",\"apellidos\":\"Pérez\",\"rut\":\"12345678\",\"dv\":\"9\",\"correo\":\"juan@example.com\",\"contrasena\":\"secret\",\"direccion\":\"Av. Siempre Viva 123\",\"comunaId\":13101}")))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario registrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaUsuario.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<RespuestaUsuario> registrarUsuario(@Valid @RequestBody SolicitudRegistro request,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "tipo", required = false) String tipo) {
        RespuestaUsuario creado = (tipo == null || tipo.isBlank())
                ? servicioUsuario.registerClient(request)
                : servicioUsuario.register(request, tipo);
        // Devolver 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/forgot")
    @Operation(summary = "Recuperar contraseña", description = "Genera una contraseña temporal para el usuario indicado")
    public ResponseEntity<com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaRecuperacion> recuperar(@Valid @RequestBody SolicitudRecuperacionContrasena req) {
        if (req == null || req.getCorreo() == null || req.getCorreo().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!servicioUsuario.existsByCorreo(req.getCorreo())) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }
        String temp = generarTemporal();
        servicioUsuario.resetContrasena(req.getCorreo(), temp);
        return ResponseEntity.ok(new com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaRecuperacion(temp));
    }

    private static String generarTemporal(){
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<10;i++){ sb.append(AB.charAt(rnd.nextInt(AB.length()))); }
        return sb.toString();
    }
}

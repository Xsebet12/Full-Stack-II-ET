package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudOperacionItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCarrito;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Gestión del carrito de compras del usuario")
@SecurityRequirement(name = "bearerAuth")
public class ControladorCarrito {

    private final ServicioCarrito servicioCarrito;

    public ControladorCarrito(ServicioCarrito servicioCarrito) {
        this.servicioCarrito = servicioCarrito;
    }

    @GetMapping
    @Operation(summary = "Obtener carrito", description = "Obtiene el carrito del usuario autenticado")
    public ResponseEntity<RespuestaCarrito> obtenerCarrito(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<RespuestaCarrito>build();
        return ResponseEntity.ok(servicioCarrito.getMyCart(principal));
    }

    @PostMapping("/add")
    @Operation(summary = "Agregar producto", description = "Agrega cantidad de un producto al carrito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrito actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaCarrito.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<RespuestaCarrito> agregar(@AuthenticationPrincipal UserDetails principal,
                        @RequestBody SolicitudOperacionItemCarrito request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<RespuestaCarrito>build();
        return ResponseEntity.ok(servicioCarrito.addItem(principal, request));
    }

    @PostMapping("/remove")
    @Operation(summary = "Quitar producto", description = "Quita cantidad de un producto del carrito; si la cantidad supera la existente, se elimina el item")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrito actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaCarrito.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<RespuestaCarrito> quitar(@AuthenticationPrincipal UserDetails principal,
                           @RequestBody SolicitudOperacionItemCarrito request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<RespuestaCarrito>build();
        return ResponseEntity.ok(servicioCarrito.removeItem(principal, request));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Vaciar carrito", description = "Elimina todos los productos del carrito del usuario")
    public ResponseEntity<Void> vaciar(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        servicioCarrito.clear(principal);
        return ResponseEntity.noContent().build();
    }
}

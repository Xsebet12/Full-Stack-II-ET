package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioComuna;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/api/comunas")
@Tag(name = "Comunas", description = "Gestión de comunas")
public class ControladorComuna {

    private final ServicioComuna servicioComuna;

    public ControladorComuna(ServicioComuna servicioComuna) {
        this.servicioComuna = servicioComuna;
    }

    @GetMapping
    @Operation(summary = "Listar comunas", description = "Devuelve todas las comunas")
    public List<Comuna> listar() {
        return servicioComuna.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener comuna", description = "Devuelve la comuna por su id")
    public Comuna obtener(@PathVariable Integer id) {
        return servicioComuna.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear comuna", description = "Crea una nueva comuna")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comuna creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comuna.class), examples = @ExampleObject(value = "{\"id\":1,\"nomComuna\":\"Santiago\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Comuna> crear(@RequestBody Comuna comuna) {
        Comuna creado = servicioComuna.create(comuna);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comuna", description = "Actualiza la comuna indicada por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comuna actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comuna.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Comuna no encontrada")
    })
    public Comuna actualizar(@PathVariable Integer id, @RequestBody Comuna comuna) {
        return servicioComuna.update(id, comuna);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comuna", description = "Elimina la comuna indicada por id")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        servicioComuna.delete(id);
        return ResponseEntity.noContent().build();
    }
}

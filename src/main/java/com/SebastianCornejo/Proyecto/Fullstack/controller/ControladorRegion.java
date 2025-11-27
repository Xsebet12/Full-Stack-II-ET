package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioRegion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/api/regiones")
@Tag(name = "Regiones", description = "Gestión de regiones")
public class ControladorRegion {

    private final ServicioRegion servicioRegion;

    public ControladorRegion(ServicioRegion servicioRegion) {
        this.servicioRegion = servicioRegion;
    }

    @GetMapping
    @Operation(summary = "Listar regiones", description = "Devuelve todas las regiones")
    public List<Region> listar() {
        return servicioRegion.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener región", description = "Devuelve la región por su id")
    public Region obtener(@PathVariable Integer id) {
        return servicioRegion.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear región", description = "Crea una nueva región")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Región creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Region.class), examples = @ExampleObject(value = "{\"id\":1,\"nomRegion\":\"Metropolitana\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Region> crear(@RequestBody Region region) {
        Region creado = servicioRegion.create(region);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar región", description = "Actualiza la región indicada por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Región actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Region.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Región no encontrada")
    })
    public Region actualizar(@PathVariable Integer id, @RequestBody Region region) {
        return servicioRegion.update(id, region);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar región", description = "Elimina la región indicada por id")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        servicioRegion.delete(id);
        return ResponseEntity.noContent().build();
    }
}

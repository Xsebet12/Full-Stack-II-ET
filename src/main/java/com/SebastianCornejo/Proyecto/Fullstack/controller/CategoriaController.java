package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCategoria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gestión de categorías")
public class CategoriaController {

    private final ServicioCategoria servicioCategoria;

    public CategoriaController(ServicioCategoria servicioCategoria) {
        this.servicioCategoria = servicioCategoria;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías")
    public List<Categoria> listar() {
    return servicioCategoria.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría", description = "Devuelve la categoría por su id")
    public Categoria obtener(@PathVariable Long id) {
    return servicioCategoria.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class), examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"Electrónica\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Categoria> crear(@RequestBody Categoria categoria) {
    Categoria creado = servicioCategoria.create(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza la categoría indicada por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public Categoria actualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
    return servicioCategoria.update(id, categoria);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina la categoría indicada por id")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    servicioCategoria.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioImagenProducto extends JpaRepository<ImagenProducto, Long> {
    // Permite recuperar todas las imágenes asociadas a un producto.
    // Se usa, por ejemplo, antes de eliminar un producto para conocer las URLs
    // y así poder borrar los archivos físicos correspondientes en el filesystem.
    List<ImagenProducto> findAllByProductoId(Long productoId);
}

package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;

import java.util.List;

public interface ServicioProducto {
    /**
     * Recupera todos los productos disponibles.
     */
    List<Producto> findAll();

    /**
     * Recupera únicamente los productos habilitados (uso público).
     */
    List<Producto> findAllEnabled();

    /**
     * Recupera productos filtrando por el flag habilitado (true/false).
     */
    List<Producto> findAllByHabilitado(Boolean habilitado);

    /**
     * Busca un producto por su id. Si no existe lanza ResourceNotFoundException.
     */
    Producto findById(Long id);

    /** Recupera productos asociados a un proveedor. */
    List<Producto> findByProveedorId(Long proveedorId);

    /**
     * Crea un nuevo producto (sin imágenes). Valida categoría y persiste.
     */
    Producto create(Producto producto);

    /**
     * Actualiza un producto existente con los campos provistos.
     */
    Producto update(Long id, Producto producto);

    /**
     * Elimina el producto indicado por id.
     */
    void delete(Long id);

    /**
     * Actualiza únicamente la URL de la imagen principal del producto.
     */
    Producto updateImage(Long id, String urlImagen);

    /**
     * Crea un producto y asocia las URLs de imágenes pasadas (por ejemplo, cargadas
     * previamente por un endpoint que almacena ficheros). El método monta las entidades
     * ProductImage y persiste por cascada.
     */
    Producto createWithImages(Producto producto, List<String> urlsImagenes);

    /**
     * Añade una imagen adicional a un producto ya existente y la persiste.
     */
    Producto addImage(Long productoId, String urlImagen);

    /**
     * Cambia únicamente el estado 'habilitado' de un producto existente.
     */
    Producto setHabilitado(Long id, Boolean habilitado);

    /**
     * Elimina una imagen específica asociada a un producto. Si la imagen eliminada
     * era la principal, asigna como principal la primera disponible o null si no hay.
     */
    Producto removeImage(Long productoId, Long imageId);

    /**
     * Establece como imagen principal una de las imágenes existentes del producto.
     */
    Producto setMainImage(Long productoId, Long imageId);
}

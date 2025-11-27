package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ImagenProducto;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCategoria;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProducto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Objects;

@Service
public class ServicioProductoImpl implements ServicioProducto {

    private final RepositorioProducto repositorioProducto;
    private final RepositorioCategoria repositorioCategoria;
    private final RepositorioProveedor repositorioProveedor;

    public ServicioProductoImpl(RepositorioProducto repositorioProducto, RepositorioCategoria repositorioCategoria, RepositorioProveedor repositorioProveedor) {
        this.repositorioProducto = repositorioProducto;
        this.repositorioCategoria = repositorioCategoria;
        this.repositorioProveedor = repositorioProveedor;
    }

    @Override
    public List<Producto> findAll() {
        // Devuelve todos los productos (uso administrativo)
    return repositorioProducto.findAll();
    }

    @Override
    public List<Producto> findAllEnabled() {
        // Devuelve únicamente los productos habilitados (uso público)
    return repositorioProducto.findAllByHabilitadoTrue();
    }

    @Override
    public List<Producto> findAllByHabilitado(Boolean habilitado) {
        if (habilitado == null) {
            return findAll();
        }
        return repositorioProducto.findAllByHabilitado(habilitado);
    }

    @Override
    public Producto findById(Long id) {
    // Busca un producto por id, si no existe lanza ResourceNotFoundException
    return repositorioProducto.findById(Objects.requireNonNull(id))
    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    @Override
    public List<Producto> findByProveedorId(Long proveedorId) {
        return repositorioProducto.findByProveedorId(proveedorId);
    }
    @Override
    public Producto create(Producto producto) {
        // Validar que el cliente proporcione una categoría
    if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new PeticionInvalidaException("Debe indicar la categoría mediante su id en el objeto 'category'");
        }
    // Validar y setear proveedor (si viene)
    if (producto.getProveedor() != null) {
        if (producto.getProveedor().getId() == null) {
            throw new PeticionInvalidaException("Si se especifica 'proveedor' debe incluir su id");
        }
        Proveedor proveedor = repositorioProveedor.findById(Objects.requireNonNull(producto.getProveedor().getId()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id " + producto.getProveedor().getId()));
        producto.setProveedor(proveedor);
    }
    // Obtener la categoría real desde la BD para evitar referencias inconsistentes
    Categoria categoria = repositorioCategoria.findById(Objects.requireNonNull(producto.getCategoria().getId()))
    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + producto.getCategoria().getId()));
    producto.setCategoria(categoria);
        // Asegurar habilitado por defecto cuando no venga especificado
        if (producto.getHabilitado() == null) {
            producto.setHabilitado(Boolean.TRUE);
        }
        try {
            // Persistir el producto
            return Objects.requireNonNull(repositorioProducto.save(producto));
        } catch (DataIntegrityViolationException e) {
            // Mapear violaciones de integridad a BadRequest para enviar 400 al cliente
            throw new PeticionInvalidaException("Violación de integridad de datos al crear producto: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto update(Long id, Producto producto) {
        Producto existing = findById(id);
        existing.setNombre(producto.getNombre());
        existing.setDescripcion(producto.getDescripcion());
        existing.setPrecio(producto.getPrecio());
        existing.setStock(producto.getStock());
        // Actualizar estado 'habilitado' si el cliente lo envía
        if (producto.getHabilitado() != null) {
            existing.setHabilitado(producto.getHabilitado());
        }
        // Actualizar URL de imagen si viene en la petición
        if (producto.getImagen() != null) {
            existing.setImagen(producto.getImagen());
        }
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            Categoria categoria = repositorioCategoria.findById(Objects.requireNonNull(producto.getCategoria().getId()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + producto.getCategoria().getId()));
            existing.setCategoria(categoria);
        }
    if (producto.getProveedor() != null && producto.getProveedor().getId() != null) {
        Proveedor proveedor = repositorioProveedor.findById(Objects.requireNonNull(producto.getProveedor().getId()))
            .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id " + producto.getProveedor().getId()));
        existing.setProveedor(proveedor);
    }
        try {
            // Guardar cambios del producto
            return Objects.requireNonNull(repositorioProducto.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar producto: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto updateImage(Long id, String urlImagen) {
        Producto existing = findById(id);
        existing.setImagen(urlImagen);
        try {
            // Actualiza únicamente la URL principal de la imagen
            return Objects.requireNonNull(repositorioProducto.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar imagen: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto createWithImages(Producto producto, List<String> urlsImagenes) {
        // Crea un producto y asocia las URLs de imágenes pasadas. El flujo es:
        // 1) Validar categoría y resolverla desde DB
        // 2) Construir entidades ProductImage asociadas al objeto Product
        // 3) Asignar colección y establecer la imagen principal si no existía
        // 4) Persistir el producto (las imágenes se persistirán por cascade)
        // Validar y setear categoría
    if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new PeticionInvalidaException("Debe indicar la categoría mediante su id en el objeto 'category'");
        }
    Categoria categoria = repositorioCategoria.findById(Objects.requireNonNull(producto.getCategoria().getId()))
        .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + producto.getCategoria().getId()));
    producto.setCategoria(categoria);

        // Validar y setear proveedor
    if (producto.getProveedor() == null || producto.getProveedor().getId() == null) {
        throw new PeticionInvalidaException("Debe indicar el proveedor mediante su id en el objeto 'proveedor'");
    }
    Proveedor proveedor = repositorioProveedor.findById(Objects.requireNonNull(producto.getProveedor().getId()))
        .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id " + producto.getProveedor().getId()));
    producto.setProveedor(proveedor);
        // Asegurar habilitado por defecto cuando se crea con imágenes
        if (producto.getHabilitado() == null) {
            producto.setHabilitado(Boolean.TRUE);
        }

        // Construir conjunto de ProductImage
        // Si vienen URLs de imágenes, construir los objetos ProductImage asociados
        if (urlsImagenes != null && !urlsImagenes.isEmpty()) {
            LinkedHashSet<ImagenProducto> imagenes = new LinkedHashSet<>();
            for (String url : urlsImagenes) {
                if (url == null || url.isBlank()) continue;
                // Construir entidad ImagenProducto sin persistir aún (se persistirá por cascade si está configurado)
                ImagenProducto pi = ImagenProducto.builder().url(url).producto(producto).build();
                imagenes.add(pi);
            }
            if (!imagenes.isEmpty()) {
                // Asignar colección y establecer la imagen principal si no estaba definida
                producto.setImagenes(imagenes);
                if (producto.getImagen() == null) {
                    producto.setImagen(imagenes.iterator().next().getUrl());
                }
            }
        }
        try {
            return Objects.requireNonNull(repositorioProducto.save(producto));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al crear producto con imágenes: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto addImage(Long productId, String urlImagen) {
        Producto existing = findById(productId);
        if (urlImagen == null || urlImagen.isBlank()) {
            throw new PeticionInvalidaException("URL de imagen inválida");
        }
        Set<ImagenProducto> imagenes = existing.getImagenes();
        if (imagenes == null) {
            imagenes = new LinkedHashSet<>();
            existing.setImagenes(imagenes);
        }
        ImagenProducto pi = ImagenProducto.builder().url(urlImagen).producto(existing).build();
        // Añadir la nueva imagen a la colección del producto
        imagenes.add(pi);
        // Establecer como imagen principal si aún no existe
        if (existing.getImagen() == null) {
            existing.setImagen(urlImagen);
        }
        try {
            return Objects.requireNonNull(repositorioProducto.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al agregar imagen: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        Producto existing = findById(id);
        repositorioProducto.delete(Objects.requireNonNull(existing));
    }

    @Override
    public Producto setHabilitado(Long id, Boolean habilitado) {
        Producto existing = findById(id);
        existing.setHabilitado(habilitado == null ? Boolean.FALSE : habilitado);
        try {
            return Objects.requireNonNull(repositorioProducto.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al cambiar estado habilitado: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto removeImage(Long productoId, Long imageId) {
        Producto existing = findById(productoId);
        Set<ImagenProducto> imagenes = existing.getImagenes();
        if (imagenes == null || imagenes.isEmpty()) {
            return existing; // nada que eliminar
        }
        String currentMain = existing.getImagen();
        boolean removed = false;
        Iterator<ImagenProducto> it = imagenes.iterator();
        while (it.hasNext()) {
            ImagenProducto ip = it.next();
            if (ip.getId() != null && ip.getId().equals(imageId)) {
                // eliminar del set (orphanRemoval=true)
                it.remove();
                removed = true;
                // si era principal, recalcular
                if (ip.getUrl() != null && ip.getUrl().equals(currentMain)) {
                    String newMain = null;
                    for (ImagenProducto rest : imagenes) { newMain = rest.getUrl(); break; }
                    existing.setImagen(newMain);
                }
                break;
            }
        }
        if (!removed) {
            // no encontrada: considerar lanzar 404
            throw new RecursoNoEncontradoException("Imagen no encontrada con id " + imageId + " para producto " + productoId);
        }
        try {
            return repositorioProducto.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al eliminar imagen: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Producto setMainImage(Long productoId, Long imageId) {
        Producto existing = findById(productoId);
        Set<ImagenProducto> imagenes = existing.getImagenes();
        if (imagenes == null || imagenes.isEmpty()) {
            throw new RecursoNoEncontradoException("El producto no tiene imágenes");
        }
        String targetUrl = null;
        for (ImagenProducto ip : imagenes) {
            if (ip.getId() != null && ip.getId().equals(imageId)) {
                targetUrl = ip.getUrl();
                break;
            }
        }
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new RecursoNoEncontradoException("Imagen no encontrada con id " + imageId + " para producto " + productoId);
        }
        existing.setImagen(targetUrl);
        try {
            return repositorioProducto.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al establecer imagen principal: " + e.getMostSpecificCause().getMessage());
        }
    }
}

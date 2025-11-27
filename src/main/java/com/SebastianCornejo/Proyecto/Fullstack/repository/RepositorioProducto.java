package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioProducto extends JpaRepository<Producto, Long> {
	/** Cuenta la cantidad de productos que pertenecen a una categoría. */
	long countByCategoriaId(Long categoriaId);

    /** Devuelve todos los productos marcados como habilitados. */
    List<Producto> findAllByHabilitadoTrue();

	/** Devuelve productos filtrando por el flag habilitado (true/false). */
	List<Producto> findAllByHabilitado(Boolean habilitado);

	/** Buscar por proveedor (provider_id en la BD).
	 * Nota: mantenemos el nombre de la columna provider_id en la entidad Producto mediante @JoinColumn
	 * por lo que aquí usamos la propiedad 'proveedor.id' en la consulta generada por Spring Data.
	 */
	List<Producto> findByProveedorId(Long proveedorId);

	long countByProveedorId(Long proveedorId);
}

package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ContactoProveedor;

import java.util.List;

/**
 * Interface en español que expone los métodos del servicio de proveedores.
 */
public interface ServicioProveedor {
    List<Proveedor> findAll();
    Proveedor findById(Long id);
    Proveedor create(Proveedor proveedor);
    Proveedor update(Long id, Proveedor proveedor);
    void delete(Long id);

    Proveedor addContact(Long proveedorId, ContactoProveedor contacto);
    Proveedor updateContact(Long proveedorId, Long contactoId, ContactoProveedor actualizacion);
    Proveedor removeContact(Long proveedorId, Long contactoId);

    Proveedor updateLogo(Long id, String urlLogo);

    /**
     * Recupera la lista de proveedores cuyo estado es ACTIVO (uso público).
     */
    List<Proveedor> findAllActivos();

    /**
     * Recupera proveedores por estado: true => ACTIVO, false => INACTIVO.
     */
    List<Proveedor> findAllPorEstado(Boolean activo);

    /**
     * Cambia únicamente el estado del proveedor: true => ACTIVO, false => INACTIVO
     */
    Proveedor setEstado(Long id, Boolean activo);

    /**
     * Devuelve la lista de contactos asociados a un proveedor.
     * Retorna lista vacía si no tiene contactos.
     */
    List<ContactoProveedor> listarContactos(Long proveedorId);

    /**
     * Marca un contacto como principal y desmarca los demás del mismo proveedor.
     */
    Proveedor setPrincipal(Long proveedorId, Long contactoId);
}

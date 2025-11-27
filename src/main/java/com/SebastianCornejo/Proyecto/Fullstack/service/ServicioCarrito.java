package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudOperacionItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaCarrito;
import org.springframework.security.core.userdetails.UserDetails;

public interface ServicioCarrito {
    /**
     * Obtiene la representación del carrito para el usuario autenticado.
     */
    RespuestaCarrito getMyCart(UserDetails principal);

    /**
     * Agrega una cantidad de producto al carrito (crea item si no existe).
     */
    RespuestaCarrito addItem(UserDetails principal, SolicitudOperacionItemCarrito request);

    /**
     * Reduce la cantidad de un producto en el carrito o lo elimina si la
     * cantidad resultante es cero o negativa.
     */
    RespuestaCarrito removeItem(UserDetails principal, SolicitudOperacionItemCarrito request);

    /**
     * Elimina todos los items del carrito del usuario.
     */
    void clear(UserDetails principal);
}

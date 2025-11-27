package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudOperacionItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Carrito;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCarrito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class ServicioCarritoImpl implements ServicioCarrito {

    private final RepositorioCarrito repositorioCarrito;
    private final RepositorioItemCarrito repositorioItemCarrito;
    private final RepositorioProducto repositorioProducto;
    private final RepositorioUsuario repositorioUsuario;

    public ServicioCarritoImpl(RepositorioCarrito repositorioCarrito,
                           RepositorioItemCarrito repositorioItemCarrito,
                           RepositorioProducto repositorioProducto,
                           RepositorioUsuario repositorioUsuario) {
        this.repositorioCarrito = repositorioCarrito;
        this.repositorioItemCarrito = repositorioItemCarrito;
        this.repositorioProducto = repositorioProducto;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaCarrito getMyCart(UserDetails principal) {
    Usuario user = resolveUser(principal);
    Carrito cart = repositorioCarrito.findByUsuario(Objects.requireNonNull(user))
        .orElseGet(() -> repositorioCarrito.save(Objects.requireNonNull(Carrito.builder()
            .usuario(user)
            .items(new HashSet<>())
            .build())));
        // Construye y retorna una vista DTO del carrito (items, totales, conteo)
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public RespuestaCarrito addItem(UserDetails principal, SolicitudOperacionItemCarrito request) {
        validateRequest(request);
    Usuario user = resolveUser(principal);
        Carrito cart = repositorioCarrito.findByUsuario(Objects.requireNonNull(user))
        .orElseGet(() -> repositorioCarrito.save(Objects.requireNonNull(Carrito.builder()
            .usuario(user)
            .items(new HashSet<>())
            .build())));
    Producto producto = repositorioProducto.findById(Objects.requireNonNull(request.getProductoId()))
    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + request.getProductoId()));
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PeticionInvalidaException("Producto sin precio válido");
        }
    ItemCarrito item = repositorioItemCarrito.findByCarritoIdAndProductoId(
            Objects.requireNonNull(cart.getId()),
            Objects.requireNonNull(producto.getId()))
        .orElse(null);
        int disponible = Math.max(0, producto.getStock() == null ? 0 : producto.getStock());
        int baseQty = item == null ? 0 : item.getCantidad();
        int nuevaCantidad = baseQty + request.getCantidad();
        if (nuevaCantidad > disponible) {
            throw new PeticionInvalidaException("Stock insuficiente. Disponible: " + disponible);
        }
        if (item == null) {
            item = ItemCarrito.builder()
                .carrito(cart)
                .producto(producto)
                .cantidad(request.getCantidad())
                .precioUnitario(producto.getPrecio())
                .build();
            if (cart.getItems() == null) cart.setItems(new HashSet<>());
            cart.getItems().add(Objects.requireNonNull(item));
        } else {
            item.setCantidad(nuevaCantidad);
        }
    repositorioItemCarrito.save(Objects.requireNonNull(item));
        // Se persiste o actualiza el item y se retorna la vista actualizada del carrito.
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public RespuestaCarrito removeItem(UserDetails principal, SolicitudOperacionItemCarrito request) {
        validateRequest(request);
    Usuario user = resolveUser(principal);
        Carrito cart = repositorioCarrito.findByUsuario(Objects.requireNonNull(user))
        .orElseGet(() -> repositorioCarrito.save(Objects.requireNonNull(Carrito.builder()
            .usuario(user)
            .items(new HashSet<>())
            .build())));
    ItemCarrito item = repositorioItemCarrito.findByCarritoIdAndProductoId(
                Objects.requireNonNull(cart.getId()),
                Objects.requireNonNull(request.getProductoId()))
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no está en el carrito"));

    int newQty = item.getCantidad() - request.getCantidad();
        if (newQty <= 0) {
            repositorioItemCarrito.delete(item);
        } else {
            item.setCantidad(newQty);
            repositorioItemCarrito.save(item);
        }
        // Devuelve la vista del carrito actualizada después de la eliminación/parcialmente actualización
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public void clear(UserDetails principal) {
    Usuario user = resolveUser(principal);
    repositorioCarrito.findByUsuario(user).ifPresent(cart -> {
            // Eliminar por repositorio para asegurar sincronización inmediata
            List<ItemCarrito> items = repositorioItemCarrito.findByCarritoId(cart.getId());
            if (!items.isEmpty()) {
                repositorioItemCarrito.deleteAll(items);
            }
        });
    }

    private void validateRequest(SolicitudOperacionItemCarrito request) {
        if (request == null) {
            throw new PeticionInvalidaException("Debe enviar la solicitud con producto y cantidad");
        }
        if (request.getProductoId() == null) {
            throw new PeticionInvalidaException("Debe indicar el productoId");
        }
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new PeticionInvalidaException("La cantidad debe ser mayor que cero");
        }
    }

    private Usuario resolveUser(UserDetails principal) {
        if (principal == null) {
            throw new PeticionInvalidaException("Usuario no autenticado");
        }
    return repositorioUsuario.findByCorreo(principal.getUsername())
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + principal.getUsername()));
    }
    private RespuestaCarrito buildResponse(Carrito cart) {
        java.util.Set<ItemCarrito> set = cart.getItems();
        List<ItemCarrito> items = new ArrayList<>();
        if (set != null && !set.isEmpty()) items.addAll(set);
        List<RespuestaItemCarrito> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
    for (ItemCarrito it : items) {
        BigDecimal subtotal = it.getSubtotal();
        total = total.add(subtotal);
        count += it.getCantidad();
        itemResponses.add(RespuestaItemCarrito.builder()
            .productoId(it.getProducto().getId())
            .nombre(it.getProducto().getNombre())
            .precioUnitario(it.getPrecioUnitario())
            .cantidad(it.getCantidad())
            .subtotal(subtotal)
            .stockDisponible(it.getProducto().getStock())
            .build());
    }
        // Mapea los items a DTOs, calcula total y cantidad total, y retorna el CartResponse.
        return RespuestaCarrito.builder()
                .items(itemResponses)
                .total(total)
                .cantidadItems(count)
                .build();
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaVenta;
import com.SebastianCornejo.Proyecto.Fullstack.entity.*;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudVentaDirecta;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Procesamiento de compras y actualización de stock")
@SecurityRequirement(name = "bearerAuth")
public class ControladorVentas {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioCarrito repositorioCarrito;
    private final RepositorioItemCarrito repositorioItemCarrito;
    private final RepositorioProducto repositorioProducto;
    private final RepositorioVenta repositorioVenta;
    private final RepositorioDetalleVenta repositorioDetalleVenta;

    public ControladorVentas(RepositorioUsuario repositorioUsuario,
                             RepositorioCarrito repositorioCarrito,
                             RepositorioItemCarrito repositorioItemCarrito,
                             RepositorioProducto repositorioProducto,
                             RepositorioVenta repositorioVenta,
                             RepositorioDetalleVenta repositorioDetalleVenta) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCarrito = repositorioCarrito;
        this.repositorioItemCarrito = repositorioItemCarrito;
        this.repositorioProducto = repositorioProducto;
        this.repositorioVenta = repositorioVenta;
        this.repositorioDetalleVenta = repositorioDetalleVenta;
    }

    @PostMapping("/ingresar")
    @Operation(summary = "Ingresar venta", description = "Valida stock, genera venta y detalles, actualiza stock y limpia carrito")
    @Transactional
    public ResponseEntity<RespuestaVenta> ingresar(@AuthenticationPrincipal UserDetails principal){
        if (principal == null) throw new PeticionInvalidaException("Usuario no autenticado");
        Usuario u = repositorioUsuario.findByCorreo(principal.getUsername())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Carrito cart = repositorioCarrito.findByUsuario(u)
                .orElseGet(() -> repositorioCarrito.save(Carrito.builder().usuario(u).items(new HashSet<>()).build()));
        Set<ItemCarrito> itemsSet = cart.getItems();
        List<ItemCarrito> items = repositorioItemCarrito.findByCarritoId(cart.getId());
        if (items.isEmpty()) throw new PeticionInvalidaException("El carrito está vacío");

        BigDecimal total = BigDecimal.ZERO;
        // Validación de stock y cálculo de total
        for (ItemCarrito it : items) {
            Producto p = repositorioProducto.findById(it.getProducto().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            int disponible = Math.max(0, p.getStock() == null ? 0 : p.getStock());
            if (it.getCantidad() > disponible) {
                throw new PeticionInvalidaException("Stock insuficiente para " + p.getNombre() + ". Disponible: " + disponible);
            }
            total = total.add(it.getSubtotal());
        }

        // Persistir venta
        Venta venta = Venta.builder()
                .usuario(u)
                .fecha(Instant.now())
                .total(total)
                .build();
        venta = repositorioVenta.save(venta);

        // Persistir detalles y actualizar stock
        for (ItemCarrito it : items) {
            Producto p = repositorioProducto.findById(it.getProducto().getId()).orElseThrow();
            int nuevoStock = Math.max(0, (p.getStock() == null ? 0 : p.getStock()) - it.getCantidad());
            p.setStock(nuevoStock);
            repositorioProducto.save(p);
            DetalleVenta dv = DetalleVenta.builder()
                    .venta(venta)
                    .producto(p)
                    .cantidad(it.getCantidad())
                    .precioUnitario(it.getPrecioUnitario())
                    .subtotal(it.getSubtotal())
                    .build();
            repositorioDetalleVenta.save(dv);
        }

        // Limpiar carrito
        if (!items.isEmpty()) {
            repositorioItemCarrito.deleteAll(items);
        }

        RespuestaVenta resp = new RespuestaVenta(venta.getId(), total, items.stream().mapToInt(ItemCarrito::getCantidad).sum(), "Compra registrada exitosamente");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/ingresar-directa")
    @Operation(summary = "Ingresar venta directa", description = "Genera venta y detalles desde items del body")
    @Transactional
    public ResponseEntity<RespuestaVenta> ingresarDirecta(@AuthenticationPrincipal UserDetails principal, @RequestBody SolicitudVentaDirecta body){
        if (principal == null) throw new PeticionInvalidaException("Usuario no autenticado");
        if (body == null || body.getItems() == null || body.getItems().isEmpty()) throw new PeticionInvalidaException("Items vacíos");
        Usuario u = repositorioUsuario.findByCorreo(principal.getUsername())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        BigDecimal total = BigDecimal.ZERO;
        for (SolicitudVentaDirecta.Item it : body.getItems()) {
            if (it.getProductoId() == null || it.getCantidad() == null || it.getCantidad() <= 0) throw new PeticionInvalidaException("Item inválido");
            Producto p = repositorioProducto.findById(it.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            int disponible = Math.max(0, p.getStock() == null ? 0 : p.getStock());
            if (it.getCantidad() > disponible) throw new PeticionInvalidaException("Stock insuficiente para " + p.getNombre() + ". Disponible: " + disponible);
            if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) throw new PeticionInvalidaException("Producto sin precio válido");
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(it.getCantidad())));
        }
        Venta venta = Venta.builder().usuario(u).fecha(Instant.now()).total(total).build();
        venta = repositorioVenta.save(venta);
        for (SolicitudVentaDirecta.Item it : body.getItems()) {
            Producto p = repositorioProducto.findById(it.getProductoId()).orElseThrow();
            int nuevoStock = Math.max(0, (p.getStock() == null ? 0 : p.getStock()) - it.getCantidad());
            p.setStock(nuevoStock);
            repositorioProducto.save(p);
            DetalleVenta dv = DetalleVenta.builder()
                    .venta(venta)
                    .producto(p)
                    .cantidad(it.getCantidad())
                    .precioUnitario(p.getPrecio())
                    .subtotal(p.getPrecio().multiply(BigDecimal.valueOf(it.getCantidad())))
                    .build();
            repositorioDetalleVenta.save(dv);
        }
        RespuestaVenta resp = new RespuestaVenta(venta.getId(), total, body.getItems().stream().mapToInt(SolicitudVentaDirecta.Item::getCantidad).sum(), "Compra registrada exitosamente");
        return ResponseEntity.ok(resp);
    }
}

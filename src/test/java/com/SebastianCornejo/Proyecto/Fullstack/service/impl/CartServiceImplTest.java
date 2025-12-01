package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudOperacionItemCarrito;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private RepositorioCarrito cartRepository;
    @Mock
    private RepositorioItemCarrito cartItemRepository;
    @Mock
    private RepositorioProducto productRepository;
    @Mock
    private RepositorioUsuario userRepository;

    @InjectMocks
    private ServicioCarritoImpl cartService;

    private Usuario user;
    private UserDetails principal;
    private Carrito cart;
    private Producto product;
    private ItemCarrito item;

    @BeforeEach
    void setUp() {
    user = Usuario.builder().id(1L).correo("client@example.com").habilitado(true).contrasena("pw").build();
    principal = new org.springframework.security.core.userdetails.User("client@example.com", "pw", List.of(() -> "ROLE_CLIENTE"));
    product = Producto.builder().id(100L).nombre("Producto").precio(new BigDecimal("100.00")).stock(10).habilitado(true).build();
        item = ItemCarrito.builder().id(10L).producto(product).cantidad(2).precioUnitario(product.getPrecio()).build();
        cart = Carrito.builder().id(5L).usuario(user).items(new HashSet<>(Set.of(item))).build();
    }

    @Test
    @DisplayName("getMyCart devuelve el carrito existente con totales correctos")
    void testGetMyCartExisting() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));

    RespuestaCarrito response = cartService.getMyCart(principal);
    assertEquals(1, response.getItems().size());
    assertEquals(new BigDecimal("200.00"), response.getTotal());
    assertEquals(2, response.getCantidadItems());

        verify(userRepository).findByCorreo("client@example.com");
        verify(cartRepository).findByUsuario(user);
    }

    @Test
    @DisplayName("getMyCart lanza BadRequest si el principal es nulo")
    void testGetMyCartPrincipalNull() {
    assertThrows(PeticionInvalidaException.class, () -> cartService.getMyCart(null));
        verifyNoInteractions(userRepository, cartRepository, cartItemRepository, productRepository);
    }

    @Test
    @DisplayName("getMyCart lanza ResourceNotFound si el usuario no existe")
    void testGetMyCartUserNotFound() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.empty());
    assertThrows(RecursoNoEncontradoException.class, () -> cartService.getMyCart(principal));
        verify(userRepository).findByCorreo("client@example.com");
        verifyNoInteractions(cartRepository, cartItemRepository, productRepository);
    }

    @Test
    @DisplayName("addItem incrementa cantidad de item existente y retorna totales")
    void testAddItemIncrementExisting() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCarritoIdAndProductoId(cart.getId(), product.getId())).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(ItemCarrito.class))).thenAnswer(invocation -> Objects.requireNonNull(invocation.getArgument(0)));

    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(100L);
    req.setCantidad(2);
    RespuestaCarrito resp = cartService.addItem(principal, req);

        assertEquals(1, resp.getItems().size());
    assertEquals(4, resp.getCantidadItems());
        assertEquals(new BigDecimal("400.00"), resp.getTotal());
        assertEquals(4, item.getCantidad());

        verify(cartItemRepository).save(Objects.requireNonNull(item));
    }

    @Test
    @DisplayName("addItem crea item cuando no existe y lo incluye en respuesta")
    void testAddItemCreateNew() {
        Carrito emptyCart = Carrito.builder().id(6L).usuario(user).items(new HashSet<>()).build();
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(emptyCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCarritoIdAndProductoId(emptyCart.getId(), product.getId())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(ItemCarrito.class))).thenAnswer(invocation -> {
            ItemCarrito saved = Objects.requireNonNull(invocation.getArgument(0));
            // Simulate persistence linking: add to cart items so buildResponse sees it
            emptyCart.getItems().add(saved);
            return Objects.requireNonNull(saved);
        });

    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(100L);
    req.setCantidad(3);
    RespuestaCarrito resp = cartService.addItem(principal, req);

        assertEquals(1, resp.getItems().size());
    assertEquals(3, resp.getCantidadItems());
        assertEquals(new BigDecimal("300.00"), resp.getTotal());
        verify(cartItemRepository).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("addItem valida solicitud y arroja BadRequest en casos inválidos")
    void testAddItemValidationErrors() {
        // null request
    assertThrows(PeticionInvalidaException.class, () -> cartService.addItem(principal, null));
    // missing productoId
    SolicitudOperacionItemCarrito req1 = new SolicitudOperacionItemCarrito();
    req1.setCantidad(1);
    assertThrows(PeticionInvalidaException.class, () -> cartService.addItem(principal, req1));
        // invalid quantity
    SolicitudOperacionItemCarrito req2 = new SolicitudOperacionItemCarrito();
    req2.setProductoId(100L);
    req2.setCantidad(0);
    assertThrows(PeticionInvalidaException.class, () -> cartService.addItem(principal, req2));
    }

    @Test
    @DisplayName("addItem arroja ResourceNotFound si el producto no existe")
    void testAddItemProductNotFound() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(999L);
    req.setCantidad(1);
    assertThrows(RecursoNoEncontradoException.class, () -> cartService.addItem(principal, req));
        verify(productRepository).findById(999L);
    }

    @Test
    @DisplayName("removeItem reduce cantidad y guarda, totales actualizados")
    void testRemoveItemReduceQuantity() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCarritoIdAndProductoId(cart.getId(), product.getId())).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(ItemCarrito.class))).thenAnswer(invocation -> Objects.requireNonNull(invocation.getArgument(0)));

    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(100L);
    req.setCantidad(1);
    RespuestaCarrito resp = cartService.removeItem(principal, req);

        assertEquals(1, resp.getItems().size());
    assertEquals(1, resp.getCantidadItems());
        assertEquals(new BigDecimal("100.00"), resp.getTotal());
        assertEquals(1, item.getCantidad());
        verify(cartItemRepository).save(Objects.requireNonNull(item));
    }

    @Test
    @DisplayName("removeItem elimina el item cuando la nueva cantidad es <= 0")
    void testRemoveItemDeletes() {
        Carrito cartWithItem = Carrito.builder().id(7L).usuario(user).items(new HashSet<>(Set.of(item))).build();
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cartWithItem));
        when(cartItemRepository.findByCarritoIdAndProductoId(cartWithItem.getId(), product.getId())).thenReturn(Optional.of(item));
        doAnswer(invocation -> {
            // simulate deletion linkage
            cartWithItem.getItems().remove(item);
            return null;
        }).when(cartItemRepository).delete(argThat(Objects::nonNull));

    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(100L);
    req.setCantidad(2);
    RespuestaCarrito resp = cartService.removeItem(principal, req);

        assertEquals(0, resp.getItems().size());
    assertEquals(0, resp.getCantidadItems());
        assertEquals(0, resp.getTotal().compareTo(new BigDecimal("0.00")));
        verify(cartItemRepository).delete(Objects.requireNonNull(item));
    }

    @Test
    @DisplayName("removeItem arroja ResourceNotFound si el item no está en el carrito")
    void testRemoveItemNotInCart() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCarritoIdAndProductoId(cart.getId(), 999L)).thenReturn(Optional.empty());
    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(999L);
    req.setCantidad(1);
    assertThrows(RecursoNoEncontradoException.class, () -> cartService.removeItem(principal, req));
    }

    @Test
    @DisplayName("clear elimina todos los items del carrito existente")
    void testClear() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCarritoId(cart.getId())).thenReturn(Objects.requireNonNull(java.util.List.of(item)));

        cartService.clear(principal);

        verify(cartItemRepository).findByCarritoId(cart.getId());
        verify(cartItemRepository).deleteAll(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("clear no falla si el carrito no existe")
    void testClearNoCart() {
        when(userRepository.findByCorreo("client@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsuario(user)).thenReturn(Optional.empty());
        cartService.clear(principal);
        verify(cartRepository).findByUsuario(user);
        verifyNoInteractions(cartItemRepository);
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudOperacionItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaItemCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaCarrito;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCarrito;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import com.SebastianCornejo.Proyecto.Fullstack.testconfig.TestSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Objects;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControladorCarrito.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CartControllerWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class CartControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
        private ServicioCarrito servicioCarrito;

    // jwtAuthenticationFilter no es usado en estos tests

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioCarrito servicioCarrito() {
            return Mockito.mock(ServicioCarrito.class);
        }
        // Beans de seguridad importados desde TestSecurityConfig
    }

    @Test
        @DisplayName("GET /api/carrito devuelve resumen del carrito")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetCart() throws Exception {
    RespuestaItemCarrito item = RespuestaItemCarrito.builder()
        .productoId(1L)
        .nombre("Laptop")
        .precioUnitario(new BigDecimal("999.99"))
        .cantidad(1)
        .subtotal(new BigDecimal("999.99"))
        .build();
    RespuestaCarrito resp = RespuestaCarrito.builder()
        .items(List.of(item))
        .total(new BigDecimal("999.99"))
        .cantidadItems(1)
        .build();
    Mockito.when(servicioCarrito.getMyCart(Mockito.any(UserDetails.class))).thenReturn(resp);

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$.items[0].nombre", Objects.requireNonNull(is("Laptop"))))
                .andExpect(jsonPath("$.total", Objects.requireNonNull(is(999.99))))
                .andExpect(jsonPath("$.cantidadItems", Objects.requireNonNull(is(1))));
    }

    @Test
        @DisplayName("POST /api/carrito/add agrega producto y devuelve carrito")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testAddItem() throws Exception {
    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(1L);
    req.setCantidad(2);

    RespuestaItemCarrito item = RespuestaItemCarrito.builder()
        .productoId(1L)
        .nombre("Mouse")
        .precioUnitario(new BigDecimal("19.99"))
        .cantidad(2)
        .subtotal(new BigDecimal("39.98"))
        .build();
    RespuestaCarrito resp = RespuestaCarrito.builder()
        .items(List.of(item))
        .total(new BigDecimal("39.98"))
        .cantidadItems(2)
        .build();
    Mockito.when(servicioCarrito.addItem(Mockito.any(UserDetails.class), Mockito.any(SolicitudOperacionItemCarrito.class))).thenReturn(resp);

        mockMvc.perform(post("/api/carrito/add")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productoId", Objects.requireNonNull(is(1))))
                .andExpect(jsonPath("$.items[0].cantidad", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.total", Objects.requireNonNull(is(39.98))))
                .andExpect(jsonPath("$.cantidadItems", Objects.requireNonNull(is(2))));
    }

    @Test
        @DisplayName("POST /api/carrito/remove quita producto y devuelve carrito")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testRemoveItem() throws Exception {
    SolicitudOperacionItemCarrito req = new SolicitudOperacionItemCarrito();
    req.setProductoId(1L);
    req.setCantidad(1);

    RespuestaCarrito resp = RespuestaCarrito.builder()
        .items(List.of())
        .total(new BigDecimal("0.00"))
        .cantidadItems(0)
        .build();
    Mockito.when(servicioCarrito.removeItem(Mockito.any(UserDetails.class), Mockito.any(SolicitudOperacionItemCarrito.class))).thenReturn(resp);

        mockMvc.perform(post("/api/carrito/remove")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", Objects.requireNonNull(hasSize(0))))
                .andExpect(jsonPath("$.total", Objects.requireNonNull(is(0.00))))
                .andExpect(jsonPath("$.cantidadItems", Objects.requireNonNull(is(0))));
    }

    @Test
        @DisplayName("DELETE /api/carrito/clear vacía el carrito y devuelve 204")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testClear() throws Exception {
        mockMvc.perform(delete("/api/carrito/clear"))
                .andExpect(status().isNoContent());
    Mockito.verify(servicioCarrito).clear(Mockito.any(UserDetails.class));
    }
}

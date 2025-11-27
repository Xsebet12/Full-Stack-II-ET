package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProducto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.SebastianCornejo.Proyecto.Fullstack.testconfig.TestSecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
 
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioImagenProducto;

@WebMvcTest(ControladorProducto.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestSecurityConfig.class, ProductControllerAuthSimpleTest.TestConfig.class})
class ProductControllerAuthSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioProducto servicioProducto;


    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioProducto servicioProducto() {
            return Mockito.mock(ServicioProducto.class);
        }
        @Bean
        public RepositorioImagenProducto repositorioImagenProducto() {
            return Mockito.mock(RepositorioImagenProducto.class);
        }
    }

    @Test
    void anonymous_sees_enabled_only() throws Exception {
        Producto p = Producto.builder().id(1L).nombre("Laptop").habilitado(true).build();
        Mockito.when(servicioProducto.findAllEnabled()).thenReturn(Arrays.asList(p));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("Laptop"))));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void admin_with_all_true_sees_all() throws Exception {
        Producto p1 = Producto.builder().id(1L).nombre("A").habilitado(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("B").habilitado(false).build();
        Mockito.when(servicioProducto.findAll()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/productos").param("all", "true").with(Objects.requireNonNull(user("admin").roles("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(2))))
                .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("A"))))
                .andExpect(jsonPath("$[1].nombre", Objects.requireNonNull(is("B"))));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void non_admin_with_all_true_forbidden() throws Exception {
        mockMvc.perform(get("/api/productos").param("all", "true").with(Objects.requireNonNull(user("user").roles("USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void non_admin_stock_critico_all_true_forbidden() throws Exception {
        mockMvc.perform(get("/api/productos/stock-critico").param("all", "true").with(Objects.requireNonNull(user("user").roles("USER"))))
                .andExpect(status().isForbidden());
    }

    // TestExceptionTranslator and required security mocks are provided by TestSecurityConfig
}

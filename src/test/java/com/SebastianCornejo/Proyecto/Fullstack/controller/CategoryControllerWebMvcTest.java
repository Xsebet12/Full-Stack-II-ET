package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCategoria;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import com.SebastianCornejo.Proyecto.Fullstack.testconfig.TestSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Objects;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CategoryControllerWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class CategoryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioCategoria servicioCategoria;

    // jwtAuthenticationFilter no es usado en este test

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioCategoria servicioCategoria() {
            return Mockito.mock(ServicioCategoria.class);
        }
        // Beans de seguridad importados desde TestSecurityConfig
    }

    @Test
    @DisplayName("GET /api/categorias devuelve lista")
    void testList() throws Exception {
        Categoria c = Categoria.builder().id(1L).nombre("Electrónica").build();
    Mockito.when(servicioCategoria.findAll()).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("Electrónica"))));
    }

    @Test
    @DisplayName("GET /api/categorias/{id} devuelve categoría")
    void testGet() throws Exception {
        Categoria c = Categoria.builder().id(1L).nombre("Electrónica").build();
    Mockito.when(servicioCategoria.findById(1L)).thenReturn(c);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(1))))
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Electrónica"))));
    }

    @Test
    @DisplayName("POST /api/categorias crea y devuelve categoría")
    void testCreate() throws Exception {
        Categoria req = Categoria.builder().nombre("Accesorios").build();
        Categoria created = Categoria.builder().id(2L).nombre("Accesorios").build();
    Mockito.when(servicioCategoria.create(Mockito.any(Categoria.class))).thenReturn(created);

        mockMvc.perform(post("/api/categorias")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Accesorios"))));
    }

    @Test
    @DisplayName("PUT /api/categorias/{id} actualiza y devuelve categoría")
    void testUpdate() throws Exception {
        Categoria req = Categoria.builder().nombre("Electro").build();
        Categoria updated = Categoria.builder().id(1L).nombre("Electro").build();
    Mockito.when(servicioCategoria.update(Mockito.eq(1L), Mockito.any(Categoria.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Electro"))));
    }

    @Test
    @DisplayName("DELETE /api/categorias/{id} devuelve 204")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    Mockito.verify(servicioCategoria).delete(1L);
    }
}

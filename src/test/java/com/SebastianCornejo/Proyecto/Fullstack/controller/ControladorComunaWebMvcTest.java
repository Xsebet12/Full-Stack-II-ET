package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioComuna;
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

@WebMvcTest(ControladorComuna.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControladorComunaWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class ControladorComunaWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioComuna servicioComuna;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioComuna servicioComuna() {
            return Mockito.mock(ServicioComuna.class);
        }
    }

    @Test
    @DisplayName("GET /api/comunas lista comunas")
    void testListarComunas() throws Exception {
        Comuna c = Comuna.builder().idComuna(1).nomComuna("Santiago").build();
        Mockito.when(servicioComuna.findAll()).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/comunas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nomComuna", Objects.requireNonNull(is("Santiago"))));
    }

    @Test
    @DisplayName("GET /api/comunas/{id}")
    void testObtener() throws Exception {
        Comuna c = Comuna.builder().idComuna(1).nomComuna("Santiago").build();
        Mockito.when(servicioComuna.findById(1)).thenReturn(c);

        mockMvc.perform(get("/api/comunas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomComuna", Objects.requireNonNull(is("Santiago"))));
    }

    @Test
    @DisplayName("POST /api/comunas crea comuna")
    void testCrear() throws Exception {
        Comuna req = Comuna.builder().nomComuna("Valparaiso").build();
        Comuna creado = Comuna.builder().idComuna(2).nomComuna("Valparaiso").build();
        Mockito.when(servicioComuna.create(Mockito.any(Comuna.class))).thenReturn(creado);

        mockMvc.perform(post("/api/comunas")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idComuna", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.nomComuna", Objects.requireNonNull(is("Valparaiso"))));
    }

    @Test
    @DisplayName("PUT /api/comunas/{id} actualiza")
    void testActualizar() throws Exception {
        Comuna req = Comuna.builder().nomComuna("Santiago Centro").build();
        Comuna actualizado = Comuna.builder().idComuna(1).nomComuna("Santiago Centro").build();
        Mockito.when(servicioComuna.update(Mockito.eq(1), Mockito.any(Comuna.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/comunas/1")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomComuna", Objects.requireNonNull(is("Santiago Centro"))));
    }

    @Test
    @DisplayName("DELETE /api/comunas/{id} elimina")
    void testEliminar() throws Exception {
        mockMvc.perform(delete("/api/comunas/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(servicioComuna).delete(1);
    }
}

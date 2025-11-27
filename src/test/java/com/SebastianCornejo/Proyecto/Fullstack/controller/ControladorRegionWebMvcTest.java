package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioRegion;
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

@WebMvcTest(ControladorRegion.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControladorRegionWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class ControladorRegionWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioRegion servicioRegion;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioRegion servicioRegion() {
            return Mockito.mock(ServicioRegion.class);
        }
    }

    @Test
    @DisplayName("GET /api/regiones lista regiones")
    void testListarRegiones() throws Exception {
        Region r = Region.builder().idRegion(1).nomRegion("Metropolitana").build();
        Mockito.when(servicioRegion.findAll()).thenReturn(Arrays.asList(r));

        mockMvc.perform(get("/api/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nomRegion", Objects.requireNonNull(is("Metropolitana"))));
    }

    @Test
    @DisplayName("GET /api/regiones/{id}")
    void testObtener() throws Exception {
        Region r = Region.builder().idRegion(1).nomRegion("Metropolitana").build();
        Mockito.when(servicioRegion.findById(1)).thenReturn(r);

        mockMvc.perform(get("/api/regiones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomRegion", Objects.requireNonNull(is("Metropolitana"))));
    }

    @Test
    @DisplayName("POST /api/regiones crea region")
    void testCrear() throws Exception {
        Region req = Region.builder().nomRegion("Biobio").build();
        Region creado = Region.builder().idRegion(2).nomRegion("Biobio").build();
        Mockito.when(servicioRegion.create(Mockito.any(Region.class))).thenReturn(creado);

        mockMvc.perform(post("/api/regiones")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRegion", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.nomRegion", Objects.requireNonNull(is("Biobio"))));
    }

    @Test
    @DisplayName("PUT /api/regiones/{id} actualiza")
    void testActualizar() throws Exception {
        Region req = Region.builder().nomRegion("Metropolitana Centro").build();
        Region actualizado = Region.builder().idRegion(1).nomRegion("Metropolitana Centro").build();
        Mockito.when(servicioRegion.update(Mockito.eq(1), Mockito.any(Region.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/regiones/1")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomRegion", Objects.requireNonNull(is("Metropolitana Centro"))));
    }

    @Test
    @DisplayName("DELETE /api/regiones/{id} elimina")
    void testEliminar() throws Exception {
        mockMvc.perform(delete("/api/regiones/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(servicioRegion).delete(1);
    }
}

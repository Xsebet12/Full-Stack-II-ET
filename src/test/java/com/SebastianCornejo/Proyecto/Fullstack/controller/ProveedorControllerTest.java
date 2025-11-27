package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ContactoProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProveedor;
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
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.Objects;

@WebMvcTest(ControladorProveedor.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ProveedorControllerTest.TestConfig.class, TestSecurityConfig.class})
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioProveedor servicioProveedor;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioProveedor servicioProveedor() {
            return Mockito.mock(ServicioProveedor.class);
        }
    }

    @Test
    @DisplayName("GET /api/proveedores devuelve lista de proveedores")
    void testListarProveedores() throws Exception {
        Proveedor p = Proveedor.builder().id(1L).companyName("ACME").serviceType("Servicios").build();
    Mockito.when(servicioProveedor.findAllActivos()).thenReturn(Arrays.asList(p));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].companyName", Objects.requireNonNull(is("ACME"))));
    }

    @Test
    @DisplayName("GET /api/proveedores?all=true devuelve todas las entidades")
    @WithMockUser(roles = {"ADMIN"})
    void testListarProveedoresAllTrue() throws Exception {
        Proveedor p1 = Proveedor.builder().id(1L).companyName("ACME").serviceType("Servicios").build();
        Proveedor p2 = Proveedor.builder().id(2L).companyName("Globex").serviceType("Comercio").build();
        Mockito.when(servicioProveedor.findAll()).thenReturn(Arrays.asList(p1, p2));

    mockMvc.perform(get("/api/proveedores?all=true").with(Objects.requireNonNull(user("admin").roles("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(2))))
                .andExpect(jsonPath("$[1].companyName", Objects.requireNonNull(is("Globex"))));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} devuelve proveedor")
    void testObtenerProveedor() throws Exception {
        Proveedor p = Proveedor.builder().id(1L).companyName("ACME").serviceType("Servicios").build();
        Mockito.when(servicioProveedor.findById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/proveedores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(1))))
                .andExpect(jsonPath("$.companyName", Objects.requireNonNull(is("ACME"))));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} devuelve 404 si no existe")
    void testObtenerProveedorNoEncontrado() throws Exception {
    Mockito.when(servicioProveedor.findById(99L)).thenThrow(new RecursoNoEncontradoException("Proveedor no encontrado"));

        mockMvc.perform(get("/api/proveedores/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/proveedores crea y devuelve proveedor")
    void testCrearProveedor() throws Exception {
        Proveedor req = Proveedor.builder().companyName("ACME").serviceType("Servicios").build();
        Proveedor creado = Proveedor.builder().id(2L).companyName("ACME").serviceType("Servicios").build();
        Mockito.when(servicioProveedor.create(Mockito.any(Proveedor.class))).thenReturn(creado);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.companyName", Objects.requireNonNull(is("ACME"))));
    }

    @Test
    @DisplayName("POST /api/proveedores devuelve 400 en petición inválida")
    void testCrearProveedorPeticionInvalida() throws Exception {
        Proveedor req = Proveedor.builder().companyName("").serviceType("Servicios").build();
    Mockito.when(servicioProveedor.create(Mockito.any(Proveedor.class))).thenThrow(new PeticionInvalidaException("Proveedor inválido"));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/proveedores/{id} actualiza y devuelve proveedor")
    void testActualizarProveedor() throws Exception {
        Proveedor req = Proveedor.builder().companyName("ACME Nuevo").serviceType("Servicios").build();
        Proveedor actualizado = Proveedor.builder().id(1L).companyName("ACME Nuevo").serviceType("Servicios").build();
        Mockito.when(servicioProveedor.update(Mockito.eq(1L), Mockito.any(Proveedor.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/proveedores/1")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", Objects.requireNonNull(is("ACME Nuevo"))));
    }

    @Test
    @DisplayName("DELETE /api/proveedores/{id} devuelve 204")
    void testEliminarProveedor() throws Exception {
        mockMvc.perform(delete("/api/proveedores/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(servicioProveedor).delete(1L);
    }

    @Test
    @DisplayName("GET /api/proveedores/{id}/contactos devuelve lista de contactos")
    void testListarContactosProveedor() throws Exception {
    ContactoProveedor c = ContactoProveedor.builder().id(10L).name("Juan").phone("123").build();
        Mockito.when(servicioProveedor.listarContactos(1L)).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/proveedores/1/contactos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].name", Objects.requireNonNull(is("Juan"))));
    }

    @Test
    @DisplayName("POST /api/proveedores/{id}/contactos agrega contacto y devuelve proveedor")
    void testAgregarContactoProveedor() throws Exception {
        ContactoProveedor req = ContactoProveedor.builder().name("Juan").phone("123").email("a@b.com").role("Gerente").build();
        Proveedor p = Proveedor.builder().id(1L).companyName("ACME").contacts(new HashSet<>(Arrays.asList(req))).build();
        Mockito.when(servicioProveedor.addContact(Mockito.eq(1L), Mockito.any(ContactoProveedor.class))).thenReturn(p);

        mockMvc.perform(post("/api/proveedores/1/contactos")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(1))))
                .andExpect(jsonPath("$.contacts", Objects.requireNonNull(notNullValue())));
    }

    @Test
    @DisplayName("POST /api/proveedores/{id}/contactos devuelve 400 si el contacto es inválido")
    void testAgregarContactoProveedorPeticionInvalida() throws Exception {
        ContactoProveedor req = ContactoProveedor.builder().name("").phone("").email("").role("").build();
    Mockito.when(servicioProveedor.addContact(Mockito.eq(1L), Mockito.any(ContactoProveedor.class))).thenThrow(new PeticionInvalidaException("Contacto inválido"));

        mockMvc.perform(post("/api/proveedores/1/contactos")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isBadRequest());
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioUsuario;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControladorUsuario.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserControllerWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private ServicioUsuario servicioUsuario;

    // jwtAuthenticationFilter no es usado en este test

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RepositorioUsuario repositorioUsuario() {
            return Mockito.mock(RepositorioUsuario.class);
        }
        @Bean
        public ServicioUsuario servicioUsuario() {
            return Mockito.mock(ServicioUsuario.class);
        }
        // Beans de seguridad importados desde TestSecurityConfig
    }

    @Test
    @DisplayName("GET /api/usuarios devuelve lista de usuarios")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testList() throws Exception {
        RespuestaUsuario dto = RespuestaUsuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .rut("12345678")
                .dv("9")
                .correo("juan@example.com")
                .telefono("+56 9 1234 5678")
                .rol(Role.ADMIN)
                .direccion("Calle 1")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        Mockito.when(servicioUsuario.listar(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].correo", Objects.requireNonNull(is("juan@example.com"))))
                .andExpect(jsonPath("$[0].telefono", Objects.requireNonNull(is("+56 9 1234 5678"))))
                .andExpect(jsonPath("$[0].rol", Objects.requireNonNull(is("ADMIN"))));
    }

    @Test
    @DisplayName("GET /api/usuarios/me devuelve perfil del usuario")
    @WithMockUser(username = "client@example.com")
    void testMe() throws Exception {
        Usuario u = Usuario.builder()
                .id(1L)
                .nombres("Cliente")
                .apellidos("Demo")
                .rut("11111111")
                .dv("1")
                .correo("client@example.com")
                .telefono("+56 9 0000 0000")
                .direccion("Calle 2")
                .habilitado(true)
                .creadoEn(Instant.now())
                .build();
    Mockito.when(repositorioUsuario.findByCorreo("client@example.com")).thenReturn(Optional.of(u));

    mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo", Objects.requireNonNull(is("client@example.com"))))
                .andExpect(jsonPath("$.telefono", Objects.requireNonNull(is("+56 9 0000 0000"))));
    }
}

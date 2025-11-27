package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudAutenticacion;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudRegistro;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.security.ProveedorTokenJwt;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioUsuario;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Objects;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControladorAutenticacion.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthControllerWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ProveedorTokenJwt tokenProvider;

    @Autowired
    private ServicioUsuario userService;


    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationManager authenticationManager() {
            return Mockito.mock(AuthenticationManager.class);
        }
        @Bean
        public ServicioUsuario userService() {
            return Mockito.mock(ServicioUsuario.class);
        }

    }

    @Test
    @DisplayName("POST /api/autenticacion/login devuelve AuthResponse con token")
    void testLogin() throws Exception {
        SolicitudAutenticacion req = new SolicitudAutenticacion();
        req.setCorreo("client@example.com");
    req.setContrasena("secret");

        UserDetails principal = User.withUsername("client@example.com").password("x").roles("CLIENT").build();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
    Mockito.when(userService.existsByCorreo("client@example.com")).thenReturn(true);
    Mockito.when(tokenProvider.generarToken(Mockito.any(UserDetails.class))).thenReturn("jwt-token");

    mockMvc.perform(post("/api/autenticacion/login")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", Objects.requireNonNull(is("jwt-token"))))
                .andExpect(jsonPath("$.tokenType", Objects.requireNonNull(is("Bearer"))))
                .andExpect(jsonPath("$.expiresIn", Objects.requireNonNull(is(3600000))));
    }

    @Test
    @DisplayName("POST /api/autenticacion/register devuelve UserResponse")
    void testRegister() throws Exception {
        SolicitudRegistro req = new SolicitudRegistro();
        req.setNombres("Juan");
        req.setApellidos("Pérez");
        req.setRut("12345678");
        req.setDv("9");
    req.setCorreo("juan@gmail.com");
    req.setContrasena("pwd");
        req.setDireccion("Calle 1");
    req.setComunaId(13101);
        req.setRol(Role.CLIENT);

        RespuestaUsuario resp = RespuestaUsuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .rut("12345678")
                .dv("9")
        .correo("juan@gmail.com")
                .rol(Role.CLIENT)
                .direccion("Calle 1")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        Mockito.when(userService.registerClient(Mockito.any(SolicitudRegistro.class))).thenReturn(resp);

    mockMvc.perform(post("/api/autenticacion/register")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(1))))
        .andExpect(jsonPath("$.correo", Objects.requireNonNull(is("juan@gmail.com"))))
                .andExpect(jsonPath("$.rol", Objects.requireNonNull(is("CLIENT"))));
    }
}

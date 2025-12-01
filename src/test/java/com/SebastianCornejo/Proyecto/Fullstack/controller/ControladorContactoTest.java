package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCorreo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.SebastianCornejo.Proyecto.Fullstack.security.ProveedorTokenJwt;
import com.SebastianCornejo.Proyecto.Fullstack.security.ServicioDetallesUsuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ControladorContacto.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class ControladorContactoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioCorreo servicioCorreo;

    @MockBean
    private ProveedorTokenJwt proveedorTokenJwt;

    @MockBean
    private ServicioDetallesUsuario servicioDetallesUsuario;

    @Test
    @DisplayName("POST /api/contacto devuelve 202 y llama servicio")
    void testContacto() throws Exception {
        String body = "{\"nombre\":\"Juan\",\"correo\":\"juan@example.com\",\"mensaje\":\"hola\"}";
        mockMvc.perform(post("/api/contacto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isAccepted());
        Mockito.verify(servicioCorreo).enviarContacto("Juan", "juan@example.com", "hola");
    }
}

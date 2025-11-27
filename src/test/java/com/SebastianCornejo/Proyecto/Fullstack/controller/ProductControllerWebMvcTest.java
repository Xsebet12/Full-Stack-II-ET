package com.SebastianCornejo.Proyecto.Fullstack.controller;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProducto;
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

import java.math.BigDecimal;
import java.util.Arrays;

import java.util.Objects;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.mock.web.MockMultipartFile;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioImagenProducto;

@WebMvcTest(ControladorProducto.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ProductControllerWebMvcTest.TestConfig.class, TestSecurityConfig.class})
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioProducto productService;

    // no se usa jwtAuthenticationFilter en este test; se omite

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ServicioProducto productService() {
            return Mockito.mock(ServicioProducto.class);
        }
        @Bean
        public RepositorioImagenProducto repositorioImagenProducto() {
            return Mockito.mock(RepositorioImagenProducto.class);
        }
        // Beans de seguridad importados desde TestSecurityConfig
    }

    @Test
    @DisplayName("GET /api/productos devuelve lista")
    void testList() throws Exception {
    Producto p = Producto.builder().id(1L).nombre("Laptop").habilitado(true).build();
    Mockito.when(productService.findAllEnabled()).thenReturn(Arrays.asList(p));

    mockMvc.perform(get("/api/productos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
        .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("Laptop"))));
    }

    @Test
    @DisplayName("GET /api/productos con q filtra por nombre (lado controlador)")
    void testListWithSearchQuery() throws Exception {
        Producto p1 = Producto.builder().id(1L).nombre("Laptop").habilitado(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("Mouse").habilitado(true).build();
        Mockito.when(productService.findAllEnabled()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/productos").param("q", "lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("Laptop"))));
    }

    @Test
    @DisplayName("GET /api/productos con categoriaId filtra por categoría")
    void testListWithCategoriaFilter() throws Exception {
        Categoria c1 = Categoria.builder().id(1L).nombre("Cat1").build();
        Categoria c2 = Categoria.builder().id(2L).nombre("Cat2").build();
        Producto p1 = Producto.builder().id(1L).nombre("A").categoria(c1).habilitado(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("B").categoria(c2).habilitado(true).build();
        Mockito.when(productService.findAllEnabled()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/productos").param("categoriaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].nombre", Objects.requireNonNull(is("A"))))
                .andExpect(jsonPath("$[0].categoria.id", Objects.requireNonNull(is(1))));
    }

    @Test
    @DisplayName("GET /api/productos/stock-critico usa umbral por defecto (5)")
    void testStockCriticoDefaultUmbral() throws Exception {
        Producto p1 = Producto.builder().id(1L).nombre("A").stock(2).habilitado(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("B").stock(5).habilitado(true).build();
        Producto p3 = Producto.builder().id(3L).nombre("C").stock(7).habilitado(true).build();
        Mockito.when(productService.findAllEnabled()).thenReturn(Arrays.asList(p1, p2, p3));

        mockMvc.perform(get("/api/productos/stock-critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(1))))
                .andExpect(jsonPath("$[0].id", Objects.requireNonNull(is(1))));
    }

    @Test
    @DisplayName("GET /api/productos/stock-critico con umbral custom")
    void testStockCriticoCustomUmbral() throws Exception {
        Producto p1 = Producto.builder().id(1L).nombre("A").stock(2).habilitado(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("B").stock(5).habilitado(true).build();
        Producto p3 = Producto.builder().id(3L).nombre("C").stock(7).habilitado(true).build();
        Mockito.when(productService.findAllEnabled()).thenReturn(Arrays.asList(p1, p2, p3));

        mockMvc.perform(get("/api/productos/stock-critico").param("umbral", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Objects.requireNonNull(hasSize(2))))
                .andExpect(jsonPath("$[?(@.id==1)]").exists())
                .andExpect(jsonPath("$[?(@.id==2)]").exists());
    }

    @Test
    @DisplayName("GET /api/productos/{id} devuelve producto")
    void testGet() throws Exception {
    Producto p = Producto.builder().id(1L).nombre("Laptop").habilitado(true).build();
        Mockito.when(productService.findById(1L)).thenReturn(p);

    mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(1))))
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Laptop"))));
    }

    @Test
    @DisplayName("POST /api/productos crea y devuelve producto")
    void testCreate() throws Exception {
    Categoria c = Categoria.builder().id(1L).nombre("Electrónica").build();
    Producto req = Producto.builder().nombre("Mouse").precio(new BigDecimal("19.99")).categoria(Categoria.builder().id(1L).build()).habilitado(true).build();
    Producto created = Producto.builder().id(2L).nombre("Mouse").precio(new BigDecimal("19.99")).categoria(c).habilitado(true).build();
    Mockito.when(productService.create(Mockito.any(Producto.class))).thenReturn(created);

    mockMvc.perform(post("/api/productos")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(2))))
                .andExpect(jsonPath("$.categoria.nombre", Objects.requireNonNull(is("Electrónica"))));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} actualiza y devuelve producto")
    void testUpdate() throws Exception {
    Producto req = Producto.builder().nombre("Laptop Pro").habilitado(true).build();
    Producto updated = Producto.builder().id(1L).nombre("Laptop Pro").habilitado(true).build();
    Mockito.when(productService.update(Mockito.eq(1L), Mockito.any(Producto.class))).thenReturn(updated);

    mockMvc.perform(put("/api/productos/1")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Laptop Pro"))));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} devuelve 204")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(productService).delete(1L);
    }

    @Test
    @DisplayName("POST /api/productos/{id}/images sube múltiples imágenes y devuelve producto")
    void testSubirImagenesMultiples() throws Exception {
        // Preparar archivos simulados
        MockMultipartFile archivo1 = new MockMultipartFile("files", "img1.jpg", "image/jpeg", "datos1".getBytes());
        MockMultipartFile archivo2 = new MockMultipartFile("files", "img2.png", "image/png", "datos2".getBytes());

    Producto productoResultado = Producto.builder().id(5L).nombre("Mouse").habilitado(true).build();
        // Cada llamada a addImage devolverá el productoResultado (se sobrescribe y se devuelve el último)
        Mockito.when(productService.addImage(Mockito.eq(1L), Mockito.anyString())).thenReturn(productoResultado);

        mockMvc.perform(multipart("/api/productos/1/images")
                        .file(archivo1)
                        .file(archivo2)
                        .contentType(Objects.requireNonNull(MediaType.MULTIPART_FORM_DATA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Objects.requireNonNull(is(5))))
                .andExpect(jsonPath("$.nombre", Objects.requireNonNull(is("Mouse"))));
    }
}

package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCategoria;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private RepositorioProducto productRepository;

    @Mock
    private RepositorioCategoria categoryRepository;

    @InjectMocks
    private ServicioProductoImpl productService;

    private Categoria category;
    private Producto product;

    @BeforeEach
    void setUp() {
        category = Categoria.builder().id(1L).nombre("Electrónica").build();
        product = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Ultrabook")
                .precio(new BigDecimal("999.99"))
                .stock(5)
        .categoria(category)
        .habilitado(true)
                .build();
    }

    @Test
    @DisplayName("findAll devuelve la lista de productos")
    void testFindAll() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        List<Producto> result = productService.findAll();
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getNombre());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("findById devuelve producto existente")
    void testFindByIdFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Producto result = productService.findById(1L);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getNombre());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("findById lanza ResourceNotFoundException si no existe")
    void testFindByIdNotFound() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
    RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> productService.findById(2L));
        assertTrue(ex.getMessage().contains("2"));
        verify(productRepository).findById(2L);
    }

    @Test
    @DisplayName("create lanza BadRequest si no se indica categoría")
    void testCreateWithoutCategory() {
    Producto p = Producto.builder().nombre("Mouse").habilitado(true).build();
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> productService.create(p));
        assertTrue(ex.getMessage().contains("categoría"));
        verifyNoInteractions(categoryRepository);
        verify(productRepository, never()).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("create lanza ResourceNotFound si la categoría no existe")
    void testCreateCategoryNotFound() {
    Producto p = Producto.builder().nombre("Mouse").categoria(Categoria.builder().id(99L).build()).habilitado(true).build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
    RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> productService.create(p));
        assertTrue(ex.getMessage().contains("99"));
        verify(categoryRepository).findById(99L);
        verify(productRepository, never()).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("create guarda y devuelve el producto cuando la categoría existe")
    void testCreateSuccess() {
    Producto p = Producto.builder().nombre("Mouse").categoria(Categoria.builder().id(1L).build()).habilitado(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Producto savedMock = Producto.builder().id(2L).categoria(category).build();
        when(productRepository.save(argThat(Objects::nonNull))).thenReturn(Objects.requireNonNull(savedMock));

        Producto created = productService.create(p);
        assertEquals(2L, created.getId());
        assertEquals("Electrónica", created.getCategoria().getNombre());
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("create mapea DataIntegrityViolation a BadRequestException")
    void testCreateDataIntegrityViolation() {
    Producto p = Producto.builder().nombre("Mouse").categoria(Categoria.builder().id(1L).build()).habilitado(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(argThat(Objects::nonNull))).thenThrow(new DataIntegrityViolationException("duplicate key"));
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> productService.create(p));
        assertTrue(ex.getMessage().toLowerCase().contains("integridad"));
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("update actualiza campos y categoría cuando se envía")
    void testUpdateSuccessWithCategoryChange() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Categoria newCategory = Categoria.builder().id(2L).nombre("Accesorios").build();
    Producto updateReq = Producto.builder()
                .nombre("Laptop Pro")
                .descripcion("Ultrabook 2025")
                .precio(new BigDecimal("1299.00"))
                .stock(10)
                .categoria(Categoria.builder().id(2L).build())
        .habilitado(true)
                .build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(argThat(Objects::nonNull))).thenReturn(Objects.requireNonNull(product));

        Producto updated = productService.update(1L, updateReq);
        assertEquals("Laptop Pro", updated.getNombre());
        assertEquals("Ultrabook 2025", updated.getDescripcion());
        assertEquals(new BigDecimal("1299.00"), updated.getPrecio());
        assertEquals(10, updated.getStock());
        assertEquals("Accesorios", updated.getCategoria().getNombre());

        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verify(productRepository).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("update mapea DataIntegrityViolation a BadRequestException")
    void testUpdateDataIntegrityViolation() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    Producto updateReq = Producto.builder().nombre("X").habilitado(true).build();
        when(productRepository.save(argThat(Objects::nonNull))).thenThrow(new DataIntegrityViolationException("constraint"));
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> productService.update(1L, updateReq));
        assertTrue(ex.getMessage().toLowerCase().contains("integridad"));
        verify(productRepository).findById(1L);
        verify(productRepository).save(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("delete elimina el producto existente")
    void testDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.delete(1L);
        verify(productRepository).findById(1L);
        verify(productRepository).delete(Objects.requireNonNull(product));
    }
}

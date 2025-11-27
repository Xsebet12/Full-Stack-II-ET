package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCategoria;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private RepositorioCategoria categoryRepository;

    @Mock
    private RepositorioProducto productRepository;

    @InjectMocks
    private ServicioCategoriaImpl categoryService;

    @Test
    @DisplayName("findAll devuelve lista")
    void testFindAll() {
    Mockito.when(categoryRepository.findAll()).thenReturn(Arrays.asList(Categoria.builder().id(1L).nombre("A").build()));
    List<Categoria> result = categoryService.findAll();
    assertEquals(1, result.size());
    assertEquals("A", result.get(0).getNombre());
    }

    @Test
    @DisplayName("findById encuentra categoría")
    void testFindByIdFound() {
    Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().id(1L).nombre("A").build()));
    Categoria c = categoryService.findById(1L);
    assertEquals(1L, c.getId());
    }

    @Test
    @DisplayName("findById lanza ResourceNotFoundException")
    void testFindByIdNotFound() {
        Mockito.when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
    RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> categoryService.findById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("create lanza BadRequest si nombre existe")
    void testCreateNameExists() {
    Mockito.when(categoryRepository.findByNombreIgnoreCase("A")).thenReturn(Optional.of(Categoria.builder().id(1L).nombre("A").build()));
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> categoryService.create(Categoria.builder().nombre("A").build()));
        assertTrue(ex.getMessage().toLowerCase().contains("existe"));
    }

    @Test
    @DisplayName("create guarda y devuelve categoría")
    void testCreateSuccess() {
    Categoria toSave = Categoria.builder().nombre("B").build();
    Categoria saved = Categoria.builder().id(2L).nombre("B").build();
    Mockito.when(categoryRepository.findByNombreIgnoreCase("B")).thenReturn(Optional.empty());
    Mockito.when(categoryRepository.save(any(Categoria.class))).thenReturn(Objects.requireNonNull(saved));
    Categoria result = categoryService.create(toSave);
    assertEquals(2L, result.getId());
    }

    @Test
    @DisplayName("create lanza BadRequest por DataIntegrityViolation")
    void testCreateDataIntegrityViolation() {
    Mockito.when(categoryRepository.findByNombreIgnoreCase("C")).thenReturn(Optional.empty());
    Mockito.when(categoryRepository.save(any(Categoria.class))).thenThrow(new DataIntegrityViolationException("dup"));
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> categoryService.create(Categoria.builder().nombre("C").build()));
        assertTrue(ex.getMessage().toLowerCase().contains("integridad"));
    }

    @Test
    @DisplayName("update actualiza y devuelve categoría")
    void testUpdateSuccess() {
    Categoria existing = Categoria.builder().id(1L).nombre("Old").build();
    Categoria update = Categoria.builder().nombre("New").build();
    Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
    Mockito.when(categoryRepository.save(any(Categoria.class))).thenAnswer(inv -> Objects.requireNonNull(inv.getArgument(0)));
    Categoria result = categoryService.update(1L, update);
    assertEquals("New", result.getNombre());
    }

    @Test
    @DisplayName("update lanza BadRequest por DataIntegrityViolation")
    void testUpdateDataIntegrityViolation() {
    Categoria existing = Categoria.builder().id(1L).nombre("Old").build();
    Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
    Mockito.when(categoryRepository.save(any(Categoria.class))).thenThrow(new DataIntegrityViolationException("dup"));
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> categoryService.update(1L, Categoria.builder().nombre("X").build()));
        assertTrue(ex.getMessage().toLowerCase().contains("integridad"));
    }

    @Test
    @DisplayName("delete elimina cuando no tiene productos asociados")
    void testDeleteSuccess() {
    Categoria existing = Categoria.builder().id(1L).nombre("A").build();
    Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
    Mockito.when(productRepository.countByCategoriaId(1L)).thenReturn(0L);
        categoryService.delete(1L);
        Mockito.verify(categoryRepository).delete(Objects.requireNonNull(existing));
    }

    @Test
    @DisplayName("delete lanza BadRequest si tiene productos asociados")
    void testDeleteHasProducts() {
    Categoria existing = Categoria.builder().id(1L).nombre("A").build();
    Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
    Mockito.when(productRepository.countByCategoriaId(1L)).thenReturn(2L);
    PeticionInvalidaException ex = assertThrows(PeticionInvalidaException.class, () -> categoryService.delete(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("productos asociados"));
    }
}

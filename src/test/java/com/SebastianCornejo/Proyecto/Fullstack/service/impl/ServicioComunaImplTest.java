package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioComuna;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioComunaImplTest {

    @Mock
    private RepositorioComuna repositorioComuna;

    @InjectMocks
    private ServicioComunaImpl servicioComuna;

    private Comuna c1;

    @BeforeEach
    void setUp() {
    c1 = Comuna.builder().idComuna(1).nomComuna("Santiago").build();
    }

    @Test
    @DisplayName("findAll devuelve lista")
    void testFindAll() {
        when(repositorioComuna.findAll()).thenReturn(Arrays.asList(c1));
        var lista = servicioComuna.findAll();
        assertEquals(1, lista.size());
        verify(repositorioComuna).findAll();
    }

    @Test
    @DisplayName("findById devuelve existente y lanza si no existe")
    void testFindById() {
        when(repositorioComuna.findById(1)).thenReturn(Optional.of(c1));
        Comuna encontrada = servicioComuna.findById(1);
        assertEquals("Santiago", encontrada.getNomComuna());

        when(repositorioComuna.findById(2)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicioComuna.findById(2));
    }

    @Test
    @DisplayName("create guarda y evita duplicados")
    void testCreate() {
        Comuna nueva = Comuna.builder().nomComuna("Valparaiso").build();
        when(repositorioComuna.findByNomComunaIgnoreCase("Valparaiso")).thenReturn(Optional.empty());
        when(repositorioComuna.save(any(Comuna.class))).thenAnswer(inv -> {
            Comuna arg = Objects.requireNonNull(inv.getArgument(0));
            arg.setIdComuna(2);
            return Objects.requireNonNull(arg);
        });
        Comuna creado = servicioComuna.create(nueva);
        assertEquals(2, creado.getIdComuna());

        // duplicado
        when(repositorioComuna.findByNomComunaIgnoreCase("Valparaiso")).thenReturn(Optional.of(creado));
        assertThrows(PeticionInvalidaException.class, () -> servicioComuna.create(nueva));
    }

    @Test
    @DisplayName("update y delete")
    void testUpdateDelete() {
        when(repositorioComuna.findById(1)).thenReturn(Optional.of(c1));
        Comuna req = Comuna.builder().nomComuna("Santiago Centro").build();
        when(repositorioComuna.save(any(Comuna.class))).thenAnswer(inv -> Objects.requireNonNull(inv.getArgument(0)));
        Comuna updated = servicioComuna.update(1, req);
        assertEquals("Santiago Centro", updated.getNomComuna());

        // delete
        servicioComuna.delete(1);
        verify(repositorioComuna).delete(any(Comuna.class));
    }
}

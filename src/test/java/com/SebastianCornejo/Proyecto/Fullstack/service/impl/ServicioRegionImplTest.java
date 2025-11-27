package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioRegion;
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
class ServicioRegionImplTest {

    @Mock
    private RepositorioRegion repositorioRegion;

    @InjectMocks
    private ServicioRegionImpl servicioRegion;

    private Region r1;

    @BeforeEach
    void setUp() {
        r1 = Region.builder().idRegion(1).nomRegion("Metropolitana").build();
    }

    @Test
    @DisplayName("findAll devuelve lista")
    void testFindAll() {
        when(repositorioRegion.findAll()).thenReturn(Arrays.asList(r1));
        var lista = servicioRegion.findAll();
        assertEquals(1, lista.size());
        verify(repositorioRegion).findAll();
    }

    @Test
    @DisplayName("findById devuelve existente y lanza si no existe")
    void testFindById() {
        when(repositorioRegion.findById(1)).thenReturn(Optional.of(r1));
        Region encontrada = servicioRegion.findById(1);
        assertEquals("Metropolitana", encontrada.getNomRegion());

        when(repositorioRegion.findById(2)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicioRegion.findById(2));
    }

    @Test
    @DisplayName("create guarda y evita duplicados")
    void testCreate() {
        Region nueva = Region.builder().nomRegion("Biobio").build();
        when(repositorioRegion.findByNomRegionIgnoreCase("Biobio")).thenReturn(Optional.empty());
        when(repositorioRegion.save(any(Region.class))).thenAnswer(inv -> {
            Region arg = Objects.requireNonNull(inv.getArgument(0));
            arg.setIdRegion(2);
            return Objects.requireNonNull(arg);
        });
        Region creado = servicioRegion.create(nueva);
        assertEquals(2, creado.getIdRegion());

        // duplicado
        when(repositorioRegion.findByNomRegionIgnoreCase("Biobio")).thenReturn(Optional.of(creado));
        assertThrows(PeticionInvalidaException.class, () -> servicioRegion.create(nueva));
    }

    @Test
    @DisplayName("update y delete")
    void testUpdateDelete() {
        when(repositorioRegion.findById(1)).thenReturn(Optional.of(r1));
        Region req = Region.builder().nomRegion("Metropolitana Centro").build();
        when(repositorioRegion.save(any(Region.class))).thenAnswer(inv -> Objects.requireNonNull(inv.getArgument(0)));
        Region updated = servicioRegion.update(1, req);
        assertEquals("Metropolitana Centro", updated.getNomRegion());

        // delete
        servicioRegion.delete(1);
        verify(repositorioRegion).delete(any(Region.class));
    }
}

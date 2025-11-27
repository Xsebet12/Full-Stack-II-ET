package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioComuna;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioComuna;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ServicioComunaImpl implements ServicioComuna {

    private final RepositorioComuna repositorioComuna;

    public ServicioComunaImpl(RepositorioComuna repositorioComuna) {
        this.repositorioComuna = repositorioComuna;
    }

    @Override
    public List<Comuna> findAll() {
        return repositorioComuna.findAll();
    }

    @Override
    public Comuna findById(Integer id) {
        return repositorioComuna.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("Comuna no encontrada con id " + id));
    }

    @Override
    public Comuna create(Comuna comuna) {
        repositorioComuna.findByNomComunaIgnoreCase(comuna.getNomComuna())
                .ifPresent(c -> { throw new PeticionInvalidaException("Ya existe una comuna con ese nombre"); });
        try {
            return repositorioComuna.save(comuna);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al crear comuna: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Comuna update(Integer id, Comuna comuna) {
        Comuna existing = findById(id);
        existing.setNomComuna(comuna.getNomComuna());
        try {
            return Objects.requireNonNull(repositorioComuna.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar comuna: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        Comuna existing = findById(id);
        repositorioComuna.delete(Objects.requireNonNull(existing));
    }
}

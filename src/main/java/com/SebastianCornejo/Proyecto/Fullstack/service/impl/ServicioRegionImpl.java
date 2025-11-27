package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioRegion;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioRegion;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.Objects;

import java.util.List;

@Service
public class ServicioRegionImpl implements ServicioRegion {

    private final RepositorioRegion repositorioRegion;

    public ServicioRegionImpl(RepositorioRegion repositorioRegion) {
        this.repositorioRegion = repositorioRegion;
    }

    @Override
    public List<Region> findAll() {
        return repositorioRegion.findAll();
    }

    @Override
    public Region findById(Integer id) {
        return repositorioRegion.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("Región no encontrada con id " + id));
    }

    @Override
    public Region create(Region region) {
        repositorioRegion.findByNomRegionIgnoreCase(region.getNomRegion())
                .ifPresent(r -> { throw new PeticionInvalidaException("Ya existe una región con ese nombre"); });
        try {
            return repositorioRegion.save(region);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al crear región: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Region update(Integer id, Region region) {
        Region existing = findById(id);
        existing.setNomRegion(region.getNomRegion());
        try {
            return Objects.requireNonNull(repositorioRegion.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar región: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        Region existing = findById(Objects.requireNonNull(id));
        repositorioRegion.delete(Objects.requireNonNull(existing));
    }
}

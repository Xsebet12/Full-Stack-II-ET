package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCategoria;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCategoria;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ServicioCategoriaImpl implements ServicioCategoria {

    private final RepositorioCategoria repositorioCategoria;
    private final RepositorioProducto repositorioProducto;

    public ServicioCategoriaImpl(RepositorioCategoria repositorioCategoria, RepositorioProducto repositorioProducto) {
        this.repositorioCategoria = repositorioCategoria;
        this.repositorioProducto = repositorioProducto;
    }

    @Override
    public List<Categoria> findAll() {
        return repositorioCategoria.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return repositorioCategoria.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + id));
    }

    @Override
    public Categoria create(Categoria categoria) {
    repositorioCategoria.findByNombreIgnoreCase(categoria.getNombre())
        .ifPresent(c -> { throw new PeticionInvalidaException("Ya existe una categoría con ese nombre"); });
        try {
            return repositorioCategoria.save(categoria);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al crear categoría: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Categoria update(Long id, Categoria categoria) {
        Categoria existing = findById(id);
        existing.setNombre(categoria.getNombre());
        try {
            return repositorioCategoria.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar categoría: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        Categoria existing = findById(id);
    long count = repositorioProducto.countByCategoriaId(id);
        if (count > 0) {
            throw new PeticionInvalidaException("No se puede eliminar la categoría porque tiene productos asociados");
        }
        repositorioCategoria.delete(Objects.requireNonNull(existing));
    }
}

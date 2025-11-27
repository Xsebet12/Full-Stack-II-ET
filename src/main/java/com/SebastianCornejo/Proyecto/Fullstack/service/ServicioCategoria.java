package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;

import java.util.List;

public interface ServicioCategoria {
    List<Categoria> findAll();
    Categoria findById(Long id);
    Categoria create(Categoria category);
    Categoria update(Long id, Categoria category);
    void delete(Long id);
}

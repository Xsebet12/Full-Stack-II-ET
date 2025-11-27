package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import java.util.List;

public interface ServicioComuna {
    List<Comuna> findAll();
    Comuna findById(Integer id);
    Comuna create(Comuna comuna);
    Comuna update(Integer id, Comuna comuna);
    void delete(Integer id);
}

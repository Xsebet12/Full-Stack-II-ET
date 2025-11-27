package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import java.util.List;

public interface ServicioRegion {
    List<Region> findAll();
    Region findById(Integer id);
    Region create(Region region);
    Region update(Integer id, Region region);
    void delete(Integer id);
}

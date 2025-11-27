package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RepositorioRegion extends JpaRepository<Region, Integer> {
    Optional<Region> findByNomRegionIgnoreCase(String nomRegion);
}

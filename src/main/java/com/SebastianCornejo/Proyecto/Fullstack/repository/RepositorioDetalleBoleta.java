package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDetalleBoleta extends JpaRepository<DetalleBoleta, Long> {
    List<DetalleBoleta> findByBoletaId(Long boletaId);
}


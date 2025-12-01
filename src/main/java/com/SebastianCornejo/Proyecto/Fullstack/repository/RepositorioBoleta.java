package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RepositorioBoleta extends JpaRepository<Boleta, Long> {
    Optional<Boleta> findTopByOrderByNumeroDesc();
    Optional<Boleta> findByVentaId(Long ventaId);
    List<Boleta> findByVentaUsuarioId(Long usuarioId);
}


package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioDetalleVenta extends JpaRepository<DetalleVenta, Long> {
    java.util.List<DetalleVenta> findByVentaId(Long ventaId);
}

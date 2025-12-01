package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.SeguimientoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioSeguimientoPedido extends JpaRepository<SeguimientoPedido, Long> {
    java.util.Optional<SeguimientoPedido> findFirstByVentaIdAndEstadoEnvio(Long ventaId, String estadoEnvio);
    java.util.Optional<SeguimientoPedido> findFirstByVentaId(Long ventaId);
}

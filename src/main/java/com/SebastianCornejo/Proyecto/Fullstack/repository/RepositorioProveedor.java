package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.EstadoProveedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioProveedor extends JpaRepository<Proveedor, Long> {
    boolean existsByCompanyNameIgnoreCase(String companyName);
    List<Proveedor> findByEstado(EstadoProveedor estado);
}

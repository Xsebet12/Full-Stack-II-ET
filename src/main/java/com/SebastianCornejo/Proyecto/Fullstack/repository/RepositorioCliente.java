package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {
    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Cliente> findAll();

    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Cliente> findByHabilitado(Boolean habilitado);
}


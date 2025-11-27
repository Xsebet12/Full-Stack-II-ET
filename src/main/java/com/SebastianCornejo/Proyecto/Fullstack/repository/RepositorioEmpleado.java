package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioEmpleado extends JpaRepository<Empleado, Long> {
    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Empleado> findAll();

    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Empleado> findByHabilitado(Boolean habilitado);

    boolean existsByCelular(String celular);
    java.util.Optional<Empleado> findByCelular(String celular);
}

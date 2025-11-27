package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByRut(String rut);
    boolean existsByCorreo(String correo);
    boolean existsByRut(String rut);
    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Usuario> findByHabilitado(Boolean habilitado);
    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<Usuario> findAll();
}

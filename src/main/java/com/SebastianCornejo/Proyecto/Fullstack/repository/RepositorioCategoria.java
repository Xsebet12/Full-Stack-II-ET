package com.SebastianCornejo.Proyecto.Fullstack.repository;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RepositorioCategoria extends JpaRepository<Categoria, Long> {
	Optional<Categoria> findByNombreIgnoreCase(String nombre);
}

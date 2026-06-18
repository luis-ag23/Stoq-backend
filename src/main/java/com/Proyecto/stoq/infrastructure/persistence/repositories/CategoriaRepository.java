package com.Proyecto.stoq.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import com.Proyecto.stoq.domain.model.Categoria;


@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    Optional<Categoria> findByNombre(String nombre);
}

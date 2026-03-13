package com.Proyecto.stoq.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.stoq.domain.model.Rol;

import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(String nombre);
}
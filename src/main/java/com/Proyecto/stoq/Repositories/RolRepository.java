package com.Proyecto.stoq.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.stoq.Entities.Rol;

import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(String nombre);
}
package com.Proyecto.stoq.infrastructure.persistence.repositories;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Proyecto.stoq.domain.model.Unidad;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, UUID> {
    List<Unidad> findByEstadoTrue();
    Optional<Unidad> findByNombre(String nombre);

}

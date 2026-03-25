package com.Proyecto.stoq.domain.ports;
import com.Proyecto.stoq.domain.model.Unidad;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnidadRepositoryPort {
    List<Unidad> findAll();

    List<Unidad> findByEstadoTrue();

    Optional<Unidad> findById(UUID id);

    Optional<Unidad> findByNombre(String nombre);

    Unidad save(Unidad unidad);
}

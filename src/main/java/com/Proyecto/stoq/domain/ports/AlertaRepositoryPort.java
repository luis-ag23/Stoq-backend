package com.Proyecto.stoq.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Alerta;

public interface AlertaRepositoryPort {

    Alerta save(Alerta alerta);

    List<Alerta> findAll();

    Optional<Alerta> findById(UUID id);

    boolean existsByProductoIdAndTipoAndLeidaFalse(UUID productoId, String tipo);

    long countByLeidaFalse();
}
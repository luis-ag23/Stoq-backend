package com.Proyecto.stoq.application.services;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.Proyecto.stoq.dto.CreateUnidadDTO;
import com.Proyecto.stoq.domain.model.Unidad;

public interface UnidadService {
    List<Unidad> obtenerUnidades();

    Optional<Unidad> obtenerUnidadPorId(UUID id);

    Unidad crearUnidad(CreateUnidadDTO dto);

    Unidad actualizarUnidad(UUID id, CreateUnidadDTO dto);

    void eliminarUnidad(UUID id);
}

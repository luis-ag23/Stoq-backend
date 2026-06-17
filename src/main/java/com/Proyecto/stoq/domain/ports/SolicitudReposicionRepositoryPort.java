package com.Proyecto.stoq.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;
import com.Proyecto.stoq.domain.model.EstadoSolicitud;

public interface SolicitudReposicionRepositoryPort {
    List<SolicitudReposicion> findAll();
    Optional<SolicitudReposicion> findById(UUID id);
    List<SolicitudReposicion> findByEmpresa(String empresa);
    Optional<SolicitudReposicion> findByProductoIdAndEstado(UUID productoId, EstadoSolicitud estado);
    SolicitudReposicion save(SolicitudReposicion solicitud);
    void deleteById(UUID id);
}

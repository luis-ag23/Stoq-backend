package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;
import com.Proyecto.stoq.domain.model.EstadoSolicitud;

public interface SolicitudReposicionRepository extends JpaRepository<SolicitudReposicion, UUID> {
    List<SolicitudReposicion> findByEmpresaOrderByFechaSolicitudDesc(String empresa);
    Optional<SolicitudReposicion> findByProductoIdAndEstado(UUID productoId, EstadoSolicitud estado);
}

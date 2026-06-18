package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;

public record SolicitudReposicionResponseDTO(
        UUID id,
        UUID productoId,
        String productoNombre,
        String productoCodigo,
        Integer cantidadRecomendada,
        String prioridad,
        String estado,
        Double consumoPromedioDiario,
        Integer tiempoAgotamiento,
        String rotacion,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaActualizacion
) {
    public static SolicitudReposicionResponseDTO fromEntity(SolicitudReposicion entity) {
        return new SolicitudReposicionResponseDTO(
                entity.getId(),
                entity.getProducto().getId(),
                entity.getProducto().getNombre(),
                entity.getProducto().getCodigo(),
                entity.getCantidadRecomendada(),
                entity.getPrioridad().name(),
                entity.getEstado().name(),
                entity.getConsumoPromedioDiario(),
                entity.getTiempoAgotamiento(),
                entity.getRotacion().name(),
                entity.getFechaSolicitud(),
                entity.getFechaActualizacion()
        );
    }
}

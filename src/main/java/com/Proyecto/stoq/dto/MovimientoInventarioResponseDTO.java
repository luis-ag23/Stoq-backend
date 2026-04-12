package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.security.RoleCatalog;

public record MovimientoInventarioResponseDTO(
        UUID id,
        UUID productoId,
        String productoCodigo,
        String productoNombre,
        String usuarioNombre,
        String usuarioCorreo,
        String tipoMovimiento,
        Integer cantidad,
        String motivo,
        LocalDateTime fechaMovimiento,
        Integer stockAnterior,
        Integer stockResultante
) {
    public static MovimientoInventarioResponseDTO fromEntity(Movimiento_Inventario movimiento) {
        return new MovimientoInventarioResponseDTO(
                movimiento.getId(),
                movimiento.getProducto() != null ? movimiento.getProducto().getId() : null,
                movimiento.getProducto() != null ? movimiento.getProducto().getCodigo() : null,
                movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null,
                movimiento.getUsuario() != null ? movimiento.getUsuario().getNombre() : null,
                movimiento.getUsuario() != null ? movimiento.getUsuario().getCorreo() : null,
                RoleCatalog.normalize(movimiento.getTipoMovimiento()),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                movimiento.getFechaMovimiento(),
                movimiento.getStockAnterior(),
                movimiento.getStockResultante()
        );
    }
}
package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Alerta;

public record AlertaResponseDTO(
        UUID id,
        String tipo,
        String mensaje,
        LocalDateTime fecha,
        Boolean leida,
        UUID productoId,
        String productoCodigo,
        String productoNombre,
        String categoriaNombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer diferencia
) {
    public static AlertaResponseDTO fromEntity(Alerta alerta) {
        var producto = alerta.getProducto();

        return new AlertaResponseDTO(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getMensaje(),
                alerta.getFecha(),
                alerta.getLeida(),
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getStockActual() - producto.getStockMinimo()
        );
    }
}
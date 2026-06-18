package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;

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
        if (alerta == null) {
            return new AlertaResponseDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }

        Producto producto = alerta.getProducto();
        Integer stockActual = producto != null ? producto.getStockActual() : null;
        Integer stockMinimo = producto != null ? producto.getStockMinimo() : null;
        Integer diferencia = (stockActual != null && stockMinimo != null)
            ? stockActual - stockMinimo
            : null;

        return new AlertaResponseDTO(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getMensaje(),
                alerta.getFecha(),
                alerta.getLeida(),
            producto != null ? producto.getId() : null,
            producto != null ? producto.getCodigo() : null,
            producto != null ? producto.getNombre() : null,
            producto != null && producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
            stockActual,
            stockMinimo,
            diferencia
        );
    }
}
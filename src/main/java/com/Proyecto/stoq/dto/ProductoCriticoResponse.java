package com.Proyecto.stoq.dto;

import java.util.UUID;

import com.Proyecto.stoq.domain.model.Producto;

public record ProductoCriticoResponse(
        UUID id,
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer diferencia,
        String nivelAlerta,
        String categoriaNombre,
        String unidadAbreviatura
) {
    public static ProductoCriticoResponse fromEntity(Producto producto) {
        Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;
        Integer diferencia = stockActual - stockMinimo;
        String nivelAlerta = stockActual == 0 ? "CRITICO" : (stockActual <= stockMinimo ? "BAJO" : "NORMAL");

        return new ProductoCriticoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                stockActual,
                stockMinimo,
                diferencia,
                nivelAlerta,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getUnidad() != null ? producto.getUnidad().getAbreviatura() : null
        );
    }
}
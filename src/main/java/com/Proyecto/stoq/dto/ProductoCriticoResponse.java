package com.Proyecto.stoq.dto;

import java.util.UUID;

import com.Proyecto.stoq.domain.model.ProductoCriticoView;

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
    public static ProductoCriticoResponse fromEntity(ProductoCriticoView producto) {
        return new ProductoCriticoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getDiferencia(),
                producto.getNivelAlerta(),
                producto.getCategoriaNombre(),
                producto.getUnidadAbreviatura()
        );
    }
}
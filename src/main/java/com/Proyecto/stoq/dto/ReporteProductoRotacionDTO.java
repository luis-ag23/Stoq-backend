package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReporteProductoRotacionDTO(
        UUID productoId,
        String codigo,
        String nombre,
        Long salidasMovimientos,
        Long salidasCantidad,
        LocalDateTime ultimaSalida,
        String categoriaNombre,
        String unidadAbreviatura
) {
}
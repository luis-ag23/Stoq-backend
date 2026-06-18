package com.Proyecto.stoq.dto;

import java.util.UUID;

public record ReporteCategoriaResumenDTO(
        UUID categoriaId,
        String categoriaNombre,
        Long productosActivos,
        Long stockActualTotal,
        Long stockMinimoTotal,
        Long movimientosTotales,
        Long cantidadMovidaTotal,
        Long entradasMovimientos,
        Long salidasMovimientos,
        Long entradasCantidad,
        Long salidasCantidad
) {
}
package com.Proyecto.stoq.dto;

public record ReporteMovimientoTotalesDTO(
        Long entradasMovimientos,
        Long salidasMovimientos,
        Long entradasCantidad,
        Long salidasCantidad
) {
}

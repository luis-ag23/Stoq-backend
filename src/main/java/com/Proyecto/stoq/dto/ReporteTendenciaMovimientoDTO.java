package com.Proyecto.stoq.dto;

import java.time.LocalDate;

public record ReporteTendenciaMovimientoDTO(
        LocalDate fecha,
        Long entradasMovimientos,
        Long salidasMovimientos,
        Long entradasCantidad,
        Long salidasCantidad,
        Long saldoNeto
) {
}
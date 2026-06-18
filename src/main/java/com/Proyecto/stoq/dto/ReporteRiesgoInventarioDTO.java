package com.Proyecto.stoq.dto;

import java.util.UUID;

public record ReporteRiesgoInventarioDTO(
        UUID productoId,
        String codigo,
        String nombre,
        String tipo,
        String severidad,
        String detalle,
        Integer stockActual,
        Integer stockMinimo,
        Long ventanaDias,
        Long consumoReferencia
) {
}
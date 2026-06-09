package com.Proyecto.stoq.dto;

import java.util.UUID;

public record RecomendacionAutomaticaDTO(
        UUID productoId,
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Double consumoPromedioDiario,
        Integer tiempoAgotamiento,
        String rotacion,
        Integer cantidadRecomendada,
        String prioridad
) {
}

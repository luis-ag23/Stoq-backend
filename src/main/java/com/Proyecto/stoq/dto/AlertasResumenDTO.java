package com.Proyecto.stoq.dto;

public record AlertasResumenDTO(
        long productosCriticos,
        long notificacionesSinLeer,
        long totalAlertas
) {
}
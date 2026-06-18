package com.Proyecto.stoq.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteCategoriasResponseDTO(
        LocalDate inicio,
        LocalDate fin,
        String empresa,
        Long totalCategorias,
        Long productosActivos,
        Long stockActualTotal,
        Long movimientosTotales,
        Long cantidadMovidaTotal,
        List<ReporteCategoriaResumenDTO> categorias
) {
}
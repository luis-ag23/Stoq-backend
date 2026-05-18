package com.Proyecto.stoq.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteDashboardResponseDTO(
        LocalDate inicio,
        LocalDate fin,
        String empresa,
        Long totalProductos,
        Long productosBajoStock,
        Long totalCategorias,
        Long movimientosTotales,
        Long cantidadMovidaTotal,
        Long entradasMovimientos,
        Long salidasMovimientos,
        Long entradasCantidad,
        Long salidasCantidad,
        List<ReporteCategoriaResumenDTO> categorias,
        List<MovimientoInventarioResponseDTO> movimientosRecientes
) {
}

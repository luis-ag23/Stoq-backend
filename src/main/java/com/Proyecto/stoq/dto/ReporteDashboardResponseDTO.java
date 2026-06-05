package com.Proyecto.stoq.dto;

import java.time.LocalDate;
import java.util.List;

import com.Proyecto.stoq.dto.MovimientoInventarioResponseDTO;
import com.Proyecto.stoq.dto.ProductoCriticoResponse;
import com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO;
import com.Proyecto.stoq.dto.ReporteProductoRotacionDTO;
import com.Proyecto.stoq.dto.ReporteTendenciaMovimientoDTO;

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
        List<MovimientoInventarioResponseDTO> movimientosRecientes,
        List<ProductoCriticoResponse> productosCriticos,
        List<ReporteProductoRotacionDTO> productosMayorRotacion,
        List<ReporteTendenciaMovimientoDTO> tendencias
) {
}

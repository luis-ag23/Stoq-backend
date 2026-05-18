package com.Proyecto.stoq.application.services;

import java.time.LocalDate;

import com.Proyecto.stoq.dto.ReporteCategoriasResponseDTO;
import com.Proyecto.stoq.dto.ReporteDashboardResponseDTO;

public interface ReporteService {

    ReporteCategoriasResponseDTO obtenerReportePorCategoria(LocalDate inicio, LocalDate fin);

    ReporteDashboardResponseDTO obtenerDashboard(LocalDate inicio, LocalDate fin);

    byte[] exportarReportePdf(LocalDate inicio, LocalDate fin);

    byte[] exportarReporteExcel(LocalDate inicio, LocalDate fin);
}
package com.Proyecto.stoq.adapters.controllers;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 

import com.Proyecto.stoq.application.services.ReporteService;
import com.Proyecto.stoq.dto.ReporteCategoriasResponseDTO;
import com.Proyecto.stoq.dto.ReporteDashboardResponseDTO;

@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    private final ReporteService reporteService;

    public ReportesController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/categorias")
    public ReporteCategoriasResponseDTO obtenerReporteCategorias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return reporteService.obtenerReportePorCategoria(inicio, fin);
    }

        @GetMapping("/estadisticas")
        public ReporteDashboardResponseDTO obtenerEstadisticas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
        ) {
        return reporteService.obtenerDashboard(inicio, fin);
        }

        @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
        ) {
        byte[] contenido = reporteService.exportarReportePdf(inicio, fin);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("reporte-estadisticas.pdf").build().toString())
            .contentType(MediaType.APPLICATION_PDF)
            .body(contenido);
        }

        @GetMapping(value = "/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
        ) {
        byte[] contenido = reporteService.exportarReporteExcel(inicio, fin);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("reporte-estadisticas.xlsx").build().toString())
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(contenido);
        }
}
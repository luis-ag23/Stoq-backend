package com.Proyecto.stoq.application.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.MovimientoInventarioResponseDTO;
import com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO;
import com.Proyecto.stoq.dto.ReporteCategoriasResponseDTO;
import com.Proyecto.stoq.dto.ReporteDashboardResponseDTO;
import com.Proyecto.stoq.dto.ReporteMovimientoTotalesDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;
import com.Proyecto.stoq.infrastructure.persistence.repositories.ProductosRepository;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final ProductosRepository productosRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ReporteServiceImpl(
            ProductosRepository productosRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            UsuarioRepositoryPort usuarioRepository
    ) {
        this.productosRepository = productosRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteCategoriasResponseDTO obtenerReportePorCategoria(LocalDate inicio, LocalDate fin) {
        LocalDate fechaFin = fin != null ? fin : LocalDate.now();
        LocalDate fechaInicio = inicio != null ? inicio : fechaFin.minusDays(29);

        if (fechaInicio.isAfter(fechaFin)) {
            LocalDate temporal = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temporal;
        }

        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null || empresa.isBlank()) {
            return new ReporteCategoriasResponseDTO(fechaInicio, fechaFin, null, 0L, 0L, 0L, 0L, 0L, List.of());
        }

        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);

        List<ReporteCategoriaResumenDTO> resumenProductos = productosRepository.obtenerResumenCategorias(empresa);
        List<ReporteCategoriaResumenDTO> resumenMovimientos = movimientoInventarioRepository.obtenerResumenMovimientosPorCategoria(
                empresa,
                inicioDateTime,
                finDateTime
        );

        Map<UUID, CategoriaReporteBuilder> resumenPorCategoria = new LinkedHashMap<>();

        for (ReporteCategoriaResumenDTO item : resumenProductos) {
            if (item.categoriaId() == null) {
                continue;
            }

            resumenPorCategoria.put(item.categoriaId(), CategoriaReporteBuilder.fromProductos(item));
        }

        for (ReporteCategoriaResumenDTO item : resumenMovimientos) {
            if (item.categoriaId() == null) {
                continue;
            }

            CategoriaReporteBuilder builder = resumenPorCategoria.computeIfAbsent(
                    item.categoriaId(),
                    categoriaId -> new CategoriaReporteBuilder(item.categoriaId(), item.categoriaNombre())
            );
            builder.mergeMovimientos(item);
        }

        List<ReporteCategoriaResumenDTO> categorias = resumenPorCategoria.values().stream()
                .map(CategoriaReporteBuilder::toDto)
                .toList();

        long totalCategorias = categorias.size();
        long productosActivos = categorias.stream().mapToLong(item -> safeLong(item.productosActivos())).sum();
        long stockActualTotal = categorias.stream().mapToLong(item -> safeLong(item.stockActualTotal())).sum();
        long movimientosTotales = categorias.stream().mapToLong(item -> safeLong(item.movimientosTotales())).sum();
        long cantidadMovidaTotal = categorias.stream().mapToLong(item -> safeLong(item.cantidadMovidaTotal())).sum();

        return new ReporteCategoriasResponseDTO(
                fechaInicio,
                fechaFin,
                empresa,
                totalCategorias,
                productosActivos,
                stockActualTotal,
                movimientosTotales,
                cantidadMovidaTotal,
                categorias
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteDashboardResponseDTO obtenerDashboard(LocalDate inicio, LocalDate fin) {
        ReporteContexto contexto = construirContexto(inicio, fin);
        if (!contexto.tieneEmpresa()) {
            return new ReporteDashboardResponseDTO(
                    contexto.fechaInicio(),
                    contexto.fechaFin(),
                    null,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    List.of(),
                    List.of()
            );
        }

        ResumenCategoriaResultado resultado = construirResumenCategorias(contexto);
        ReporteMovimientoTotalesDTO totalesMovimientos = movimientoInventarioRepository.obtenerTotalesMovimientosPorEmpresa(
                contexto.empresa(),
                contexto.inicioDateTime(),
                contexto.finDateTime()
        );

        List<MovimientoInventarioResponseDTO> recientes = movimientoInventarioRepository.findRecentByEmpresaAndFechaMovimientoBetween(
                contexto.empresa(),
                contexto.inicioDateTime(),
                contexto.finDateTime(),
                PageRequest.of(0, 10)
        ).stream()
                .map(MovimientoInventarioResponseDTO::fromEntity)
                .toList();

        long entradasMovimientos = safeLong(totalesMovimientos.entradasMovimientos());
        long salidasMovimientos = safeLong(totalesMovimientos.salidasMovimientos());
        long entradasCantidad = safeLong(totalesMovimientos.entradasCantidad());
        long salidasCantidad = safeLong(totalesMovimientos.salidasCantidad());

        return new ReporteDashboardResponseDTO(
                contexto.fechaInicio(),
                contexto.fechaFin(),
                contexto.empresa(),
                productosRepository.countByEmpresaAndEstadoTrue(contexto.empresa()),
                productosRepository.contarProductosBajoStock(contexto.empresa()),
                resultado.totalCategorias(),
                entradasMovimientos + salidasMovimientos,
                entradasCantidad + salidasCantidad,
                entradasMovimientos,
                salidasMovimientos,
                entradasCantidad,
                salidasCantidad,
                resultado.categoriasOrdenadas(),
                recientes
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarReportePdf(LocalDate inicio, LocalDate fin) {
        ReporteDashboardResponseDTO dashboard = obtenerDashboard(inicio, fin);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Reporte estadistico");
                contentStream.newLineAtOffset(0, -22);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Rango: " + dashboard.inicio() + " al " + dashboard.fin());
                contentStream.newLineAtOffset(0, -16);
                contentStream.showText("Empresa: " + valorOpcion(dashboard.empresa()));
                contentStream.newLineAtOffset(0, -16);
                contentStream.showText("Total productos: " + dashboard.totalProductos());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Productos bajo stock: " + dashboard.productosBajoStock());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Movimientos totales: " + dashboard.movimientosTotales());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Cantidad movida: " + dashboard.cantidadMovidaTotal());
                contentStream.newLineAtOffset(0, -22);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("Top categorias por movimientos");
                contentStream.newLineAtOffset(0, -16);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

                for (ReporteCategoriaResumenDTO categoria : dashboard.categorias().stream().limit(8).toList()) {
                    contentStream.showText(String.format("- %s | movimientos: %d | cantidad: %d",
                            valorOpcion(categoria.categoriaNombre()),
                            safeLong(categoria.movimientosTotales()),
                            safeLong(categoria.cantidadMovidaTotal())));
                    contentStream.newLineAtOffset(0, -12);
                }

                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF del reporte", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarReporteExcel(LocalDate inicio, LocalDate fin) {
        ReporteDashboardResponseDTO dashboard = obtenerDashboard(inicio, fin);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle encabezado = crearEstiloEncabezado(workbook);
            crearHojaResumen(workbook, dashboard, encabezado);
            crearHojaCategorias(workbook, dashboard, encabezado);
            crearHojaMovimientos(workbook, dashboard, encabezado);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el Excel del reporte", exception);
        }
    }

    private String obtenerEmpresaAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }

        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName()).orElse(null);
        if (usuario == null || usuario.getEmpresa() == null) {
            return null;
        }

        String empresa = usuario.getEmpresa().trim();
        return empresa.isBlank() ? null : empresa;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    private ReporteContexto construirContexto(LocalDate inicio, LocalDate fin) {
        LocalDate fechaFin = fin != null ? fin : LocalDate.now();
        LocalDate fechaInicio = inicio != null ? inicio : fechaFin.minusDays(29);

        if (fechaInicio.isAfter(fechaFin)) {
            LocalDate temporal = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temporal;
        }

        return new ReporteContexto(fechaInicio, fechaFin, obtenerEmpresaAutenticada());
    }

    private ResumenCategoriaResultado construirResumenCategorias(ReporteContexto contexto) {
        LocalDateTime inicioDateTime = contexto.inicioDateTime();
        LocalDateTime finDateTime = contexto.finDateTime();

        List<ReporteCategoriaResumenDTO> resumenProductos = productosRepository.obtenerResumenCategorias(contexto.empresa());
        List<ReporteCategoriaResumenDTO> resumenMovimientos = movimientoInventarioRepository.obtenerResumenMovimientosPorCategoria(
                contexto.empresa(),
                inicioDateTime,
                finDateTime
        );

        Map<UUID, CategoriaReporteBuilder> resumenPorCategoria = new LinkedHashMap<>();

        for (ReporteCategoriaResumenDTO item : resumenProductos) {
            if (item.categoriaId() == null) {
                continue;
            }

            resumenPorCategoria.put(item.categoriaId(), CategoriaReporteBuilder.fromProductos(item));
        }

        for (ReporteCategoriaResumenDTO item : resumenMovimientos) {
            if (item.categoriaId() == null) {
                continue;
            }

            CategoriaReporteBuilder builder = resumenPorCategoria.computeIfAbsent(
                    item.categoriaId(),
                    categoriaId -> new CategoriaReporteBuilder(item.categoriaId(), item.categoriaNombre())
            );
            builder.mergeMovimientos(item);
        }

        List<ReporteCategoriaResumenDTO> categorias = resumenPorCategoria.values().stream()
                .map(CategoriaReporteBuilder::toDto)
                .toList();

        List<ReporteCategoriaResumenDTO> categoriasOrdenadas = categorias.stream()
                .sorted(Comparator.comparingLong((ReporteCategoriaResumenDTO item) -> safeLong(item.movimientosTotales())).reversed())
                .toList();

        long totalCategorias = categorias.size();
        long productosActivos = categorias.stream().mapToLong(item -> safeLong(item.productosActivos())).sum();
        long stockActualTotal = categorias.stream().mapToLong(item -> safeLong(item.stockActualTotal())).sum();
        long movimientosTotales = categorias.stream().mapToLong(item -> safeLong(item.movimientosTotales())).sum();
        long cantidadMovidaTotal = categorias.stream().mapToLong(item -> safeLong(item.cantidadMovidaTotal())).sum();

        return new ResumenCategoriaResultado(
                totalCategorias,
                productosActivos,
                stockActualTotal,
                movimientosTotales,
                cantidadMovidaTotal,
                categorias,
                categoriasOrdenadas
        );
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private void crearHojaResumen(Workbook workbook, ReporteDashboardResponseDTO dashboard, CellStyle encabezado) {
        Sheet sheet = workbook.createSheet("Resumen");
        int rowIndex = 0;
        rowIndex = escribirFila(sheet, rowIndex, "Empresa", valorOpcion(dashboard.empresa()));
        rowIndex = escribirFila(sheet, rowIndex, "Inicio", dashboard.inicio().toString());
        rowIndex = escribirFila(sheet, rowIndex, "Fin", dashboard.fin().toString());
        rowIndex = escribirFila(sheet, rowIndex, "Total productos", String.valueOf(dashboard.totalProductos()));
        rowIndex = escribirFila(sheet, rowIndex, "Productos bajo stock", String.valueOf(dashboard.productosBajoStock()));
        rowIndex = escribirFila(sheet, rowIndex, "Total categorias", String.valueOf(dashboard.totalCategorias()));
        rowIndex = escribirFila(sheet, rowIndex, "Movimientos totales", String.valueOf(dashboard.movimientosTotales()));
        escribirFila(sheet, rowIndex, "Cantidad movida", String.valueOf(dashboard.cantidadMovidaTotal()));
        ajustarEncabezados(sheet, 2);
    }

    private void crearHojaCategorias(Workbook workbook, ReporteDashboardResponseDTO dashboard, CellStyle encabezado) {
        Sheet sheet = workbook.createSheet("Categorias");
        Row header = sheet.createRow(0);
        crearCelda(header, 0, "Categoria", encabezado);
        crearCelda(header, 1, "Productos", encabezado);
        crearCelda(header, 2, "Stock actual", encabezado);
        crearCelda(header, 3, "Movimientos", encabezado);
        crearCelda(header, 4, "Cantidad movida", encabezado);

        int rowIndex = 1;
        for (ReporteCategoriaResumenDTO categoria : dashboard.categorias()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(valorOpcion(categoria.categoriaNombre()));
            row.createCell(1).setCellValue(safeLong(categoria.productosActivos()));
            row.createCell(2).setCellValue(safeLong(categoria.stockActualTotal()));
            row.createCell(3).setCellValue(safeLong(categoria.movimientosTotales()));
            row.createCell(4).setCellValue(safeLong(categoria.cantidadMovidaTotal()));
        }

        ajustarEncabezados(sheet, 5);
    }

    private void crearHojaMovimientos(Workbook workbook, ReporteDashboardResponseDTO dashboard, CellStyle encabezado) {
        Sheet sheet = workbook.createSheet("Movimientos");
        Row header = sheet.createRow(0);
        crearCelda(header, 0, "Fecha", encabezado);
        crearCelda(header, 1, "Producto", encabezado);
        crearCelda(header, 2, "Tipo", encabezado);
        crearCelda(header, 3, "Cantidad", encabezado);
        crearCelda(header, 4, "Motivo", encabezado);

        int rowIndex = 1;
        for (MovimientoInventarioResponseDTO movimiento : dashboard.movimientosRecientes()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(valorOpcion(movimiento.fechaMovimiento()));
            row.createCell(1).setCellValue(valorOpcion(movimiento.productoNombre()));
            row.createCell(2).setCellValue(valorOpcion(movimiento.tipoMovimiento()));
            row.createCell(3).setCellValue(movimiento.cantidad() != null ? movimiento.cantidad() : 0);
            row.createCell(4).setCellValue(valorOpcion(movimiento.motivo()));
        }

        ajustarEncabezados(sheet, 5);
    }

    private int escribirFila(Sheet sheet, int rowIndex, String etiqueta, String valor) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(etiqueta);
        row.createCell(1).setCellValue(valor);
        return rowIndex + 1;
    }

    private void crearCelda(Row row, int columnIndex, String valor, CellStyle estilo) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(valor);
        cell.setCellStyle(estilo);
    }

    private void ajustarEncabezados(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String valorOpcion(String value) {
        return value != null && !value.isBlank() ? value : "N/D";
    }

    private static final class CategoriaReporteBuilder {
        private final UUID categoriaId;
        private final String categoriaNombre;
        private long productosActivos;
        private long stockActualTotal;
        private long stockMinimoTotal;
        private long movimientosTotales;
        private long cantidadMovidaTotal;
        private long entradasMovimientos;
        private long salidasMovimientos;
        private long entradasCantidad;
        private long salidasCantidad;

        private CategoriaReporteBuilder(UUID categoriaId, String categoriaNombre) {
            this.categoriaId = categoriaId;
            this.categoriaNombre = categoriaNombre;
        }

        static CategoriaReporteBuilder fromProductos(ReporteCategoriaResumenDTO dto) {
            CategoriaReporteBuilder builder = new CategoriaReporteBuilder(dto.categoriaId(), dto.categoriaNombre());
            builder.productosActivos = safe(dto.productosActivos());
            builder.stockActualTotal = safe(dto.stockActualTotal());
            builder.stockMinimoTotal = safe(dto.stockMinimoTotal());
            return builder;
        }

        void mergeMovimientos(ReporteCategoriaResumenDTO dto) {
            this.movimientosTotales = safe(dto.movimientosTotales());
            this.cantidadMovidaTotal = safe(dto.cantidadMovidaTotal());
            this.entradasMovimientos = safe(dto.entradasMovimientos());
            this.salidasMovimientos = safe(dto.salidasMovimientos());
            this.entradasCantidad = safe(dto.entradasCantidad());
            this.salidasCantidad = safe(dto.salidasCantidad());
        }

        ReporteCategoriaResumenDTO toDto() {
            return new ReporteCategoriaResumenDTO(
                    categoriaId,
                    categoriaNombre,
                    productosActivos,
                    stockActualTotal,
                    stockMinimoTotal,
                    movimientosTotales,
                    cantidadMovidaTotal,
                    entradasMovimientos,
                    salidasMovimientos,
                    entradasCantidad,
                    salidasCantidad
            );
        }

        private static long safe(Long value) {
            return value != null ? value : 0L;
        }
    }

    private record ReporteContexto(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String empresa
    ) {
        LocalDateTime inicioDateTime() {
            return fechaInicio.atStartOfDay();
        }

        LocalDateTime finDateTime() {
            return fechaFin.atTime(LocalTime.MAX);
        }

        boolean tieneEmpresa() {
            return empresa != null && !empresa.isBlank();
        }
    }

    private record ResumenCategoriaResultado(
            long totalCategorias,
            long productosActivos,
            long stockActualTotal,
            long movimientosTotales,
            long cantidadMovidaTotal,
            List<ReporteCategoriaResumenDTO> categorias,
            List<ReporteCategoriaResumenDTO> categoriasOrdenadas
    ) {
    }
}
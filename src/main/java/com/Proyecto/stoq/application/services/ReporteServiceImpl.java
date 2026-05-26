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

        List<ReporteCategoriaResumenDTO> resumenProductos = java.util.Optional.ofNullable(productosRepository.obtenerResumenCategorias(empresa))
            .orElse(List.of());
        List<ReporteCategoriaResumenDTO> resumenMovimientos = java.util.Optional.ofNullable(movimientoInventarioRepository.obtenerResumenMovimientosPorCategoria(
                empresa,
                inicioDateTime,
                finDateTime
        )).orElse(List.of());

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

        long movimientosTotales = safeLong(movimientoInventarioRepository.contarMovimientosPorEmpresa(
            contexto.empresa(),
            contexto.inicioDateTime(),
            contexto.finDateTime()
        ));
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
                movimientosTotales,
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
        // Improved PDF layout: centered title, metadata block, KPI section and top categories list.
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float margin = 50f;
            float yStart = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth();

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // obtain fonts compatible with PDFBox 3 by constructing PDType1Font instances
                org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Title
                String title = "Reporte estadístico";
                int titleFontSize = 18;
                float titleWidth = fontBold.getStringWidth(title) / 1000 * titleFontSize;
                float titleX = (width - titleWidth) / 2f;
                contentStream.beginText();
                contentStream.setFont(fontBold, titleFontSize);
                contentStream.newLineAtOffset(titleX, yStart);
                contentStream.showText(title);
                contentStream.endText();

                float cursorY = yStart - titleFontSize - 12;

                // Metadata
                contentStream.beginText();
                contentStream.setFont(fontRegular, 11);
                contentStream.newLineAtOffset(margin, cursorY);
                contentStream.showText("Rango: " + dashboard.inicio() + " — " + dashboard.fin());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Empresa: " + valorOpcion(dashboard.empresa()));
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Generado: " + LocalDateTime.now().toString());
                contentStream.endText();

                cursorY -= 14 * 3 + 8;

                // KPIs box
                contentStream.beginText();
                contentStream.setFont(fontBold, 12);
                contentStream.newLineAtOffset(margin, cursorY);
                contentStream.showText("Resumen");
                contentStream.endText();

                cursorY -= 16;

                contentStream.beginText();
                contentStream.setFont(fontRegular, 11);
                contentStream.newLineAtOffset(margin, cursorY);
                contentStream.showText("Total productos: " + dashboard.totalProductos());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Productos bajo stock: " + dashboard.productosBajoStock());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Movimientos totales: " + dashboard.movimientosTotales());
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Cantidad movida: " + dashboard.cantidadMovidaTotal());
                contentStream.endText();

                cursorY -= 14 * 5 + 8;

                // Top categories header
                contentStream.beginText();
                contentStream.setFont(fontBold, 12);
                contentStream.newLineAtOffset(margin, cursorY);
                contentStream.showText("Top categorías por movimientos");
                contentStream.endText();

                cursorY -= 16;

                // List top categories as table-like lines
                contentStream.setFont(fontRegular, 11);
                for (ReporteCategoriaResumenDTO categoria : dashboard.categorias().stream().limit(20).toList()) {
                    if (cursorY < margin + 60) {
                        // new page
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        cursorY = page.getMediaBox().getHeight() - margin;
                        try (PDPageContentStream cs2 = new PDPageContentStream(document, page)) {
                            cs2.beginText();
                            cs2.setFont(fontRegular, 11);
                            cs2.newLineAtOffset(margin, cursorY);
                            cs2.showText(String.format("- %s | movimientos: %d | cantidad: %d",
                                    valorOpcion(categoria.categoriaNombre()),
                                    safeLong(categoria.movimientosTotales()),
                                    safeLong(categoria.cantidadMovidaTotal())));
                            cs2.endText();
                        }
                        cursorY -= 14;
                        continue;
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, cursorY);
                    contentStream.showText(String.format("- %s | movimientos: %d | cantidad: %d",
                            valorOpcion(categoria.categoriaNombre()),
                            safeLong(categoria.movimientosTotales()),
                            safeLong(categoria.cantidadMovidaTotal())));
                    contentStream.endText();
                    cursorY -= 14;
                }
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
            CellStyle headerTextStyle = crearHeaderTextStyle(workbook);
            crearHojaResumen(workbook, dashboard, encabezado);
            crearHojaCategorias(workbook, dashboard, encabezado, headerTextStyle);
            crearHojaMovimientos(workbook, dashboard, encabezado, headerTextStyle);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el Excel del reporte", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarReporteCsv(LocalDate inicio, LocalDate fin) {
        ReporteDashboardResponseDTO dashboard = obtenerDashboard(inicio, fin);
        StringBuilder sb = new StringBuilder();
        // Header info
        sb.append("Reporte estadistico\n");
        sb.append(String.format("Rango:, %s — %s\n", dashboard.inicio(), dashboard.fin()));
        sb.append(String.format("Empresa:, %s\n", valorOpcion(dashboard.empresa())));
        sb.append(String.format("Total productos:, %d\n", dashboard.totalProductos()));
        sb.append(String.format("Productos bajo stock:, %d\n", dashboard.productosBajoStock()));
        sb.append(String.format("Movimientos totales:, %d\n", dashboard.movimientosTotales()));
        sb.append(String.format("Cantidad movida:, %d\n", dashboard.cantidadMovidaTotal()));
        sb.append("\n");

        sb.append("Top categorias por movimientos\n");
        sb.append("Categoria,Movimientos,Cantidad\n");
        for (ReporteCategoriaResumenDTO categoria : dashboard.categorias()) {
            sb.append(String.format("%s,%d,%d\n",
                    csvSafe(valorOpcion(categoria.categoriaNombre())),
                    safeLong(categoria.movimientosTotales()),
                    safeLong(categoria.cantidadMovidaTotal())));
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
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

        List<ReporteCategoriaResumenDTO> resumenProductos = java.util.Optional.ofNullable(productosRepository.obtenerResumenCategorias(contexto.empresa()))
            .orElse(List.of());
        List<ReporteCategoriaResumenDTO> resumenMovimientos = java.util.Optional.ofNullable(movimientoInventarioRepository.obtenerResumenMovimientosPorCategoria(
                contexto.empresa(),
                inicioDateTime,
                finDateTime
        )).orElse(List.of());

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

    private void crearHojaCategorias(Workbook workbook, ReporteDashboardResponseDTO dashboard, CellStyle encabezado, CellStyle headerTextStyle) {
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

        // apply header text style to header row cells
        for (Cell cell : header) {
            cell.setCellStyle(headerTextStyle);
        }

        ajustarEncabezados(sheet, 5);
    }

    private void crearHojaMovimientos(Workbook workbook, ReporteDashboardResponseDTO dashboard, CellStyle encabezado, CellStyle headerTextStyle) {
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

        for (Cell cell : header) {
            cell.setCellStyle(headerTextStyle);
        }

        ajustarEncabezados(sheet, 5);
    }

    private CellStyle crearHeaderTextStyle(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(font);
        estilo.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private String csvSafe(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
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
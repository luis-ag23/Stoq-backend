package com.Proyecto.stoq.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;
import com.Proyecto.stoq.infrastructure.persistence.repositories.ProductosRepository;

public class ReporteServiceImplTest {

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private ReporteServiceImpl reporteService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reporteService = new ReporteServiceImpl(productosRepository, movimientoInventarioRepository, usuarioRepository);

        Usuario usuario = new Usuario();
        usuario.setEmpresa("Empresa A");
        when(usuarioRepository.findByCorreo("gerente@empresa.com")).thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gerente@empresa.com", "n/a")
        );
    }

    @AfterEach
    public void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void agrupaProductosYMovimientosPorCategoria() {
        UUID alimentos = UUID.randomUUID();
        UUID bebidas = UUID.randomUUID();

        when(productosRepository.obtenerResumenCategorias("Empresa A")).thenReturn(List.of(
                new ReporteCategoriaResumenDTO(alimentos, "Alimentos", 3L, 20L, 9L, 0L, 0L, 0L, 0L, 0L, 0L),
                new ReporteCategoriaResumenDTO(bebidas, "Bebidas", 2L, 12L, 4L, 0L, 0L, 0L, 0L, 0L, 0L)
        ));

        when(movimientoInventarioRepository.obtenerResumenMovimientosPorCategoria(
                "Empresa A",
                LocalDate.of(2026, 5, 1).atStartOfDay(),
                LocalDate.of(2026, 5, 17).atTime(23, 59, 59, 999_999_999)
        )).thenReturn(List.of(
                new ReporteCategoriaResumenDTO(alimentos, "Alimentos", 0L, 0L, 0L, 5L, 30L, 3L, 2L, 18L, 12L)
        ));

        var response = reporteService.obtenerReportePorCategoria(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 17));

        assertEquals("Empresa A", response.empresa());
        assertEquals(2L, response.totalCategorias());
        assertEquals(5L, response.productosActivos());
        assertEquals(32L, response.stockActualTotal());
        assertEquals(5L, response.movimientosTotales());
        assertEquals(30L, response.cantidadMovidaTotal());
        assertEquals(2, response.categorias().size());
    }
}
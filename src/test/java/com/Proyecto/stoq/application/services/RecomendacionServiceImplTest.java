package com.Proyecto.stoq.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.RecomendacionAutomaticaDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;

public class RecomendacionServiceImplTest {

    @Mock
    private ProductosRepositoryPort productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private RecomendacionServiceImpl recomendacionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        recomendacionService = new RecomendacionServiceImpl(productoRepository, movimientoRepository, usuarioRepository);
    }

    @Test
    public void testRecomendacionesConSalidasVacias() {
        Usuario usuario = new Usuario("Test User", "test@example.com", "Test Company", "hash", new Rol());
        doReturn(Optional.of(usuario)).when(usuarioRepository).findByCorreo("test@example.com");

        Producto p = new Producto();
        p.setId(UUID.randomUUID());
        p.setCodigo("PROD1");
        p.setNombre("Producto Sin Salidas");
        p.setStockActual(5);
        p.setStockMinimo(10);
        p.setEmpresa("Test Company");
        p.setEstado(true);

        doReturn(List.of(p)).when(productoRepository).findAll();
        doReturn(List.of()).when(movimientoRepository).obtenerMovimientosPorProductoYTipo(any(), eq("SALIDA"));

        List<RecomendacionAutomaticaDTO> recs = recomendacionService.obtenerRecomendaciones("test@example.com");

        assertEquals(1, recs.size());
        RecomendacionAutomaticaDTO r = recs.get(0);
        assertEquals(0.0, r.consumoPromedioDiario());
        assertEquals(9999, r.tiempoAgotamiento());
        assertEquals("BAJA", r.rotacion());
        assertEquals(15, r.cantidadRecomendada()); // (10 * 2) - 5 = 15
        assertEquals("ALTA", r.prioridad()); // Since stockActual (5) <= stockMinimo (10)
    }

    @Test
    public void testRecomendacionesConSalidasExistentes() {
        Usuario usuario = new Usuario("Test User", "test@example.com", "Test Company", "hash", new Rol());
        doReturn(Optional.of(usuario)).when(usuarioRepository).findByCorreo("test@example.com");

        Producto p = new Producto();
        p.setId(UUID.randomUUID());
        p.setCodigo("PROD2");
        p.setNombre("Producto Con Salidas");
        p.setStockActual(10);
        p.setStockMinimo(5);
        p.setEmpresa("Test Company");
        p.setEstado(true);

        // Movimiento de salida hace 10 días, cantidad total = 30 (3 por día en promedio)
        Movimiento_Inventario mov = new Movimiento_Inventario();
        mov.setCantidad(30);
        mov.setFechaMovimiento(LocalDateTime.now().minusDays(10));
        mov.setTipoMovimiento("SALIDA");

        doReturn(List.of(p)).when(productoRepository).findAll();
        doReturn(List.of(mov)).when(movimientoRepository).obtenerMovimientosPorProductoYTipo(any(), eq("SALIDA"));

        List<RecomendacionAutomaticaDTO> recs = recomendacionService.obtenerRecomendaciones("test@example.com");

        assertEquals(1, recs.size());
        RecomendacionAutomaticaDTO r = recs.get(0);
        assertTrue(r.consumoPromedioDiario() > 0.0);
        assertEquals("ALTA", r.rotacion()); // CPD >= 1.0
        assertEquals(0, r.cantidadRecomendada()); // (5 * 2) - 10 = 0
    }
}

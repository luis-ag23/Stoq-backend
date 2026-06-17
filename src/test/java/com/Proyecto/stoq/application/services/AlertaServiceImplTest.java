package com.Proyecto.stoq.application.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.List;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.dto.AlertasResumenDTO;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AlertaServiceImplTest {

    @Mock
    private AlertaRepositoryPort alertaRepository;

    @Mock
    private ProductosRepositoryPort productoRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private AlertaServiceImpl alertaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        alertaService = new AlertaServiceImpl(alertaRepository, productoRepository, movimientoInventarioRepository, usuarioRepository);
    }

    @Test
    public void creaAlertaStockBajoCuandoCruzaMinimo() {
        Producto p = new Producto();
        UUID id = UUID.randomUUID();
        p.setId(id);
        p.setCodigo("SKU-1");
        p.setNombre("Producto 1");
        p.setStockMinimo(5);

        doReturn(false).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("STOCK_BAJO"));

        alertaService.verificarCambioStock(p, 6, 5);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        Alerta creada = captor.getValue();
        assert creada.getTipo().equals("STOCK_BAJO");
    }

    @Test
    public void creaAlertaStockCeroCuandoLlegaACero() {
        Producto p = new Producto();
        UUID id = UUID.randomUUID();
        p.setId(id);
        p.setCodigo("SKU-2");
        p.setNombre("Producto 2");
        p.setStockMinimo(5);

        doReturn(false).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("STOCK_CERO"));

        alertaService.verificarCambioStock(p, 2, 0);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        Alerta creada = captor.getValue();
        assert creada.getTipo().equals("STOCK_CERO");
    }

    @Test
    public void noDuplicaSiYaCritico() {
        Producto p = new Producto();
        UUID id = UUID.randomUUID();
        p.setId(id);
        p.setCodigo("SKU-3");
        p.setNombre("Producto 3");
        p.setStockMinimo(5);

        // Simula que ya estaba por debajo (stockAnterior <= minimo)
        alertaService.verificarCambioStock(p, 4, 3);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    public void noCreaSiAlertaDelMismoTipoYaExiste() {
        Producto p = new Producto();
        UUID id = UUID.randomUUID();
        p.setId(id);
        p.setCodigo("SKU-4");
        p.setNombre("Producto 4");
        p.setStockMinimo(5);

        doReturn(true).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("STOCK_BAJO"));

        alertaService.verificarCambioStock(p, 6, 4);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    public void resumenDevuelveCerosCuandoNoHayDatos() {
        doReturn(List.of()).when(productoRepository).findAll();
        doReturn(List.of()).when(alertaRepository).findAll();
        doReturn(0L).when(alertaRepository).countByLeidaFalse();

        AlertasResumenDTO resumen = alertaService.obtenerResumen();

        assertEquals(0L, resumen.productosCriticos());
        assertEquals(0L, resumen.notificacionesSinLeer());
        assertEquals(0L, resumen.totalAlertas());
    }

    @Test
    public void creaAlertaRiesgoAgotamientoCuandoLaCoberturaEsBaja() {
        Producto producto = new Producto();
        UUID id = UUID.randomUUID();
        producto.setId(id);
        producto.setCodigo("SKU-5");
        producto.setNombre("Producto 5");
        producto.setStockActual(1);
        producto.setStockMinimo(5);

        doReturn(0L).when(movimientoInventarioRepository).sumarCantidadSalidasPorProductoEntre(any(), any(), any());
        doReturn(0L).when(movimientoInventarioRepository).contarSalidasPorProductoEntre(any(), any(), any());
        doReturn(false).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("RIESGO_AGOTAMIENTO"));

        alertaService.verificarRiesgosInventario(producto);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals("RIESGO_AGOTAMIENTO", captor.getValue().getTipo());
    }

    @Test
    public void creaAlertaConsumoAnormalCuandoSuperaUmbral() {
        Producto producto = new Producto();
        UUID id = UUID.randomUUID();
        producto.setId(id);
        producto.setCodigo("SKU-6");
        producto.setNombre("Producto 6");
        producto.setStockActual(50);
        producto.setStockMinimo(5);

        doReturn(210L).when(movimientoInventarioRepository).sumarCantidadSalidasPorProductoEntre(any(), any(), any());
        doReturn(5L).when(movimientoInventarioRepository).contarSalidasPorProductoEntre(any(), any(), any());
        doReturn(false).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("CONSUMO_ANORMAL"));

        alertaService.verificarRiesgosInventario(producto);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals("CONSUMO_ANORMAL", captor.getValue().getTipo());
    }

    @Test
    public void creaAlertaBajaRotacionCuandoNoHaySalidasEn60Dias() {
        Producto producto = new Producto();
        UUID id = UUID.randomUUID();
        producto.setId(id);
        producto.setCodigo("SKU-7");
        producto.setNombre("Producto 7");
        producto.setStockActual(20);
        producto.setStockMinimo(5);

        doReturn(0L).when(movimientoInventarioRepository).sumarCantidadSalidasPorProductoEntre(any(), any(), any());
        doReturn(0L).when(movimientoInventarioRepository).contarSalidasPorProductoEntre(any(), any(), any());
        doReturn(false).when(alertaRepository).existsByProductoIdAndTipoAndLeidaFalse(eq(id), eq("BAJA_ROTACION_PROLONGADA"));

        alertaService.verificarRiesgosInventario(producto);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals("BAJA_ROTACION_PROLONGADA", captor.getValue().getTipo());
    }

    @Test
    public void evaluacionProgramadaRecorreProductosActivos() {
        Producto activo = new Producto();
        UUID idActivo = UUID.randomUUID();
        activo.setId(idActivo);
        activo.setCodigo("SKU-8");
        activo.setNombre("Producto activo");
        activo.setStockActual(10);
        activo.setStockMinimo(5);
        activo.setEstado(true);

        Producto inactivo = new Producto();
        inactivo.setId(UUID.randomUUID());
        inactivo.setEstado(false);

        doReturn(List.of(activo, inactivo)).when(productoRepository).findAll();
        doReturn(0L).when(movimientoInventarioRepository).sumarCantidadSalidasPorProductoEntre(any(), any(), any());
        doReturn(0L).when(movimientoInventarioRepository).contarSalidasPorProductoEntre(any(), any(), any());

        alertaService.evaluarRiesgosInventarioProgramado();

        verify(movimientoInventarioRepository, org.mockito.Mockito.atLeastOnce())
                .sumarCantidadSalidasPorProductoEntre(any(), any(), any());
    }
}

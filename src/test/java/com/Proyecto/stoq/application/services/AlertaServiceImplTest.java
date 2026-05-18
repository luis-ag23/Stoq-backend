package com.Proyecto.stoq.application.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.AlertasResumenDTO;

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

    private AlertaServiceImpl alertaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        alertaService = new AlertaServiceImpl(alertaRepository, productoRepository, usuarioRepository);
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
}

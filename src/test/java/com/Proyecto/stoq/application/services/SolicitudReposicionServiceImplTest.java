package com.Proyecto.stoq.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.Proyecto.stoq.domain.model.EstadoSolicitud;
import com.Proyecto.stoq.domain.model.PrioridadSolicitud;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.model.RotacionProducto;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.SolicitudReposicionRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.SolicitudReposicionResponseDTO;

public class SolicitudReposicionServiceImplTest {

    @Mock
    private SolicitudReposicionRepositoryPort solicitudRepository;

    @Mock
    private ProductosRepositoryPort productoRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private RecomendacionService recomendacionService;

    @Mock
    private AuditService auditService;

    private SolicitudReposicionServiceImpl solicitudService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        solicitudService = new SolicitudReposicionServiceImpl(
                solicitudRepository, productoRepository, usuarioRepository, recomendacionService, auditService
        );
    }

    @Test
    public void testOperadorNoPuedeAprobar() {
        Rol rolOperador = new Rol();
        rolOperador.setNombre("OPERADOR");
        Usuario usuario = new Usuario("Operador User", "ope@example.com", "Test Company", "hash", rolOperador);
        doReturn(Optional.of(usuario)).when(usuarioRepository).findByCorreo("ope@example.com");

        SolicitudReposicion sol = new SolicitudReposicion();
        sol.setId(UUID.randomUUID());
        sol.setEmpresa("Test Company");
        sol.setEstado(EstadoSolicitud.PENDIENTE);
        sol.setProducto(new Producto());
        sol.setPrioridad(PrioridadSolicitud.ALTA);
        sol.setRotacion(RotacionProducto.BAJA);

        doReturn(Optional.of(sol)).when(solicitudRepository).findById(any());

        assertThrows(RuntimeException.class, () -> {
            solicitudService.actualizarEstadoSolicitud(sol.getId(), "ope@example.com", "APROBADA");
        });
    }

    @Test
    public void testGerenteSiPuedeAprobar() {
        Rol rolGerente = new Rol();
        rolGerente.setNombre("GERENTE");
        Usuario usuario = new Usuario("Gerente User", "ger@example.com", "Test Company", "hash", rolGerente);
        doReturn(Optional.of(usuario)).when(usuarioRepository).findByCorreo("ger@example.com");

        SolicitudReposicion sol = new SolicitudReposicion();
        sol.setId(UUID.randomUUID());
        sol.setEmpresa("Test Company");
        sol.setEstado(EstadoSolicitud.PENDIENTE);
        sol.setProducto(new Producto());
        sol.setPrioridad(PrioridadSolicitud.ALTA);
        sol.setRotacion(RotacionProducto.BAJA);

        doReturn(Optional.of(sol)).when(solicitudRepository).findById(any());
        doReturn(sol).when(solicitudRepository).save(any());

        SolicitudReposicionResponseDTO res = 
                solicitudService.actualizarEstadoSolicitud(sol.getId(), "ger@example.com", "APROBADA");

        assertEquals("APROBADA", res.estado());
        ArgumentCaptor<SolicitudReposicion> captor = ArgumentCaptor.forClass(SolicitudReposicion.class);
        verify(solicitudRepository).save(captor.capture());
        assertEquals(EstadoSolicitud.APROBADA, captor.getValue().getEstado());
    }
}

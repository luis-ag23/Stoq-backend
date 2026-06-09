package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.EstadoSolicitud;
import com.Proyecto.stoq.domain.model.PrioridadSolicitud;
import com.Proyecto.stoq.domain.model.RotacionProducto;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.SolicitudReposicionRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.RecomendacionAutomaticaDTO;
import com.Proyecto.stoq.dto.SolicitudReposicionResponseDTO;
import com.Proyecto.stoq.security.RoleCatalog;

@Service
public class SolicitudReposicionServiceImpl implements SolicitudReposicionService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudReposicionServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final SolicitudReposicionRepositoryPort solicitudRepository;
    private final ProductosRepositoryPort productoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RecomendacionService recomendacionService;
    private final AuditService auditService;

    public SolicitudReposicionServiceImpl(
            SolicitudReposicionRepositoryPort solicitudRepository,
            ProductosRepositoryPort productoRepository,
            UsuarioRepositoryPort usuarioRepository,
            RecomendacionService recomendacionService,
            AuditService auditService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.recomendacionService = recomendacionService;
        this.auditService = auditService;
    }

    @Override
    public List<SolicitudReposicionResponseDTO> obtenerSolicitudesPorEmpresa(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String empresa = usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null;
        if (empresa == null || empresa.isBlank()) {
            return List.of();
        }

        return solicitudRepository.findByEmpresa(empresa).stream()
                .map(SolicitudReposicionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void generarSolicitudesAutomaticas(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String empresa = usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null;
        if (empresa == null || empresa.isBlank()) {
            throw new RuntimeException("El usuario no tiene empresa asignada");
        }

        List<RecomendacionAutomaticaDTO> recomendaciones = recomendacionService.obtenerRecomendaciones(emailUsuario);

        for (RecomendacionAutomaticaDTO rec : recomendaciones) {
            // Solo generar solicitudes para productos que requieran reposición
            if (rec.cantidadRecomendada() <= 0) {
                continue;
            }

            // Verificar si ya existe una solicitud PENDIENTE para este producto
            Optional<SolicitudReposicion> solicitudExistente = 
                    solicitudRepository.findByProductoIdAndEstado(rec.productoId(), EstadoSolicitud.PENDIENTE);

            if (solicitudExistente.isPresent()) {
                SolicitudReposicion sol = solicitudExistente.get();
                Map<String, Object> estadoAnterior = snapshotSolicitud(sol);

                sol.setCantidadRecomendada(rec.cantidadRecomendada());
                sol.setPrioridad(PrioridadSolicitud.valueOf(rec.prioridad()));
                sol.setConsumoPromedioDiario(rec.consumoPromedioDiario());
                sol.setTiempoAgotamiento(rec.tiempoAgotamiento());
                sol.setRotacion(RotacionProducto.valueOf(rec.rotacion()));
                sol.setFechaActualizacion(LocalDateTime.now());

                SolicitudReposicion solGuardada = solicitudRepository.save(sol);
                auditService.registrarAuditoria("SolicitudReposicion", "UPDATE", solGuardada.getId(), estadoAnterior, snapshotSolicitud(solGuardada));
                logger.info("{} Solicitud de reposición PENDIENTE actualizada para producto: {}", BIZ_TAG, rec.nombre());
            } else {
                Producto producto = productoRepository.findById(rec.productoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                SolicitudReposicion nuevaSol = new SolicitudReposicion();
                nuevaSol.setProducto(producto);
                nuevaSol.setCantidadRecomendada(rec.cantidadRecomendada());
                nuevaSol.setPrioridad(PrioridadSolicitud.valueOf(rec.prioridad()));
                nuevaSol.setEstado(EstadoSolicitud.PENDIENTE);
                nuevaSol.setConsumoPromedioDiario(rec.consumoPromedioDiario());
                nuevaSol.setTiempoAgotamiento(rec.tiempoAgotamiento());
                nuevaSol.setRotacion(RotacionProducto.valueOf(rec.rotacion()));
                nuevaSol.setEmpresa(empresa);

                SolicitudReposicion solGuardada = solicitudRepository.save(nuevaSol);
                auditService.registrarAuditoria("SolicitudReposicion", "CREATE", solGuardada.getId(), null, snapshotSolicitud(solGuardada));
                logger.info("{} Nueva solicitud de reposición PENDIENTE creada para producto: {}", BIZ_TAG, rec.nombre());
            }
        }
    }

    @Override
    @Transactional
    public SolicitudReposicionResponseDTO actualizarEstadoSolicitud(UUID id, String emailUsuario, String nuevoEstadoStr) {
        Usuario usuario = usuarioRepository.findByCorreo(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String empresa = usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null;
        if (empresa == null || empresa.isBlank()) {
            throw new RuntimeException("El usuario no tiene empresa asignada");
        }

        SolicitudReposicion solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!empresa.equalsIgnoreCase(solicitud.getEmpresa())) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        EstadoSolicitud nuevoEstado;
        try {
            nuevoEstado = EstadoSolicitud.valueOf(nuevoEstadoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de solicitud no válido");
        }

        // Aplicar control de acceso para que solo ADMIN o GERENTE puedan cambiar el estado a APROBADA o RECHAZADA
        if (nuevoEstado == EstadoSolicitud.APROBADA || nuevoEstado == EstadoSolicitud.RECHAZADA) {
            String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
            String normalizedRol = RoleCatalog.normalize(rolNombre);
            if (!RoleCatalog.ADMIN.equals(normalizedRol) && !RoleCatalog.GERENTE.equals(normalizedRol)) {
                logger.warn("{} Usuario {} con rol {} intentó cambiar estado a {} sin autorización.", 
                        BIZ_TAG, emailUsuario, normalizedRol, nuevoEstado);
                throw new RuntimeException("No tiene autorización para cambiar el estado a APROBADA o RECHAZADA");
            }
        }

        Map<String, Object> estadoAnterior = snapshotSolicitud(solicitud);
        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        SolicitudReposicion solGuardada = solicitudRepository.save(solicitud);
        auditService.registrarAuditoria("SolicitudReposicion", "UPDATE", solGuardada.getId(), estadoAnterior, snapshotSolicitud(solGuardada));

        logger.info("{} Solicitud de reposición {} cambiada a estado: {} por usuario: {}", 
                BIZ_TAG, id, nuevoEstado, emailUsuario);

        return SolicitudReposicionResponseDTO.fromEntity(solGuardada);
    }

    private Map<String, Object> snapshotSolicitud(SolicitudReposicion solicitud) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", solicitud.getId());
        snapshot.put("productoId", solicitud.getProducto() != null ? solicitud.getProducto().getId() : null);
        snapshot.put("cantidadRecomendada", solicitud.getCantidadRecomendada());
        snapshot.put("prioridad", solicitud.getPrioridad() != null ? solicitud.getPrioridad().name() : null);
        snapshot.put("estado", solicitud.getEstado() != null ? solicitud.getEstado().name() : null);
        snapshot.put("consumoPromedioDiario", solicitud.getConsumoPromedioDiario());
        snapshot.put("tiempoAgotamiento", solicitud.getTiempoAgotamiento());
        snapshot.put("rotacion", solicitud.getRotacion() != null ? solicitud.getRotacion().name() : null);
        snapshot.put("empresa", solicitud.getEmpresa());
        snapshot.put("fechaSolicitud", solicitud.getFechaSolicitud());
        snapshot.put("fechaActualizacion", solicitud.getFechaActualizacion());
        return snapshot;
    }
}

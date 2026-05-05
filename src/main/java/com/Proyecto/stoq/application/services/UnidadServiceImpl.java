package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.dto.CreateUnidadDTO;

@Service
public class UnidadServiceImpl implements UnidadService {
    private static final Logger logger = LoggerFactory.getLogger(UnidadServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final UnidadRepositoryPort unidadRepository;
    private final AuditService auditService;

    public UnidadServiceImpl(UnidadRepositoryPort unidadRepository, AuditService auditService) {
        this.unidadRepository = unidadRepository;
        this.auditService = auditService;
    }

    @Override
    public List<Unidad> obtenerUnidades() {
        return unidadRepository.findAll();
    }

    @Override
    public Optional<Unidad> obtenerUnidadPorId(UUID id) {
        return unidadRepository.findById(id);
    }

    @Override
    public Unidad crearUnidad(CreateUnidadDTO dto) {
        Unidad unidad = new Unidad();
        unidad.setNombre(dto.nombre);
        unidad.setAbreviatura(dto.abreviatura);
        logger.info("{} CREATE Unidad | nombre={} | abreviatura={}", BIZ_TAG, dto.nombre, dto.abreviatura);
        Unidad unidadGuardada = unidadRepository.save(unidad);
        auditService.registrarAuditoria("Unidad", "CREATE", unidadGuardada.getId(), null, snapshotUnidad(unidadGuardada));
        return unidadGuardada;
    }

    @Override
    public Unidad actualizarUnidad(UUID id, CreateUnidadDTO dto) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));
        Map<String, Object> estadoAnterior = snapshotUnidad(unidad);

        if (dto.nombre != null && !dto.nombre.isBlank()) {
            unidad.setNombre(dto.nombre);
        }

        if (dto.abreviatura != null && !dto.abreviatura.isBlank()) {
            unidad.setAbreviatura(dto.abreviatura);
        }

        logger.info("{} UPDATE Unidad | id={} | nombre={}", BIZ_TAG, id, unidad.getNombre());
        Unidad unidadActualizada = unidadRepository.save(unidad);
        auditService.registrarAuditoria("Unidad", "UPDATE", id, estadoAnterior, snapshotUnidad(unidadActualizada));
        return unidadActualizada;
    }

    @Override
    public void eliminarUnidad(UUID id) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        logger.info("{} DELETE Unidad | id={}", BIZ_TAG, id);
        auditService.registrarAuditoria("Unidad", "DELETE", id, snapshotUnidad(unidad));
        unidadRepository.deleteById(id);
    }

    private Map<String, Object> snapshotUnidad(Unidad unidad) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", unidad.getId());
        snapshot.put("nombre", unidad.getNombre());
        snapshot.put("abreviatura", unidad.getAbreviatura());
        snapshot.put("estado", unidad.getEstado());
        return snapshot;
    }
    
}

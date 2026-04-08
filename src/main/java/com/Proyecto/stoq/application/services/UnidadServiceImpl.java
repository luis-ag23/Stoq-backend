package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public Unidad crearUnidad(CreateUnidadDTO dto) {
        Unidad unidad = new Unidad();
        unidad.setNombre(dto.nombre);
        unidad.setAbreviatura(dto.abreviatura);
        logger.info("{} CREATE Unidad | nombre={} | abreviatura={}", BIZ_TAG, dto.nombre, dto.abreviatura);
        Unidad unidadGuardada = unidadRepository.save(unidad);
        auditService.registrarAuditoria("Unidad", "CREATE", unidadGuardada.getId(), null, snapshotUnidad(unidadGuardada));
        return unidadGuardada;
    }

    private Map<String, Object> snapshotUnidad(Unidad unidad) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", unidad.getId());
        snapshot.put("nombre", unidad.getNombre());
        snapshot.put("abreviatura", unidad.getAbreviatura());
        return snapshot;
    }
    
}

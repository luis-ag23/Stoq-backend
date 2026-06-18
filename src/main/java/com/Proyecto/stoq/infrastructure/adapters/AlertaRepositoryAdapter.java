package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.AlertaRepository;

@Repository
public class AlertaRepositoryAdapter implements AlertaRepositoryPort {

    private final AlertaRepository alertaRepository;

    public AlertaRepositoryAdapter(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Override
    public Alerta save(Alerta alerta) {
        return alertaRepository.save(alerta);
    }

    @Override
    public List<Alerta> findAll() {
        return alertaRepository.findAll();
    }

    @Override
    public Optional<Alerta> findById(UUID id) {
        return alertaRepository.findById(id);
    }

    @Override
    public boolean existsByProductoIdAndTipoAndLeidaFalse(UUID productoId, String tipo) {
        return alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(productoId, tipo);
    }

    @Override
    public long countByLeidaFalse() {
        return alertaRepository.countByLeidaFalse();
    }
}
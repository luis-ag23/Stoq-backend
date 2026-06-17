package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import com.Proyecto.stoq.domain.model.SolicitudReposicion;
import com.Proyecto.stoq.domain.model.EstadoSolicitud;
import com.Proyecto.stoq.domain.ports.SolicitudReposicionRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.SolicitudReposicionRepository;

@Repository
public class SolicitudReposicionRepositoryAdapter implements SolicitudReposicionRepositoryPort {

    private final SolicitudReposicionRepository repository;

    public SolicitudReposicionRepositoryAdapter(SolicitudReposicionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SolicitudReposicion> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<SolicitudReposicion> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<SolicitudReposicion> findByEmpresa(String empresa) {
        return repository.findByEmpresaOrderByFechaSolicitudDesc(empresa);
    }

    @Override
    public Optional<SolicitudReposicion> findByProductoIdAndEstado(UUID productoId, EstadoSolicitud estado) {
        return repository.findByProductoIdAndEstado(productoId, estado);
    }

    @Override
    public SolicitudReposicion save(SolicitudReposicion solicitud) {
        return repository.save(solicitud);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}

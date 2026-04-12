package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.UnidadRepository;

@Repository
public class UnidadRepositoryAdapter implements UnidadRepositoryPort {

    private final UnidadRepository repository;

    public UnidadRepositoryAdapter(UnidadRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Unidad> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Unidad> findByEstadoTrue() {
        return repository.findByEstadoTrue();
    }

    @Override
    public Optional<Unidad> findByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

    @Override
    public Unidad save(Unidad unidad) {
        return repository.save(unidad);
    }

    @Override
    public Optional<Unidad> findById(UUID id) {
        return repository.findById(id);
    }
}
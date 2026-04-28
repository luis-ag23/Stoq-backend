package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.RolRepository;

@Repository
public class RolRepositoryAdapter implements RolRepositoryPort {

    private final RolRepository rolRepository;

    public RolRepositoryAdapter(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    @Override
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Override
    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }
}
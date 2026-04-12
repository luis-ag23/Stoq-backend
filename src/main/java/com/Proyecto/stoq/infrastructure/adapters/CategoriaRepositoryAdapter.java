package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.CategoriaRepository;



@Repository
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaRepository repository;

    public CategoriaRepositoryAdapter(CategoriaRepository repository){
        this.repository = repository;
    }

    @Override
    public List<Categoria> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Categoria> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Categoria save(Categoria categoria) {
        return repository.save(categoria);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Categoria> findByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

}

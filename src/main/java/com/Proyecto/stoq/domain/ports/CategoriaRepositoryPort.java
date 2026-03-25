package com.Proyecto.stoq.domain.ports;

import com.Proyecto.stoq.domain.model.Categoria;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepositoryPort {
    List<Categoria> findAll();
    Optional<Categoria> findById(UUID id);
    Categoria save(Categoria categoria);
    void deleteById(UUID id);
    Optional<Categoria> findByNombre(String nombre);
}

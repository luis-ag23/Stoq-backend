package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.dto.CreateCategoriaDTO;

@Service
public class CategoriaServiceImpl implements CategoriaService{

    private final CategoriaRepositoryPort categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAll();    
    }

    @Override
    public Optional<Categoria> obtenerCategoriaPorId(UUID id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public Categoria crearCategoria(CreateCategoriaDTO dto) {
        if (categoriaRepository.findByNombre(dto.nombre).isPresent()) {
            throw new RuntimeException("La categoría ya existe");
        }

        Categoria categoria = new Categoria(
            dto.nombre,
            dto.descripcion
        );
        return categoriaRepository.save(categoria);

    }

    @Override
    public Categoria actualizarCategoria(UUID id, CreateCategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if(dto.nombre != null && !dto.nombre.isBlank()) {
            Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(dto.nombre);

            if (categoriaExistente.isPresent() && !categoriaExistente.get().getId().equals(id)) {
                throw new RuntimeException("El nombre de la categoría ya está en uso");
            }
        }

        categoria.setNombre(dto.nombre);
        categoria.setDescripcion(dto.descripcion);

        return categoriaRepository.save(categoria);
    }

    @Override
    public void eliminarCategoria(UUID id) {
        if (!categoriaRepository.findById(id).isPresent()) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoriaRepository.deleteById(id);
    }
    
}

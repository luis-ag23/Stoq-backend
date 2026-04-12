package com.Proyecto.stoq.application.services;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.dto.CreateCategoriaDTO;


public interface CategoriaService {
    List<Categoria> obtenerCategorias();

    Optional<Categoria> obtenerCategoriaPorId(UUID id);

    Categoria crearCategoria(CreateCategoriaDTO dto);

    Categoria actualizarCategoria(UUID id, CreateCategoriaDTO dto);

    void eliminarCategoria(UUID id);
}

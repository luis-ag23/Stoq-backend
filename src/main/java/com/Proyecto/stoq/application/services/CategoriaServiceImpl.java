package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.dto.CreateCategoriaDTO;

@Service
public class CategoriaServiceImpl implements CategoriaService{

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final CategoriaRepositoryPort categoriaRepository;
    private final AuditService auditService;

    public CategoriaServiceImpl(CategoriaRepositoryPort categoriaRepository, AuditService auditService) {
        this.categoriaRepository = categoriaRepository;
        this.auditService = auditService;
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

        logger.info("{} CREATE Categoria | nombre={}", BIZ_TAG, dto.nombre);
        Categoria categoria = new Categoria(
            dto.nombre,
            dto.descripcion
        );
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        auditService.registrarAuditoria("Categoria", "CREATE", categoriaGuardada.getId(), null, snapshotCategoria(categoriaGuardada));
        return categoriaGuardada;

    }

    @Override
    public Categoria actualizarCategoria(UUID id, CreateCategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Map<String, Object> estadoAnterior = snapshotCategoria(categoria);

        if(dto.nombre != null && !dto.nombre.isBlank()) {
            Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(dto.nombre);

            if (categoriaExistente.isPresent() && !categoriaExistente.get().getId().equals(id)) {
                throw new RuntimeException("El nombre de la categoría ya está en uso");
            }
        }

        categoria.setNombre(dto.nombre);
        categoria.setDescripcion(dto.descripcion);

        logger.info("{} UPDATE Categoria | id={} | nuevoNombre={}", BIZ_TAG, id, dto.nombre);
        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        auditService.registrarAuditoria("Categoria", "UPDATE", id, estadoAnterior, snapshotCategoria(categoriaActualizada));
        return categoriaActualizada;
    }

    @Override
    public void eliminarCategoria(UUID id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        if (!categoria.isPresent()) {
            throw new RuntimeException("Categoría no encontrada");
        }

        logger.info("{} DELETE Categoria | id={}", BIZ_TAG, id);
        auditService.registrarAuditoria("Categoria", "DELETE", id, snapshotCategoria(categoria.get()));
        categoriaRepository.deleteById(id);
    }

    private Map<String, Object> snapshotCategoria(Categoria categoria) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", categoria.getId());
        snapshot.put("nombre", categoria.getNombre());
        snapshot.put("descripcion", categoria.getDescripcion());
        snapshot.put("estado", categoria.getEstado());
        return snapshot;
    }
    
}

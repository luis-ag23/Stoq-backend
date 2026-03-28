package com.Proyecto.stoq.adapters.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Proyecto.stoq.application.services.CategoriaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.dto.CreateCategoriaDTO;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/api/categorias")
public class CategoriasController {
    private final CategoriaService categoriaService;

    public CategoriasController(CategoriaService categoriaService){
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> obtenerCategorias() {
        return categoriaService.obtenerCategorias();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable UUID id) {
        Optional<Categoria> categoria = categoriaService.obtenerCategoriaPorId(id);
        if (categoria.isPresent()) {
            return ResponseEntity.ok(categoria.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public Categoria crearCategoria(@RequestBody CreateCategoriaDTO dto) {
        return categoriaService.crearCategoria(dto);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable UUID id, @RequestBody CreateCategoriaDTO dto) {
        try {
            Categoria categoria = categoriaService.actualizarCategoria(id, dto);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable UUID id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

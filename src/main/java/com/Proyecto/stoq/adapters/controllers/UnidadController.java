package com.Proyecto.stoq.adapters.controllers;

import java.util.List; 
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Proyecto.stoq.application.services.UnidadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.Proyecto.stoq.domain.model.Unidad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.Proyecto.stoq.dto.CreateUnidadDTO;


@RestController
@RequestMapping("/api/unidades")
public class UnidadController {
    private final UnidadService unidadService;

    public UnidadController(UnidadService unidadService) {
        this.unidadService = unidadService;
    }

    @GetMapping
    public List<Unidad> listado_unidades() {
        return unidadService.obtenerUnidades();
    }

    @PostMapping
    public Unidad crear_unidad(@RequestBody CreateUnidadDTO dto) {
        return unidadService.crearUnidad(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unidad> actualizar_unidad(@PathVariable UUID id, @RequestBody CreateUnidadDTO dto) {
        return ResponseEntity.ok(unidadService.actualizarUnidad(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar_unidad(@PathVariable UUID id) {
        unidadService.eliminarUnidad(id);
        return ResponseEntity.noContent().build();
    }
    
    

}

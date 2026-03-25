package com.Proyecto.stoq.adapters.controllers;

import java.util.List; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Proyecto.stoq.application.services.UnidadService;
import org.springframework.web.bind.annotation.GetMapping;
import com.Proyecto.stoq.domain.model.Unidad;
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
    
    

}

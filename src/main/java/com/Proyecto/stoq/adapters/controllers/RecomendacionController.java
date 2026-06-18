package com.Proyecto.stoq.adapters.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.RecomendacionService;
import com.Proyecto.stoq.dto.RecomendacionAutomaticaDTO;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping
    public ResponseEntity<List<RecomendacionAutomaticaDTO>> obtenerRecomendaciones(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RecomendacionAutomaticaDTO> recomendaciones = 
                recomendacionService.obtenerRecomendaciones(authentication.getName());

        return ResponseEntity.ok(recomendaciones);
    }
}

package com.Proyecto.stoq.adapters.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.AlertaService;
import com.Proyecto.stoq.dto.AlertaResponseDTO;
import com.Proyecto.stoq.dto.AlertasResumenDTO;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<AlertaResponseDTO> obtenerAlertas() {
        return alertaService.obtenerAlertas().stream()
                .map(AlertaResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/resumen")
    public AlertasResumenDTO obtenerResumen() {
        return alertaService.obtenerResumen();
    }

    @PutMapping("/{id}/marcar-leida")
    public AlertaResponseDTO marcarComoLeida(@PathVariable UUID id) {
        return AlertaResponseDTO.fromEntity(alertaService.marcarComoLeida(id));
    }

    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        alertaService.marcarTodasComoLeidas();
        return ResponseEntity.noContent().build();
    }
}
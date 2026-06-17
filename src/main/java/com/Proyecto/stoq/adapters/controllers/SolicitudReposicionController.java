package com.Proyecto.stoq.adapters.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.Proyecto.stoq.application.services.SolicitudReposicionService;
import com.Proyecto.stoq.dto.CreateSolicitudReposicionDTO;
import com.Proyecto.stoq.dto.SolicitudReposicionResponseDTO;
import com.Proyecto.stoq.dto.UpdateSolicitudEstadoDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudReposicionController {

    private final SolicitudReposicionService solicitudService;

    public SolicitudReposicionController(SolicitudReposicionService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudReposicionResponseDTO>> obtenerSolicitudes(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                solicitudService.obtenerSolicitudesPorEmpresa(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<SolicitudReposicionResponseDTO> crearSolicitudManual(
            Authentication authentication,
            @Valid @RequestBody CreateSolicitudReposicionDTO dto
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SolicitudReposicionResponseDTO response =
                solicitudService.crearSolicitudManual(authentication.getName(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/generar")
    public ResponseEntity<Void> generarSolicitudes(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        solicitudService.generarSolicitudesAutomaticas(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudReposicionResponseDTO> actualizarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSolicitudEstadoDTO dto,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SolicitudReposicionResponseDTO response =
                solicitudService.actualizarEstadoSolicitud(id, authentication.getName(), dto.estado());

        return ResponseEntity.ok(response);
    }
}
package com.Proyecto.stoq.adapters.controllers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.MovimientoInventarioService;
import com.Proyecto.stoq.dto.CreateMovimientoInventarioDTO;
import com.Proyecto.stoq.dto.MovimientoInventarioResponseDTO;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public List<MovimientoInventarioResponseDTO> obtenerMovimientos() {
        return movimientoService.obtenerMovimientos().stream()
                .map(MovimientoInventarioResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/rango")
    public List<MovimientoInventarioResponseDTO> obtenerMovimientosPorRango(
            @RequestParam String inicio,
            @RequestParam String fin
    ) {
        LocalDateTime fechaInicio = parseDateTimeParam(inicio);
        LocalDateTime fechaFin = parseDateTimeParam(fin);

        return movimientoService.obtenerMovimientosPorRango(fechaInicio, fechaFin).stream()
                .map(MovimientoInventarioResponseDTO::fromEntity)
                .toList();
    }

    private LocalDateTime parseDateTimeParam(String value) {
        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception ignored) {
            return LocalDateTime.parse(value);
        }
    }

    @PostMapping
    public ResponseEntity<MovimientoInventarioResponseDTO> crearMovimiento(
            @Valid @RequestBody CreateMovimientoInventarioDTO dto,
            Authentication authentication
    ) {
        String correoUsuario = authentication.getName();

        var movimiento = movimientoService.registrarMovimiento(correoUsuario, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MovimientoInventarioResponseDTO.fromEntity(movimiento));
    }
}
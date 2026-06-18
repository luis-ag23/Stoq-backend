package com.Proyecto.stoq.adapters.controllers;

import com.Proyecto.stoq.adapters.repositories.AuditLogRepository;
import com.Proyecto.stoq.domain.model.AuditLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Obtener todos los logs de auditoría
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> obtenerTodosLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    /**
     * Obtener logs por entidad (ej: Usuario, Producto)
     */
    @GetMapping("/entidad/{entidad}")
    public ResponseEntity<List<AuditLog>> obtenerLogsPorEntidad(@PathVariable String entidad) {
        return ResponseEntity.ok(auditLogRepository.findByEntidad(entidad));
    }

    /**
     * Obtener logs por usuario que realizó la acción
     */
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<AuditLog>> obtenerLogsPorUsuario(@PathVariable String usuario) {
        return ResponseEntity.ok(auditLogRepository.findByCreatedBy(usuario));
    }

    /**
     * Obtener logs de un registro específico
     */
    @GetMapping("/registro/{entidad}/{idRegistro}")
    public ResponseEntity<List<AuditLog>> obtenerLogsPorRegistro(
            @PathVariable String entidad,
            @PathVariable UUID idRegistro) {
        return ResponseEntity.ok(auditLogRepository.findByEntidadAndIdRegistro(entidad, idRegistro));
    }

    /**
     * Obtener logs en un rango de fechas
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/rango")
    public ResponseEntity<List<AuditLog>> obtenerLogsPorFechas(
            @RequestParam String inicio,
            @RequestParam String fin) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime fechaInicio = LocalDateTime.parse(inicio, formatter);
            LocalDateTime fechaFin = LocalDateTime.parse(fin, formatter);
            
            List<AuditLog> logs = auditLogRepository.findByCreatedDateBetween(fechaInicio, fechaFin);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener un log específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> obtenerLogPorId(@PathVariable UUID id) {
        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

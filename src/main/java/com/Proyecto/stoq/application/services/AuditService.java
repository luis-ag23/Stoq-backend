package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.adapters.repositories.AuditLogRepository;
import com.Proyecto.stoq.domain.model.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra una operación de auditoría
     *
     * @param entidad Nombre de la entidad (ej: Usuario, Producto)
     * @param operacion Tipo de operación (CREATE, UPDATE, DELETE, etc.)
     * @param idRegistro ID del registro afectado
     * @param cambiosAnterior Valor anterior (JSON)
     * @param cambiosNuevo Valor nuevo (JSON)
     */
    public void registrarAuditoria(String entidad, String operacion, UUID idRegistro,
                                   Object cambiosAnterior, Object cambiosNuevo) {
        try {
            String ipOrigen = obtenerIpOrigen();
            String userAgent = obtenerUserAgent();
            String endpoint = obtenerEndpoint();

            String cambiosAnteriorJson = cambiosAnterior != null ? 
                    objectMapper.writeValueAsString(cambiosAnterior) : null;
            String cambiosNuevoJson = cambiosNuevo != null ? 
                    objectMapper.writeValueAsString(cambiosNuevo) : null;

            AuditLog auditLog = new AuditLog(
                    entidad,
                    operacion,
                    idRegistro,
                    cambiosAnteriorJson,
                    cambiosNuevoJson,
                    ipOrigen,
                    userAgent,
                    endpoint
            );

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // No lanzar excepción si la auditoría falla, solo registrar en logs
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    /**
     * Registra una operación sin cambios específicos (útil para DELETE)
     */
    public void registrarAuditoria(String entidad, String operacion, UUID idRegistro, Object antes) {
        registrarAuditoria(entidad, operacion, idRegistro, antes, null);
    }

    /**
     * Obtiene la IP de origen de la solicitud
     */
    private String obtenerIpOrigen() {
        try {
            HttpServletRequest request = 
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
            return clientIp;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Obtiene el User-Agent de la solicitud
     */
    private String obtenerUserAgent() {
        try {
            HttpServletRequest request = 
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Obtiene el endpoint de la solicitud
     */
    private String obtenerEndpoint() {
        try {
            HttpServletRequest request = 
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Obtiene el usuario autenticado actual
     */
    public String obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Silenciar excepción
        }
        return "SYSTEM";
    }
}

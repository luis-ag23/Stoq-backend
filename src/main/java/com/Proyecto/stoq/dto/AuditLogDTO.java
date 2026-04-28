package com.Proyecto.stoq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogDTO {
    private UUID id;
    private String entidad;
    private String operacion;
    private UUID idRegistro;
    private String cambiosAnterior;
    private String cambiosNuevo;
    private String usuario;
    private LocalDateTime fecha;
    private String ipOrigen;
    private String userAgent;
    private String endpoint;

    public AuditLogDTO() {}

    public AuditLogDTO(UUID id, String entidad, String operacion, UUID idRegistro,
                       String cambiosAnterior, String cambiosNuevo, String usuario,
                       LocalDateTime fecha, String ipOrigen, String userAgent, String endpoint) {
        this.id = id;
        this.entidad = entidad;
        this.operacion = operacion;
        this.idRegistro = idRegistro;
        this.cambiosAnterior = cambiosAnterior;
        this.cambiosNuevo = cambiosNuevo;
        this.usuario = usuario;
        this.fecha = fecha;
        this.ipOrigen = ipOrigen;
        this.userAgent = userAgent;
        this.endpoint = endpoint;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public UUID getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(UUID idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getCambiosAnterior() {
        return cambiosAnterior;
    }

    public void setCambiosAnterior(String cambiosAnterior) {
        this.cambiosAnterior = cambiosAnterior;
    }

    public String getCambiosNuevo() {
        return cambiosNuevo;
    }

    public void setCambiosNuevo(String cambiosNuevo) {
        this.cambiosNuevo = cambiosNuevo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "AuditLogDTO{" +
                "id=" + id +
                ", entidad='" + entidad + '\'' +
                ", operacion='" + operacion + '\'' +
                ", idRegistro=" + idRegistro +
                ", usuario='" + usuario + '\'' +
                ", fecha=" + fecha +
                ", ipOrigen='" + ipOrigen + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}

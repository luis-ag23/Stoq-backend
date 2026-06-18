package com.Proyecto.stoq.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String entidad;

    @Column(nullable = false)
    private String operacion;

    @Column(nullable = false)
    private UUID idRegistro;

    @Column(columnDefinition = "TEXT")
    private String cambiosAnterior;

    @Column(columnDefinition = "TEXT")
    private String cambiosNuevo;

    @CreatedBy
    @Column(nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private String ipOrigen;

    @Column
    private String userAgent;

    @Column
    private String endpoint;

    public AuditLog() {}

    public AuditLog(String entidad, String operacion, UUID idRegistro, 
                   String cambiosAnterior, String cambiosNuevo, 
                   String ipOrigen, String userAgent, String endpoint) {
        this.entidad = entidad;
        this.operacion = operacion;
        this.idRegistro = idRegistro;
        this.cambiosAnterior = cambiosAnterior;
        this.cambiosNuevo = cambiosNuevo;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
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
}

package com.Proyecto.stoq.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitudes_reposicion")
public class SolicitudReposicion {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_recomendada", nullable = false)
    private Integer cantidadRecomendada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadSolicitud prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(name = "consumo_promedio_diario", nullable = false)
    private Double consumoPromedioDiario;

    @Column(name = "tiempo_agotamiento", nullable = false)
    private Integer tiempoAgotamiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RotacionProducto rotacion;

    @Column(nullable = false)
    private String empresa;

    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public SolicitudReposicion() {}

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidadRecomendada() {
        return cantidadRecomendada;
    }

    public void setCantidadRecomendada(Integer cantidadRecomendada) {
        this.cantidadRecomendada = cantidadRecomendada;
    }

    public PrioridadSolicitud getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadSolicitud prioridad) {
        this.prioridad = prioridad;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Double getConsumoPromedioDiario() {
        return consumoPromedioDiario;
    }

    public void setConsumoPromedioDiario(Double consumoPromedioDiario) {
        this.consumoPromedioDiario = consumoPromedioDiario;
    }

    public Integer getTiempoAgotamiento() {
        return tiempoAgotamiento;
    }

    public void setTiempoAgotamiento(Integer tiempoAgotamiento) {
        this.tiempoAgotamiento = tiempoAgotamiento;
    }

    public RotacionProducto getRotacion() {
        return rotacion;
    }

    public void setRotacion(RotacionProducto rotacion) {
        this.rotacion = rotacion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

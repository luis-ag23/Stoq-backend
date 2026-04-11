package com.Proyecto.stoq.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "ubicacion")
    private String ubicacion;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "stock_actual")
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(nullable = false)
    private boolean estado = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = true;
    }
    public Producto() {}

    public Producto(String codigo, String nombre, String ubicacion, Categoria categoria, Unidad unidad, Integer stockMinimo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.categoria = categoria;
        this.unidad = unidad;
        this.stockMinimo = stockMinimo;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public Boolean getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

package com.Proyecto.stoq.domain.model;

import java.util.UUID;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "vw_productos_criticos")
public class ProductoCriticoView {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "diferencia")
    private Integer diferencia;

    @Column(name = "nivel_alerta")
    private String nivelAlerta;

    @Column(name = "categoria_nombre")
    private String categoriaNombre;

    @Column(name = "unidad_abreviatura")
    private String unidadAbreviatura;

    protected ProductoCriticoView() {
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

    public Integer getStockActual() {
        return stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public Integer getDiferencia() {
        return diferencia;
    }

    public String getNivelAlerta() {
        return nivelAlerta;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public String getUnidadAbreviatura() {
        return unidadAbreviatura;
    }
}
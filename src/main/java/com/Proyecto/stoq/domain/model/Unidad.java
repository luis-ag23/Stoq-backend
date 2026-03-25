package com.Proyecto.stoq.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidades")
public class Unidad {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true )
    private String nombre;

    @Column(nullable = false,unique = true)
    private String abreviatura;

    @Column(nullable = false)
    private boolean estado = true;

    public Unidad() {}

    public Unidad(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }   

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

package com.Proyecto.stoq.Entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue
    private UUID id;

    private String nombre;

    private String descripcion;

    public Rol() {}

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
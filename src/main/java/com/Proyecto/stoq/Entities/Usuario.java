package com.Proyecto.stoq.Entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    private String nombre;

    private String correo;

    @Column(name = "contrasena_hash")
    private String contrasenaHash;

    private Boolean estado;

    private String rol;

    public Usuario() {}

    public Usuario(String nombre, String correo, String contrasenaHash, Boolean estado, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.estado = estado;
        this.rol = rol;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Boolean getEstado() {
        return estado;
    }

    public String getRol() {
        return rol;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
package com.Proyecto.stoq.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(nullable = false)
    private Boolean estado = true;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public Usuario() {}

    public Usuario(String nombre, String correo,String empresa, String contrasenaHash, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.empresa = empresa;
        this.contrasenaHash = contrasenaHash;
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

    public String getEmpresa() {
        return empresa;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Boolean getEstado() {
        return estado;
    }

    public Rol getRol() {
        return rol;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
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

    public void setRol(Rol rol){
        this.rol = rol;
    }
}
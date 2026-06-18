package com.Proyecto.stoq.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class CreateProductDTO {
    @Size(max = 255, message = "El código no puede exceder los 255 caracteres")
    public String codigo;

    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    public String nombre;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    public String ubicacion;
    public UUID categoriaId;
    public UUID unidadId;

    @Min(value = 1, message = "El stock inicial debe ser mayor que 0")
    public int stock_inicial;

    @Min(value = 1, message = "El stock minimo debe ser mayor que 0")
    public int stock_minimo;
}

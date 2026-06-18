package com.Proyecto.stoq.dto;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;


public class UpdateProductDTO {
    @Size(max = 255, message = "El código no puede exceder los 255 caracteres")
    public String codigo;

    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    public String nombre;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    public String ubicacion;
    public UUID categoriaId;
    public UUID unidadId;

    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    public Integer stock_minimo;
}

package com.Proyecto.stoq.dto;
import java.util.UUID;


public class UpdateProductDTO {
    public String codigo;
    public String nombre;
    public String ubicacion;
    public UUID categoriaId;
    public UUID unidadId;
    public Integer stock_minimo;
}

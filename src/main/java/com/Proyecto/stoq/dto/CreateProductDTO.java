package com.Proyecto.stoq.dto;

import java.util.UUID;

public class CreateProductDTO {
    public String codigo;
    public String nombre;
    public UUID categoriaId;
    public UUID unidadId;
    public int stock_minimo;
}

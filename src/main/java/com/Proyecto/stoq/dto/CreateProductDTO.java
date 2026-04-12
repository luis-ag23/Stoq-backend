package com.Proyecto.stoq.dto;

import java.util.UUID;

public class CreateProductDTO {
    public String codigo;
    public String nombre;
    public String ubicacion;
    public UUID categoriaId;
    public UUID unidadId;
    public int stock_inicial;
    public int stock_minimo;
}

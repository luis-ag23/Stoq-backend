package com.Proyecto.stoq.application.usecases;

import java.util.List;

import com.Proyecto.stoq.dto.ProductoCriticoResponse;

public interface ObtenerProductosCriticosUseCase {

    List<ProductoCriticoResponse> obtenerProductosCriticos();
}
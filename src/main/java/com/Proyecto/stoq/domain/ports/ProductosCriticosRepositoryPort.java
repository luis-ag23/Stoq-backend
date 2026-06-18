package com.Proyecto.stoq.domain.ports;

import java.util.List;

import com.Proyecto.stoq.domain.model.ProductoCriticoView;

public interface ProductosCriticosRepositoryPort {

    List<ProductoCriticoView> findAll();
}
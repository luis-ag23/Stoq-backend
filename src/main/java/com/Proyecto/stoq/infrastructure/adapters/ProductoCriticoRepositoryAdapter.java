package com.Proyecto.stoq.infrastructure.adapters;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.Proyecto.stoq.domain.model.ProductoCriticoView;
import com.Proyecto.stoq.domain.ports.ProductosCriticosRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.ProductosCriticosViewRepository;

@Repository
public class ProductoCriticoRepositoryAdapter implements ProductosCriticosRepositoryPort {

    private final ProductosCriticosViewRepository productosCriticosViewRepository;

    public ProductoCriticoRepositoryAdapter(ProductosCriticosViewRepository productosCriticosViewRepository) {
        this.productosCriticosViewRepository = productosCriticosViewRepository;
    }

    @Override
    public List<ProductoCriticoView> findAll() {
        return productosCriticosViewRepository.findAllByOrderByStockActualAsc();
    }
}
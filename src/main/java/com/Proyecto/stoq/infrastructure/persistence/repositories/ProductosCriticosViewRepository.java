package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.stoq.domain.model.ProductoCriticoView;

public interface ProductosCriticosViewRepository extends JpaRepository<ProductoCriticoView, UUID> {

    List<ProductoCriticoView> findAllByOrderByStockActualAsc();
}
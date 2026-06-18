package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.Proyecto.stoq.domain.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {

    @EntityGraph(attributePaths = {"producto", "producto.categoria"})
    java.util.List<Alerta> findAll();

    @EntityGraph(attributePaths = {"producto", "producto.categoria"})
    java.util.Optional<Alerta> findById(UUID id);

    boolean existsByProductoIdAndTipoAndLeidaFalse(UUID productoId, String tipo);

    long countByLeidaFalse();
}
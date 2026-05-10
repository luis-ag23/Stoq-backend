package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.stoq.domain.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {

    boolean existsByProductoIdAndTipoAndLeidaFalse(UUID productoId, String tipo);

    long countByLeidaFalse();
}
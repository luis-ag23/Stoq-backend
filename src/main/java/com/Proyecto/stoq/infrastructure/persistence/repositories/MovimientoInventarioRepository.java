package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;

public interface MovimientoInventarioRepository extends JpaRepository<Movimiento_Inventario, UUID> {
    List<Movimiento_Inventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento_Inventario> findAllByOrderByFechaMovimientoDesc();
}
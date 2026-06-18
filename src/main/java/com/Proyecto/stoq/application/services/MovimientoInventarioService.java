package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.util.List;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.dto.CreateMovimientoInventarioDTO;

public interface MovimientoInventarioService {
    List<Movimiento_Inventario> obtenerMovimientos();

    List<Movimiento_Inventario> obtenerMovimientosPorRango(LocalDateTime inicio, LocalDateTime fin);

    Movimiento_Inventario registrarMovimiento(String correoUsuario, CreateMovimientoInventarioDTO dto);

    
}
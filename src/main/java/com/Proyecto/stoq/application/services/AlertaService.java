package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.AlertasResumenDTO;

public interface AlertaService {

    void verificarStockBajo(Producto producto);

    void verificarCambioStock(Producto producto, Integer stockAnterior, Integer stockResultante);

    List<Alerta> obtenerAlertas();

    AlertasResumenDTO obtenerResumen();

    void verificarRiesgosInventario(Producto producto);

    void evaluarRiesgosInventarioProgramado();

    Alerta marcarComoLeida(UUID id);

    void marcarTodasComoLeidas();
}
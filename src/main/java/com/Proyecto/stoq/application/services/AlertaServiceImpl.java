package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.dto.AlertasResumenDTO;

@Service
public class AlertaServiceImpl implements AlertaService {

    private static final String TIPO_STOCK_BAJO = "STOCK_BAJO";

    private final AlertaRepositoryPort alertaRepository;
    private final ProductosRepositoryPort productoRepository;

    public AlertaServiceImpl(
            AlertaRepositoryPort alertaRepository,
            ProductosRepositoryPort productoRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public void verificarStockBajo(Producto producto) {
        if (producto == null || producto.getId() == null) {
            return;
        }

        if (producto.getStockActual() >= producto.getStockMinimo()) {
            return;
        }

        boolean yaExiste = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(
                producto.getId(),
                TIPO_STOCK_BAJO
        );

        if (yaExiste) {
            return;
        }

        String mensaje = producto.getCodigo() + " - " + producto.getNombre()
                + " está por debajo del stock mínimo";

        Alerta alerta = new Alerta(
                TIPO_STOCK_BAJO,
                mensaje,
                producto
        );

        alertaRepository.save(alerta);
    }

    @Override
    public List<Alerta> obtenerAlertas() {
        return alertaRepository.findAll();
    }

    @Override
    public AlertasResumenDTO obtenerResumen() {
        List<Producto> productos = productoRepository.findAll();

        long productosCriticos = productos.stream()
                .filter(producto -> producto.getStockActual() < producto.getStockMinimo())
                .count();

        long notificacionesSinLeer = alertaRepository.countByLeidaFalse();
        long totalAlertas = alertaRepository.findAll().size();

        return new AlertasResumenDTO(
                productosCriticos,
                notificacionesSinLeer,
                totalAlertas
        );
    }

    @Override
    @Transactional
    public Alerta marcarComoLeida(UUID id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));

        alerta.setLeida(true);
        return alertaRepository.save(alerta);
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas() {
        List<Alerta> alertas = alertaRepository.findAll();

        for (Alerta alerta : alertas) {
            alerta.setLeida(true);
            alertaRepository.save(alerta);
        }
    }
}
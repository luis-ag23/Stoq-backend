package com.Proyecto.stoq.application.services;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.dto.AlertasResumenDTO;

@Service
public class AlertaServiceImpl implements AlertaService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";
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
            logger.warn("{} ALERTA omitida | producto nulo o sin id", BIZ_TAG);
            return;
        }

        Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;

        if (stockActual >= stockMinimo) {
            logger.debug(
                    "{} ALERTA omitida | productoId={} | codigo={} | stockActual={} | stockMinimo={}",
                    BIZ_TAG,
                    producto.getId(),
                    producto.getCodigo(),
                    stockActual,
                    stockMinimo
            );
            return;
        }

        boolean yaExiste = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(
                producto.getId(),
                TIPO_STOCK_BAJO
        );

        if (yaExiste) {
            logger.info(
                    "{} ALERTA STOCK_BAJO ya existente | productoId={} | codigo={}",
                    BIZ_TAG,
                    producto.getId(),
                    producto.getCodigo()
            );
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

        logger.info(
                "{} ALERTA STOCK_BAJO creada | productoId={} | codigo={} | stockActual={} | stockMinimo={}",
                BIZ_TAG,
                producto.getId(),
                producto.getCodigo(),
                stockActual,
                stockMinimo
        );
    }

    @Override
    @Transactional
    public void verificarCambioStock(Producto producto, Integer stockAnterior, Integer stockResultante) {
        if (producto == null || producto.getId() == null) {
            logger.warn("{} ALERTA omitida | producto nulo o sin id", BIZ_TAG);
            return;
        }

        int actual = stockResultante != null ? stockResultante : 0;
        int minimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;

        // Priorizar alerta STOCK_CERO cuando el stock llega a 0
        if (actual == 0) {
            boolean existeCero = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), "STOCK_CERO");
            if (existeCero) {
                logger.info("{} ALERTA STOCK_CERO ya existente | productoId={} | codigo={}", BIZ_TAG, producto.getId(), producto.getCodigo());
                return;
            }

            String mensaje = producto.getCodigo() + " - " + producto.getNombre() + " alcanzó stock cero";
            Alerta alerta = new Alerta("STOCK_CERO", mensaje, producto);
            alertaRepository.save(alerta);

            logger.info("{} ALERTA STOCK_CERO creada | productoId={} | codigo={} | stockActual=0", BIZ_TAG, producto.getId(), producto.getCodigo());
            return;
        }

        // Si antes ya estaba por debajo o en mínimo y sigue así, evitar duplicados
        if (stockAnterior != null && stockAnterior <= minimo && actual <= minimo) {
            logger.info("{} ALERTA omitida | productoId={} | codigo={} | ya crítico", BIZ_TAG, producto.getId(), producto.getCodigo());
            return;
        }

        // Si llega al mínimo (o por debajo) desde encima del mínimo -> STOCK_BAJO
        if ((stockAnterior == null || stockAnterior > minimo) && actual <= minimo) {
            boolean existe = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), TIPO_STOCK_BAJO);
            if (existe) {
                logger.info("{} ALERTA STOCK_BAJO ya existente | productoId={} | codigo={}", BIZ_TAG, producto.getId(), producto.getCodigo());
                return;
            }

            String mensaje = producto.getCodigo() + " - " + producto.getNombre() + " está por debajo del stock mínimo";
            Alerta alerta = new Alerta(TIPO_STOCK_BAJO, mensaje, producto);
            alertaRepository.save(alerta);

            logger.info("{} ALERTA STOCK_BAJO creada | productoId={} | codigo={} | stockActual={} | stockMinimo={}",
                    BIZ_TAG,
                    producto.getId(),
                    producto.getCodigo(),
                    actual,
                    minimo
            );
        }
    }

    @Override
    public List<Alerta> obtenerAlertas() {
        return alertaRepository.findAll();
    }

    @Override
    public AlertasResumenDTO obtenerResumen() {
        List<Producto> productos = productoRepository.findAll();
        if (productos == null) {
            productos = Collections.emptyList();
        }

        long productosCriticos = productos.stream()
        .filter(producto -> Boolean.TRUE.equals(producto.getEstado()))
        .filter(producto -> {
            Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
            Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;
            return stockActual < stockMinimo;
        })
        .count();

        long notificacionesSinLeer = alertaRepository.countByLeidaFalse();
        List<Alerta> alertas = alertaRepository.findAll();
        long totalAlertas = alertas != null ? alertas.size() : 0L;

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
        Alerta alertaActualizada = alertaRepository.save(alerta);

        logger.info("{} ALERTA marcada como leída | alertaId={}", BIZ_TAG, id);

        return alertaActualizada;
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas() {
        List<Alerta> alertas = alertaRepository.findAll();

        for (Alerta alerta : alertas) {
            alerta.setLeida(true);
            alertaRepository.save(alerta);
        }

        logger.info("{} ALERTAS marcadas como leídas | total={}", BIZ_TAG, alertas.size());
    }
}
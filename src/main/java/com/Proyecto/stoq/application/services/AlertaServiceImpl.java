package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.AlertaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.AlertasResumenDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;

@Service
public class AlertaServiceImpl implements AlertaService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";
    private static final String TIPO_STOCK_BAJO = "STOCK_BAJO";
    private static final String TIPO_STOCK_CERO = "STOCK_CERO";
    private static final String TIPO_RIESGO_AGOTAMIENTO = "RIESGO_AGOTAMIENTO";
    private static final String TIPO_CONSUMO_ANORMAL = "CONSUMO_ANORMAL";
    private static final String TIPO_BAJA_ROTACION = "BAJA_ROTACION_PROLONGADA";
    private static final int VENTANA_RIESGO_DIAS = 30;
    private static final int VENTANA_CONSUMO_DIAS = 7;
    private static final int VENTANA_ROTACION_DIAS = 60;
    private static final int DIAS_COBERTURA_RIESGO = 7;
    private static final double FACTOR_CONSUMO_ANORMAL = 1.75d;

    private final AlertaRepositoryPort alertaRepository;
    private final ProductosRepositoryPort productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public AlertaServiceImpl(
            AlertaRepositoryPort alertaRepository,
            ProductosRepositoryPort productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            UsuarioRepositoryPort usuarioRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.usuarioRepository = usuarioRepository;
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

        // Si antes ya estaba por debajo o en mínimo y sigue así, evitar duplicados,
        // pero permitir el salto a stock cero para generar la alerta prioritaria.
        if (actual != 0 && stockAnterior != null && stockAnterior <= minimo && actual <= minimo) {
            logger.info("{} ALERTA omitida | productoId={} | codigo={} | ya crítico", BIZ_TAG, producto.getId(), producto.getCodigo());
            return;
        }

        // Priorizar alerta STOCK_CERO cuando el stock llega a 0
        if (actual == 0) {
            boolean existeCero = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), TIPO_STOCK_CERO);
            if (existeCero) {
                logger.info("{} ALERTA STOCK_CERO ya existente | productoId={} | codigo={}", BIZ_TAG, producto.getId(), producto.getCodigo());
                return;
            }

            String mensaje = producto.getCodigo() + " - " + producto.getNombre() + " alcanzó stock cero";
            Alerta alerta = new Alerta(TIPO_STOCK_CERO, mensaje, producto);
            alertaRepository.save(alerta);

            logger.info("{} ALERTA STOCK_CERO creada | productoId={} | codigo={} | stockActual=0", BIZ_TAG, producto.getId(), producto.getCodigo());
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
    @Transactional
    public void verificarRiesgosInventario(Producto producto) {
        if (producto == null || producto.getId() == null) {
            logger.warn("{} RIESGO omitido | producto nulo o sin id", BIZ_TAG);
            return;
        }

        Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;
        if (stockActual <= 0) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime inicioRiesgo = ahora.minusDays(VENTANA_RIESGO_DIAS);
        LocalDateTime inicioConsumo = ahora.minusDays(VENTANA_CONSUMO_DIAS);
        LocalDateTime inicioRotacion = ahora.minusDays(VENTANA_ROTACION_DIAS);

        long cantidadSalidas30 = safeLong(movimientoInventarioRepository.sumarCantidadSalidasPorProductoEntre(producto.getId(), inicioRiesgo, ahora));
        long salidas7 = safeLong(movimientoInventarioRepository.contarSalidasPorProductoEntre(producto.getId(), inicioConsumo, ahora));
        long cantidadSalidas7 = safeLong(movimientoInventarioRepository.sumarCantidadSalidasPorProductoEntre(producto.getId(), inicioConsumo, ahora));
        long salidas60 = safeLong(movimientoInventarioRepository.contarSalidasPorProductoEntre(producto.getId(), inicioRotacion, ahora));

        double promedioSalidaDiaria = cantidadSalidas30 > 0 ? (double) cantidadSalidas30 / VENTANA_RIESGO_DIAS : 0d;
        double diasCobertura = promedioSalidaDiaria > 0 ? stockActual / promedioSalidaDiaria : Double.POSITIVE_INFINITY;

        if ((promedioSalidaDiaria > 0 && diasCobertura <= DIAS_COBERTURA_RIESGO) || (stockMinimo > 0 && stockActual <= stockMinimo * 2)) {
            crearAlertaCriticaSiNoExiste(
                    producto,
                    TIPO_RIESGO_AGOTAMIENTO,
                    producto.getCodigo() + " - " + producto.getNombre() + " presenta riesgo próximo de agotamiento"
            );
        }

        double consumoEsperado7Dias = promedioSalidaDiaria * VENTANA_CONSUMO_DIAS;
        long umbralAnormal = Math.max(Math.round(consumoEsperado7Dias * FACTOR_CONSUMO_ANORMAL), Math.max(stockMinimo.longValue(), 1L));
        if (salidas7 >= 3 && cantidadSalidas7 >= umbralAnormal) {
            crearAlertaCriticaSiNoExiste(
                    producto,
                    TIPO_CONSUMO_ANORMAL,
                    producto.getCodigo() + " - " + producto.getNombre() + " muestra incremento anormal de consumo"
            );
        }

        if (salidas60 == 0 && stockActual > stockMinimo) {
            crearAlertaCriticaSiNoExiste(
                    producto,
                    TIPO_BAJA_ROTACION,
                    producto.getCodigo() + " - " + producto.getNombre() + " tiene baja rotación prolongada"
            );
        }

        logger.debug(
                "{} RIESGO evaluado | productoId={} | codigo={} | salidas30={} | salidas7={} | salidas60={} | diasCobertura={}",
                BIZ_TAG,
                producto.getId(),
                producto.getCodigo(),
                cantidadSalidas30,
                salidas7,
                salidas60,
                diasCobertura
        );
    }

    @Override
    public List<Alerta> obtenerAlertas() {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return Collections.emptyList();
        }

        return alertaRepository.findAll().stream()
                .filter(alerta -> perteneceAEmpresa(alerta, empresa))
                .toList();
    }

    @Override
    public AlertasResumenDTO obtenerResumen() {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return new AlertasResumenDTO(0, 0, 0);
        }

        List<Producto> productos = productoRepository.findAll();
        if (productos == null) {
            productos = Collections.emptyList();
        }

        long productosCriticos = productos.stream()
        .filter(producto -> perteneceAEmpresa(producto, empresa))
        .filter(producto -> Boolean.TRUE.equals(producto.getEstado()))
        .filter(producto -> {
            Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
            Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;
            return stockActual <= stockMinimo;
        })
        .count();

        long notificacionesSinLeer = alertaRepository.findAll().stream()
                .filter(alerta -> perteneceAEmpresa(alerta, empresa))
                .filter(alerta -> Boolean.FALSE.equals(alerta.getLeida()))
                .count();
        List<Alerta> alertas = alertaRepository.findAll().stream()
                .filter(alerta -> perteneceAEmpresa(alerta, empresa))
                .toList();
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

        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null || !perteneceAEmpresa(alerta, empresa)) {
            throw new RuntimeException("Alerta no encontrada");
        }

        alerta.setLeida(true);
        Alerta alertaActualizada = alertaRepository.save(alerta);

        logger.info("{} ALERTA marcada como leída | alertaId={}", BIZ_TAG, id);

        return alertaActualizada;
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas() {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return;
        }

        List<Alerta> alertas = alertaRepository.findAll().stream()
                .filter(alerta -> perteneceAEmpresa(alerta, empresa))
                .toList();

        for (Alerta alerta : alertas) {
            alerta.setLeida(true);
            alertaRepository.save(alerta);
        }

        logger.info("{} ALERTAS marcadas como leídas | total={}", BIZ_TAG, alertas.size());
    }

    private boolean perteneceAEmpresa(Producto producto, String empresa) {
        if (producto == null || empresa == null || producto.getEmpresa() == null) {
            return false;
        }

        return empresa.equalsIgnoreCase(producto.getEmpresa().trim());
    }

    private boolean perteneceAEmpresa(Alerta alerta, String empresa) {
        return alerta != null && perteneceAEmpresa(alerta.getProducto(), empresa);
    }

    private String obtenerEmpresaAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }

        return usuarioRepository.findByCorreo(authentication.getName())
                .map(Usuario::getEmpresa)
                .map(empresa -> empresa != null ? empresa.trim() : null)
                .filter(empresa -> empresa != null && !empresa.isBlank())
                .orElse(null);
    }

    private void crearAlertaCriticaSiNoExiste(Producto producto, String tipo, String mensaje) {
        boolean existe = alertaRepository.existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), tipo);
        if (existe) {
            logger.info("{} ALERTA {} ya existente | productoId={} | codigo={}", BIZ_TAG, tipo, producto.getId(), producto.getCodigo());
            return;
        }

        alertaRepository.save(new Alerta(tipo, mensaje, producto));
        logger.info("{} ALERTA {} creada | productoId={} | codigo={}", BIZ_TAG, tipo, producto.getId(), producto.getCodigo());
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }
}
package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.RecomendacionAutomaticaDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;

@Service
public class RecomendacionServiceImpl implements RecomendacionService {

    private static final Logger logger = LoggerFactory.getLogger(RecomendacionServiceImpl.class);
    private static final String PREDICTIVE_TAG = "[STOQ-PREDICTIVO]";

    private final ProductosRepositoryPort productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public RecomendacionServiceImpl(
            ProductosRepositoryPort productoRepository,
            MovimientoInventarioRepository movimientoRepository,
            UsuarioRepositoryPort usuarioRepository
    ) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<RecomendacionAutomaticaDTO> obtenerRecomendaciones(String emailUsuario) {
        logger.info("{} Iniciando cálculo de recomendaciones para usuario: {}", PREDICTIVE_TAG, emailUsuario);

        Usuario usuario = usuarioRepository.findByCorreo(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String empresa = usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null;
        if (empresa == null || empresa.isBlank()) {
            logger.warn("{} Usuario {} no tiene empresa asignada.", PREDICTIVE_TAG, emailUsuario);
            return List.of();
        }

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().trim().equalsIgnoreCase(empresa))
                .filter(p -> Boolean.TRUE.equals(p.getEstado()))
                .toList();

        logger.info("{} Se encontraron {} productos activos para la empresa {}", PREDICTIVE_TAG, productos.size(), empresa);

        return productos.stream()
                .map(this::calcularRecomendacionParaProducto)
                .collect(Collectors.toList());
    }

    private RecomendacionAutomaticaDTO calcularRecomendacionParaProducto(Producto producto) {
        UUID productoId = producto.getId();
        String nombre = producto.getNombre();
        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        int stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;

        logger.info("{} Analizando producto: {} (ID: {}) | Stock actual: {} | Stock mínimo: {}", 
                PREDICTIVE_TAG, nombre, productoId, stockActual, stockMinimo);

        List<Movimiento_Inventario> salidas = movimientoRepository.obtenerMovimientosPorProductoYTipo(productoId, "SALIDA");

        double cpd = 0.0;
        int tiempoAgotamiento = 9999;
        String rotacion = "BAJA";

        if (salidas.isEmpty()) {
            logger.warn("{} Sin movimientos históricos de SALIDA para el producto: {}. CPD asignado = 0.0, Tiempo de agotamiento = 9999", 
                    PREDICTIVE_TAG, nombre);
        } else {
            LocalDateTime oldestDate = salidas.get(0).getFechaMovimiento();
            long days = ChronoUnit.DAYS.between(oldestDate, LocalDateTime.now());
            if (days < 1) {
                days = 1;
            }

            long totalSalidas = salidas.stream().mapToLong(Movimiento_Inventario::getCantidad).sum();
            cpd = (double) totalSalidas / days;
            
            if (cpd > 0.0) {
                tiempoAgotamiento = (int) Math.round((double) stockActual / cpd);
            }

            if (cpd >= 1.0) {
                rotacion = "ALTA";
            }

            logger.info("{} Producto: {} | {} salidas en {} días. CPD: {} | Tiempo agotamiento proyectado: {} días | Rotación: {}", 
                    PREDICTIVE_TAG, nombre, totalSalidas, days, cpd, tiempoAgotamiento, rotacion);
        }

        // Cantidad recomendada: (stockMinimo * 2) - stockActual
        int cantidadRecomendada = Math.max(0, (stockMinimo * 2) - stockActual);

        // Prioridad
        String prioridad = "BAJA";
        if (stockActual == 0 || stockActual <= stockMinimo || tiempoAgotamiento <= 3) {
            prioridad = "ALTA";
        } else if (tiempoAgotamiento <= 10) {
            prioridad = "MEDIA";
        }

        logger.info("{} Producto: {} | Cantidad recomendada: {} | Prioridad asignada: {}", 
                PREDICTIVE_TAG, nombre, cantidadRecomendada, prioridad);

        return new RecomendacionAutomaticaDTO(
                productoId,
                producto.getCodigo(),
                nombre,
                stockActual,
                stockMinimo,
                cpd,
                tiempoAgotamiento,
                rotacion,
                cantidadRecomendada,
                prioridad
        );
    }
}

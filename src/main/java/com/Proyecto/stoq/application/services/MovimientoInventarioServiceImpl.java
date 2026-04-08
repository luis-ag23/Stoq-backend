package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.CreateMovimientoInventarioDTO;
import com.Proyecto.stoq.infrastructure.persistence.repositories.MovimientoInventarioRepository;
import com.Proyecto.stoq.security.RoleCatalog;

@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoInventarioServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductosRepositoryPort productoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final AuditService auditService;

    public MovimientoInventarioServiceImpl(
            MovimientoInventarioRepository movimientoRepository,
            ProductosRepositoryPort productoRepository,
            UsuarioRepositoryPort usuarioRepository,
            AuditService auditService
    ) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Override
    public List<Movimiento_Inventario> obtenerMovimientos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc();
    }

    @Override
    public List<Movimiento_Inventario> obtenerMovimientosPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaMovimientoBetween(inicio, fin);
    }

    @Override
    @Transactional
    public Movimiento_Inventario registrarMovimiento(String correoUsuario, CreateMovimientoInventarioDTO dto) {
        Producto producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tipoMovimiento = RoleCatalog.normalize(dto.tipoMovimiento());
        if (!"ENTRADA".equals(tipoMovimiento) && !"SALIDA".equals(tipoMovimiento)) {
            throw new RuntimeException("Tipo de movimiento no valido");
        }

        Integer stockAnterior = producto.getStockActual() != null ? producto.getStockActual() : 0;
        Integer stockResultante = "ENTRADA".equals(tipoMovimiento)
                ? stockAnterior + dto.cantidad()
                : stockAnterior - dto.cantidad();

        if (stockResultante < 0) {
            throw new RuntimeException("El stock no puede quedar en negativo");
        }

        logger.info("{} MOVIMIENTO {} | productoId={} | cantidad={} | stockAnterior={} | stockResultante={} | usuario={}",
            BIZ_TAG,
            tipoMovimiento,
            dto.productoId(),
            dto.cantidad(),
            stockAnterior,
            stockResultante,
            correoUsuario);

        producto.setStockActual(stockResultante);
        productoRepository.save(producto);

        Movimiento_Inventario movimiento = new Movimiento_Inventario();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setMotivo(dto.motivo().trim());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockResultante);

        Movimiento_Inventario movimientoGuardado = movimientoRepository.save(movimiento);
        auditService.registrarAuditoria(
                "MovimientoInventario",
                "CREATE",
                movimientoGuardado.getId(),
                null,
                snapshotMovimiento(movimientoGuardado)
        );
        return movimientoGuardado;
    }

    private Map<String, Object> snapshotMovimiento(Movimiento_Inventario movimiento) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", movimiento.getId());
        snapshot.put("productoId", movimiento.getProducto() != null ? movimiento.getProducto().getId() : null);
        snapshot.put("usuarioId", movimiento.getUsuario() != null ? movimiento.getUsuario().getId() : null);
        snapshot.put("tipoMovimiento", movimiento.getTipoMovimiento());
        snapshot.put("cantidad", movimiento.getCantidad());
        snapshot.put("motivo", movimiento.getMotivo());
        snapshot.put("stockAnterior", movimiento.getStockAnterior());
        snapshot.put("stockResultante", movimiento.getStockResultante());
        snapshot.put("fechaMovimiento", movimiento.getFechaMovimiento());
        return snapshot;
    }
}
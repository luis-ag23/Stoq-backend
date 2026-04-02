package com.Proyecto.stoq.application.services;

import java.time.LocalDateTime;
import java.util.List;

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

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductosRepositoryPort productoRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public MovimientoInventarioServiceImpl(
            MovimientoInventarioRepository movimientoRepository,
            ProductosRepositoryPort productoRepository,
            UsuarioRepositoryPort usuarioRepository
    ) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
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

        return movimientoRepository.save(movimiento);
    }
}
package com.Proyecto.stoq.application.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.dto.CreateMovimientoInventarioDTO;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.UpdateProductDTO;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final ProductosRepositoryPort productoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final UnidadRepositoryPort unidadRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final AuditService auditService;

    public ProductoServiceImpl(ProductosRepositoryPort productoRepository, 
        CategoriaRepositoryPort categoriaRepository, 
        UnidadRepositoryPort unidadRepository,
        MovimientoInventarioService movimientoInventarioService,
        AuditService auditService) 
    {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadRepository = unidadRepository;
        this.movimientoInventarioService = movimientoInventarioService;
        this.auditService = auditService;
    }

    @Override
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(UUID id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional
    public Producto crearProducto(CreateProductDTO dto, String correoUsuario) {
        if (productoRepository.findByCodigo(dto.codigo).isPresent()) {
            throw new RuntimeException("El código del producto ya está registrado");
        }

        Categoria categoria = categoriaRepository.findById(dto.categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Unidad unidad = unidadRepository.findById(dto.unidadId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        Producto producto = new Producto(
                dto.codigo,
                dto.nombre,
                limpiarTexto(dto.ubicacion),
                categoria,
                unidad,
                dto.stock_minimo
        );

        logger.info("{} CREATE Producto | codigo={} | nombre={}", BIZ_TAG, dto.codigo, dto.nombre);
        Producto productoGuardado = productoRepository.save(producto);

        if (dto.stock_inicial > 0) {
            if (correoUsuario == null || correoUsuario.isBlank()) {
                throw new RuntimeException("Usuario autenticado requerido para registrar el stock inicial");
            }

            CreateMovimientoInventarioDTO movimientoInicial = new CreateMovimientoInventarioDTO(
                    productoGuardado.getId(),
                    "ENTRADA",
                    dto.stock_inicial,
                    "Stock inicial del producto"
            );
            Movimiento_Inventario movimiento = movimientoInventarioService.registrarMovimiento(correoUsuario, movimientoInicial);
            productoGuardado = movimiento.getProducto();
        }

        auditService.registrarAuditoria("Producto", "CREATE", productoGuardado.getId(), null, snapshotProducto(productoGuardado));
        return productoGuardado;
    }

    @Override
    public Producto actualizarProducto(UUID id, UpdateProductDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Map<String, Object> estadoAnterior = snapshotProducto(producto);

        if (dto.nombre != null && !dto.nombre.isBlank()) {
            producto.setNombre(dto.nombre);
        }

        if (dto.ubicacion != null) {
            producto.setUbicacion(limpiarTexto(dto.ubicacion));
        }

        if (dto.codigo != null && !dto.codigo.isBlank()) {
            Optional<Producto> productoConMismoCodigo = productoRepository.findByCodigo(dto.codigo);

            if (productoConMismoCodigo.isPresent()
                    && !productoConMismoCodigo.get().getCodigo().equals(dto.codigo)) {
                throw new RuntimeException("El código ya está registrado por otro producto");
            }

            producto.setCodigo(dto.codigo);
        }

        if (dto.categoriaId != null) {
            Categoria categoria = categoriaRepository.findById(dto.categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            producto.setCategoria(categoria);
        }

        if (dto.unidadId != null) {
            Unidad unidad = unidadRepository.findById(dto.unidadId)
                    .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

            producto.setUnidad(unidad);
        }

        if (dto.stock_minimo != null) {
            producto.setStockMinimo(dto.stock_minimo);
        }

        logger.info("{} UPDATE Producto | id={} | codigo={}", BIZ_TAG, id, producto.getCodigo());
        Producto productoActualizado = productoRepository.save(producto);
        auditService.registrarAuditoria("Producto", "UPDATE", id, estadoAnterior, snapshotProducto(productoActualizado));
        return productoActualizado;
    }

    @Override
    public void eliminarProducto(UUID id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (!producto.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }
        logger.info("{} DELETE Producto | id={}", BIZ_TAG, id);
        auditService.registrarAuditoria("Producto", "DELETE", id, snapshotProducto(producto.get()));
        productoRepository.deleteById(id);
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        return texto.trim();
    }

    private Map<String, Object> snapshotProducto(Producto producto) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", producto.getId());
        snapshot.put("codigo", producto.getCodigo());
        snapshot.put("nombre", producto.getNombre());
        snapshot.put("ubicacion", producto.getUbicacion());
        snapshot.put("categoriaId", producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        snapshot.put("unidadId", producto.getUnidad() != null ? producto.getUnidad().getId() : null);
        snapshot.put("stockActual", producto.getStockActual());
        snapshot.put("stockMinimo", producto.getStockMinimo());
        snapshot.put("estado", producto.getEstado());
        return snapshot;
    }

}

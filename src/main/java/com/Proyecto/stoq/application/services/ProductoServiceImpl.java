package com.Proyecto.stoq.application.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.stoq.application.usecases.ObtenerProductosCriticosUseCase;
import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.CreateMovimientoInventarioDTO;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.ProductoCriticoResponse;
import com.Proyecto.stoq.dto.UpdateProductDTO;

@Service
public class ProductoServiceImpl implements ProductoService, ObtenerProductosCriticosUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final ProductosRepositoryPort productoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final UnidadRepositoryPort unidadRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final AuditService auditService;
    private final UsuarioRepositoryPort usuarioRepository;

    public ProductoServiceImpl(ProductosRepositoryPort productoRepository, 
        CategoriaRepositoryPort categoriaRepository, 
        UnidadRepositoryPort unidadRepository,
        MovimientoInventarioService movimientoInventarioService,
        AuditService auditService,
        UsuarioRepositoryPort usuarioRepository) 
    {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadRepository = unidadRepository;
        this.movimientoInventarioService = movimientoInventarioService;
        this.auditService = auditService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Producto> obtenerProductos() {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return List.of();
        }

        return productoRepository.findAll().stream()
                .filter(producto -> perteneceAEmpresa(producto, empresa))
                .toList();
    }

    @Override
    public List<ProductoCriticoResponse> obtenerProductosCriticos() {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return List.of();
        }

        return productoRepository.findAll().stream()
                .filter(producto -> perteneceAEmpresa(producto, empresa))
                .filter(producto -> Boolean.TRUE.equals(producto.getEstado()))
                .filter(producto -> {
                    Integer stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                    Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;
                    return stockActual <= stockMinimo;
                })
                .map(ProductoCriticoResponse::fromEntity)
                .toList();
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(UUID id) {
        String empresa = obtenerEmpresaAutenticada();
        if (empresa == null) {
            return Optional.empty();
        }

        return productoRepository.findById(id)
                .filter(producto -> perteneceAEmpresa(producto, empresa));
    }

    @Override
    @Transactional
    public Producto crearProducto(CreateProductDTO dto, String correoUsuario) {
        String empresaUsuario = obtenerEmpresaUsuario(correoUsuario);

        Optional<Producto> productoExistente = productoRepository.findByCodigo(dto.codigo);
        if (productoExistente.isPresent() && perteneceAEmpresa(productoExistente.get(), empresaUsuario)) {
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
            producto.setEmpresa(empresaUsuario);

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
        String empresa = obtenerEmpresaAutenticada();
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        validarProductoDeEmpresa(producto, empresa);
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
                    && !productoConMismoCodigo.get().getId().equals(id)
                    && perteneceAEmpresa(productoConMismoCodigo.get(), empresa)) {
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
    @Transactional
    public void eliminarProducto(UUID id) {
        String empresa = obtenerEmpresaAutenticada();
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        validarProductoDeEmpresa(producto, empresa);

        Map<String, Object> estadoAnterior = snapshotProducto(producto);

        producto.setEstado(false);

        Producto productoActualizado = productoRepository.save(producto);

        logger.info("{} DISABLE Producto | id={}", BIZ_TAG, id);
        auditService.registrarAuditoria(
                "Producto",
                "DELETE",
                id,
                estadoAnterior,
                snapshotProducto(productoActualizado)
        );
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
        snapshot.put("empresa", producto.getEmpresa());
        snapshot.put("categoriaId", producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        snapshot.put("unidadId", producto.getUnidad() != null ? producto.getUnidad().getId() : null);
        snapshot.put("stockActual", producto.getStockActual());
        snapshot.put("stockMinimo", producto.getStockMinimo());
        snapshot.put("estado", producto.getEstado());
        return snapshot;
    }

    private String obtenerEmpresaAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }

        return usuarioRepository.findByCorreo(authentication.getName())
                .map(usuario -> usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null)
                .filter(empresa -> empresa != null && !empresa.isBlank())
                .orElse(null);
    }

    private String obtenerEmpresaUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new RuntimeException("Usuario autenticado requerido para determinar la empresa");
        }

        return usuarioRepository.findByCorreo(correoUsuario.trim())
                .map(usuario -> usuario.getEmpresa() != null ? usuario.getEmpresa().trim() : null)
                .filter(empresa -> empresa != null && !empresa.isBlank())
                .orElseThrow(() -> new RuntimeException("El usuario autenticado no tiene empresa asignada"));
    }

    private void validarProductoDeEmpresa(Producto producto, String empresa) {
        if (!perteneceAEmpresa(producto, empresa)) {
            throw new RuntimeException("Producto no encontrado");
        }
    }

    private boolean perteneceAEmpresa(Producto producto, String empresa) {
        if (producto == null || empresa == null || producto.getEmpresa() == null) {
            return false;
        }

        return empresa.equalsIgnoreCase(producto.getEmpresa().trim());
    }

}

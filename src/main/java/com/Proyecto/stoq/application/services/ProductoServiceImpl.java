package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.UpdateProductDTO;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductosRepositoryPort productoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final UnidadRepositoryPort unidadRepository;

    public ProductoServiceImpl(ProductosRepositoryPort productoRepository, 
        CategoriaRepositoryPort categoriaRepository, 
        UnidadRepositoryPort unidadRepository) 
    {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadRepository = unidadRepository;
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
    public Producto crearProducto(CreateProductDTO dto) {
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
                categoria,
                unidad,
                dto.stock_minimo
        );

        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizarProducto(UUID id, UpdateProductDTO dto) {
       Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (dto.nombre != null && !dto.nombre.isBlank()) {
            producto.setNombre(dto.nombre);
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

        return productoRepository.save(producto);
    }

    @Override
    public void eliminarProducto(UUID id) {
        if (!productoRepository.findById(id).isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

}

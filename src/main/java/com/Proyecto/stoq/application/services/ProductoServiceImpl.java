package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.infrastructure.persistence.repositories.ProductosRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.UpdateProductDTO;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductosRepositoryPort productoRepository;

    public ProductoServiceImpl(ProductosRepositoryPort productoRepository) 
    {
        this.productoRepository = productoRepository;
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
        Producto producto = new Producto(
            dto.nombre,
            dto.codigo,
            dto.categoria,
            dto.unidad,
            dto.stock_minimo
        );
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizarProducto(UUID id, UpdateProductDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarProducto'");
    }

    @Override
    public void eliminarProducto(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarProducto'");
    }

}

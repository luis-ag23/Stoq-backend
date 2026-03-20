package com.Proyecto.stoq.infrastructure.adapters;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.ProductosRepository;

@Repository
public class ProductoRepositoryAdapter implements ProductosRepositoryPort {

    private final ProductosRepository productosRepository;

    public ProductoRepositoryAdapter(ProductosRepository productosRepository) {
        this.productosRepository = productosRepository;
    }

    @Override
    public Optional<Producto> findByNombre(String nombre) {
        return productosRepository.findByNombre(nombre);
    }

    @Override
    public Producto save(Producto producto) {
        return productosRepository.save(producto);
    }

}

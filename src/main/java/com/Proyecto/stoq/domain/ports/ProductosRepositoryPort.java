package com.Proyecto.stoq.domain.ports;
import java.util.Optional;
import com.Proyecto.stoq.domain.model.Producto;


public interface ProductosRepositoryPort {
    Optional<Producto> findByNombre(String nombre);
    Producto save(Producto producto);
}

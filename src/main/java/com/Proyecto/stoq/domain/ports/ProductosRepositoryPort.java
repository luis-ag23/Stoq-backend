package com.Proyecto.stoq.domain.ports;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import com.Proyecto.stoq.domain.model.Producto;


public interface ProductosRepositoryPort {

    List<Producto> findAll();
    Optional<Producto> findById(UUID id);
    Optional<Producto> findByNombre(String nombre);
    Producto save(Producto producto);
    void deleteById(UUID id);
}

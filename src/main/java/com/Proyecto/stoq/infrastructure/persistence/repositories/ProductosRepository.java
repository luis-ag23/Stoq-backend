package com.Proyecto.stoq.infrastructure.persistence.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.stoq.domain.model.Producto;
import java.util.Optional;
import java.util.UUID;

public interface ProductosRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByNombre(String nombre);
}

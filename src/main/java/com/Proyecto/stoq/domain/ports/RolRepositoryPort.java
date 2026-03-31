package com.Proyecto.stoq.domain.ports;

import java.util.Optional;
import com.Proyecto.stoq.domain.model.Rol;

public interface RolRepositoryPort {

    Optional<Rol> findByNombre(String nombre);

    Rol save(Rol rol);
}
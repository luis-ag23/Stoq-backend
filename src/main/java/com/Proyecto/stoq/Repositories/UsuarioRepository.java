package com.Proyecto.stoq.Repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.stoq.Entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

}
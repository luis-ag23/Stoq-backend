package com.Proyecto.stoq.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    List<Usuario> findAll();

    Optional<Usuario> findById(UUID id);

    Usuario save(Usuario usuario);

    void deleteById(UUID id);

    Optional<Usuario> findByCorreo(String correo);
}
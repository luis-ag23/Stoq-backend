package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;

public interface UsuarioService {

    List<Usuario> obtenerUsuarios();

    Optional<Usuario> obtenerUsuarioPorId(UUID id);

    Usuario crearUsuario(CreateUsuarioDTO dto);

    Usuario actualizarUsuario(UUID id, CreateUsuarioDTO dto);

    void eliminarUsuario(UUID id);

    Usuario login(String correo, String contrasena);

}
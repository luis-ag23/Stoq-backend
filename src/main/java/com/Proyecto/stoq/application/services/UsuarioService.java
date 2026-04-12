package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.dto.LoginResponseDTO;
import com.Proyecto.stoq.dto.UpdateUsuarioDTO;

public interface UsuarioService {

    List<Usuario> obtenerUsuarios();

    Optional<Usuario> obtenerUsuarioPorId(UUID id);

    Optional<Usuario> obtenerUsuarioPorCorreo(String correo);

    Usuario crearUsuario(CreateUsuarioDTO dto);

    Usuario actualizarUsuario(UUID id, UpdateUsuarioDTO dto);

    void eliminarUsuario(UUID id);

    LoginResponseDTO login(String correo, String contrasena);

}
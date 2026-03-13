package com.Proyecto.stoq.application.services;

import java.util.List;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;

public interface UsuarioService {

    List<Usuario> obtenerUsuarios();

    Usuario crearUsuario(CreateUsuarioDTO dto);

}
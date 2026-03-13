package com.Proyecto.stoq.domain.ports;

import java.util.List;
import com.Proyecto.stoq.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    List<Usuario> findAll();

    Usuario save(Usuario usuario);

}
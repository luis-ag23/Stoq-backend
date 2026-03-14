package com.Proyecto.stoq.application.services;

import org.springframework.stereotype.Service;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import java.util.List;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    public UsuarioServiceImpl(
        UsuarioRepositoryPort usuarioRepository,
        RolRepositoryPort rolRepository
    ){
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public List<Usuario> obtenerUsuarios(){
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario crearUsuario(CreateUsuarioDTO dto){

        Rol rol = rolRepository
                .findByNombre(dto.rol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario(
                dto.nombre,
                dto.correo,
                dto.contrasenaHash,
                dto.estado,
                rol
        );

        return usuarioRepository.save(usuario);
    }
}
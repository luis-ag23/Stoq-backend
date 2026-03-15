package com.Proyecto.stoq.application.services;

import org.springframework.stereotype.Service;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<Usuario> obtenerUsuarioPorId(UUID id){
        return usuarioRepository.findById(id);
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

    @Override
    public Usuario actualizarUsuario(UUID id, CreateUsuarioDTO dto){
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol rol = rolRepository
                .findByNombre(dto.rol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuarioExistente.setNombre(dto.nombre);
        usuarioExistente.setCorreo(dto.correo);
        usuarioExistente.setContrasenaHash(dto.contrasenaHash);
        usuarioExistente.setEstado(dto.estado);
        usuarioExistente.setRol(rol);

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminarUsuario(UUID id){
        if (!usuarioRepository.findById(id).isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public Usuario login(String correo, String contrasena){

        Usuario usuario = usuarioRepository
            .findByCorreo(correo)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!usuario.getContrasenaHash().equals(contrasena)){
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }
}
package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.security.JwtService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final JwtService jwtService;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    public UsuarioServiceImpl(
        UsuarioRepositoryPort usuarioRepository,
        RolRepositoryPort rolRepository, JwtService jwtService
    ){
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
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
                .findByNombre(dto.rol.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuarioExistente.setNombre(dto.nombre);
        usuarioExistente.setCorreo(dto.correo);
        usuarioExistente.setContrasenaHash(dto.contrasenaHash);
        usuarioExistente.setEstado(dto.estado);
        usuarioExistente.setRol(rol);

        System.out.println("ROL BUSCADO: " + dto.rol);

        rolRepository.findByNombre(dto.rol)
                .ifPresent(r -> System.out.println("ROL ENCONTRADO: " + r.getNombre()));

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
    public String login(String correo, String contrasena){
        Usuario usuario = usuarioRepository
        .findByCorreo(correo)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if(!usuario.getContrasenaHash().equals(contrasena)){
            throw new RuntimeException("Contraseña incorrecta");
        }
        return jwtService.generateToken(usuario.getCorreo());
    }

    
}
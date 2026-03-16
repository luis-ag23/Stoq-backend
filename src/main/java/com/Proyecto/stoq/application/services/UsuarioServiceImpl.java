package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.dto.UpdateUsuarioDTO;
import com.Proyecto.stoq.security.JwtService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    public UsuarioServiceImpl(
        UsuarioRepositoryPort usuarioRepository,
        RolRepositoryPort rolRepository, JwtService jwtService, PasswordEncoder passwordEncoder
    ){
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
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

        if (usuarioRepository.findByCorreo(dto.correo).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Rol rol = rolRepository
                .findByNombre(dto.rol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String passwordHash = passwordEncoder.encode(dto.contrasenaHash);
        Usuario usuario = new Usuario(
            dto.nombre,
            dto.correo,
            passwordHash,
            dto.estado,
            rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizarUsuario(UUID id, UpdateUsuarioDTO dto){
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (dto.nombre != null && !dto.nombre.isBlank()) {
            usuario.setNombre(dto.nombre);
        }

        if (dto.correo != null && !dto.correo.isBlank()) {
            Optional<Usuario> usuarioConMismoCorreo = usuarioRepository.findByCorreo(dto.correo);

            if (usuarioConMismoCorreo.isPresent()
                    && !usuarioConMismoCorreo.get().getId().equals(id)) {
                throw new RuntimeException("El correo ya está registrado por otro usuario");
            }

            usuario.setCorreo(dto.correo);
        }

        if (dto.contrasena != null && !dto.contrasena.isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(dto.contrasena));
        }

        if (dto.rol != null && !dto.rol.isBlank()) {
            Rol rol = rolRepository.findByNombre(dto.rol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            usuario.setRol(rol);
        }

        rolRepository.findByNombre(dto.rol)
                .ifPresent(r -> System.out.println("ROL ENCONTRADO: " + r.getNombre()));

        return usuarioRepository.save(usuario);
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
        if(!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())){
           throw new RuntimeException("Contraseña incorrecta");
        }
        return jwtService.generateToken(usuario.getCorreo());
    }

    
}
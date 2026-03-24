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
    public Optional<Usuario> obtenerUsuarioPorCorreo(String correo){
        return usuarioRepository.findByCorreo(normalizarCorreo(correo));
    }

    @Override
    public Usuario crearUsuario(CreateUsuarioDTO dto){

        String correoNormalizado = normalizarCorreo(dto.correo());

        if (usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Rol rol = rolRepository
                .findByNombre(dto.rol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String passwordHash = passwordEncoder.encode(dto.contrasena());
        Usuario usuario = new Usuario(
            limpiarTexto(dto.nombre()),
            correoNormalizado,
            limpiarTexto(dto.empresa()),
            passwordHash,
            rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizarUsuario(UUID id, UpdateUsuarioDTO dto){
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            usuario.setNombre(limpiarTexto(dto.nombre()));
        }

        if (dto.correo() != null && !dto.correo().isBlank()) {
            String correoNormalizado = normalizarCorreo(dto.correo());
            Optional<Usuario> usuarioConMismoCorreo = usuarioRepository.findByCorreo(correoNormalizado);

            if (usuarioConMismoCorreo.isPresent()
                    && !usuarioConMismoCorreo.get().getId().equals(id)) {
                throw new RuntimeException("El correo ya está registrado por otro usuario");
            }

            usuario.setCorreo(correoNormalizado);
        }

        if (dto.empresa() != null) {
            usuario.setEmpresa(limpiarTexto(dto.empresa()));
        }

        if (dto.contrasena() != null && !dto.contrasena().isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(dto.contrasena()));
        }

        if (dto.rol() != null && !dto.rol().isBlank()) {
            Rol rol = rolRepository.findByNombre(dto.rol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            usuario.setRol(rol);
        }

        if (dto.estado() != null) {
            usuario.setEstado(dto.estado());
        }

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
        .findByCorreo(normalizarCorreo(correo))
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())){
           throw new RuntimeException("Contraseña incorrecta");
        }
        return jwtService.generateToken(usuario.getCorreo());
    }

    private String normalizarCorreo(String correo) {
        return limpiarTexto(correo).toLowerCase();
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.trim();
    }
}
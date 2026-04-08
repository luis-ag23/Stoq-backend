package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.dto.LoginResponseDTO;
import com.Proyecto.stoq.dto.UpdateUsuarioDTO;
import com.Proyecto.stoq.security.JwtService;
import com.Proyecto.stoq.security.RoleCatalog;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);
    private static final String BIZ_TAG = "[STOQ-BIZ]";

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;
    private final AuditService auditService;

    public UsuarioServiceImpl(
        UsuarioRepositoryPort usuarioRepository,
        RolRepositoryPort rolRepository,
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        AuditService auditService
    ){
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
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

        String nombreRol = normalizarRol(dto.rol());
        if (!RoleCatalog.isAllowed(nombreRol)) {
            throw new RuntimeException("Rol no permitido");
        }

        Rol rol = rolRepository
            .findByNombre(nombreRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String passwordHash = passwordEncoder.encode(dto.contrasena());
        Usuario usuario = new Usuario(
            limpiarTexto(dto.nombre()),
            correoNormalizado,
            limpiarTexto(dto.empresa()),
            passwordHash,
            rol);

        logger.info("{} CREATE Usuario | correo={} | rol={}", BIZ_TAG, correoNormalizado, nombreRol);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        auditService.registrarAuditoria("Usuario", "CREATE", usuarioGuardado.getId(), null, snapshotUsuario(usuarioGuardado));
        return usuarioGuardado;
    }

    @Override
    public Usuario actualizarUsuario(UUID id, UpdateUsuarioDTO dto){
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Map<String, Object> estadoAnterior = snapshotUsuario(usuario);
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
            String nombreRol = normalizarRol(dto.rol());
            if (!RoleCatalog.isAllowed(nombreRol)) {
                throw new RuntimeException("Rol no permitido");
            }

            Rol rol = rolRepository.findByNombre(nombreRol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            usuario.setRol(rol);
        }

        if (dto.estado() != null) {
            usuario.setEstado(dto.estado());
        }

        logger.info("{} UPDATE Usuario | id={} | correo={}", BIZ_TAG, id, usuario.getCorreo());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        auditService.registrarAuditoria("Usuario", "UPDATE", id, estadoAnterior, snapshotUsuario(usuarioActualizado));
        return usuarioActualizado;
    }

    @Override
    public void eliminarUsuario(UUID id){
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (!usuario.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        logger.info("{} DELETE Usuario | id={} | correo={}", BIZ_TAG, id, usuario.get().getCorreo());
        auditService.registrarAuditoria("Usuario", "DELETE", id, snapshotUsuario(usuario.get()));
        usuarioRepository.deleteById(id);
    }

    @Override
    public LoginResponseDTO login(String correo, String contrasena){
        Usuario usuario = usuarioRepository
        .findByCorreo(normalizarCorreo(correo))
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())){
           throw new RuntimeException("Contraseña incorrecta");
        }
        String rolNormalizado = RoleCatalog.normalize(usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        logger.info("{} LOGIN Usuario | correo={} | rol={}", BIZ_TAG, usuario.getCorreo(), rolNormalizado);
        auditService.registrarAuditoria("Usuario", "LOGIN", usuario.getId(), null, snapshotSesionUsuario(usuario, rolNormalizado));
        return new LoginResponseDTO(
                jwtService.generateToken(usuario.getCorreo()),
                rolNormalizado,
                usuario.getNombre(),
                usuario.getCorreo()
        );
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

    private String normalizarRol(String rol) {
        return RoleCatalog.normalize(limpiarTexto(rol));
    }

    private Map<String, Object> snapshotUsuario(Usuario usuario) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", usuario.getId());
        snapshot.put("nombre", usuario.getNombre());
        snapshot.put("correo", usuario.getCorreo());
        snapshot.put("empresa", usuario.getEmpresa());
        snapshot.put("estado", usuario.getEstado());
        snapshot.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        return snapshot;
    }

    private Map<String, Object> snapshotSesionUsuario(Usuario usuario, String rolNormalizado) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", usuario.getId());
        snapshot.put("correo", usuario.getCorreo());
        snapshot.put("nombre", usuario.getNombre());
        snapshot.put("rol", rolNormalizado);
        return snapshot;
    }
}
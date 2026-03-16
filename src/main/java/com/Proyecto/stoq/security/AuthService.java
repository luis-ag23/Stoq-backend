package com.Proyecto.stoq.security;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.infrastructure.persistence.repositories.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    
    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService){
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    public String login(LoginRequest request){
        Usuario usuario = usuarioRepository.findByCorreo(request.correo).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!usuario.getContrasenaHash().equals(request.contrasena)){
            throw new RuntimeException("Credenciales inválidas");
        }
        return jwtService.generateToken(usuario.getCorreo());
    }
    

}

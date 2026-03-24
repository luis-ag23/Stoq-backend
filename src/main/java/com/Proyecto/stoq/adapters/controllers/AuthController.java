package com.Proyecto.stoq.adapters.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.UsuarioService;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.dto.LoginRequestDTO;
import com.Proyecto.stoq.dto.LoginResponseDTO;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto){

        String token = usuarioService.login(dto.correo(), dto.contrasena());

        return new LoginResponseDTO(token);
    }

    @PostMapping("/register")
    public LoginResponseDTO register(@Valid @RequestBody CreateUsuarioDTO dto){
        Usuario usuario = usuarioService.crearUsuario(dto);
        String token = usuarioService.login(usuario.getCorreo(), dto.contrasena());
        return new LoginResponseDTO(token);
    }
}
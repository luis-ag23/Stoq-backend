package com.Proyecto.stoq.adapters.controllers;

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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto){
        return usuarioService.login(dto.correo(), dto.contrasena());
    }

    @PostMapping("/register")
    public LoginResponseDTO register(@Valid @RequestBody CreateUsuarioDTO dto){
        Usuario usuario = usuarioService.crearUsuario(dto);
        return usuarioService.login(usuario.getCorreo(), dto.contrasena());
    }
}
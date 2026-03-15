package com.Proyecto.stoq.adapters.controllers;

import org.springframework.web.bind.annotation.*;

import com.Proyecto.stoq.application.services.UsuarioService;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.LoginRequestDTO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")  
    public Usuario login(@RequestBody LoginRequestDTO dto){
        return usuarioService.login(dto.correo, dto.contrasena);
    }
}
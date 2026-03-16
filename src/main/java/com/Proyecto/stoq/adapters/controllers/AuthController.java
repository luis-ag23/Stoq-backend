package com.Proyecto.stoq.adapters.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.UsuarioService;
import com.Proyecto.stoq.dto.LoginRequestDTO;
import com.Proyecto.stoq.dto.LoginResponseDTO;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto){

        String token = usuarioService.login(dto.correo, dto.contrasena);

        return new LoginResponseDTO(token);
    }
}
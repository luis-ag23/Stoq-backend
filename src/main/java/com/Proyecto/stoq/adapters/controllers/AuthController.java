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
<<<<<<< Updated upstream
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto){

        String token = usuarioService.login(dto.correo(), dto.contrasena());

        return new LoginResponseDTO(token);
=======
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto){
        return ResponseEntity.ok(usuarioService.login(dto.correo(), dto.contrasena()));
>>>>>>> Stashed changes
    }

    @PostMapping("/register")
    public LoginResponseDTO register(@Valid @RequestBody CreateUsuarioDTO dto){
        Usuario usuario = usuarioService.crearUsuario(dto);
<<<<<<< Updated upstream
        String token = usuarioService.login(usuario.getCorreo(), dto.contrasena());
        return new LoginResponseDTO(token);
=======
        return ResponseEntity.status(201).body(usuarioService.login(usuario.getCorreo(), dto.contrasena()));
>>>>>>> Stashed changes
    }
}
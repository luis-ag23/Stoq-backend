package com.Proyecto.stoq.adapters.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto){
        try {
            String token = usuarioService.login(dto.correo(), dto.contrasena());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUsuarioDTO dto){
        try {
            Usuario usuario = usuarioService.crearUsuario(dto);
            String token = usuarioService.login(usuario.getCorreo(), dto.contrasena());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private record ErrorResponse(String message) {
    }
}
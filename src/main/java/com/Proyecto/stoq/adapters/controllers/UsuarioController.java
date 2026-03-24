package com.Proyecto.stoq.adapters.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.UsuarioService;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.dto.UpdateUsuarioDTO;
import com.Proyecto.stoq.dto.UsuarioResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> obtenerUsuarios(){
        return usuarioService.obtenerUsuarios().stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable UUID id){
        return usuarioService.obtenerUsuarioPorId(id)
                .map(UsuarioResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioAutenticado(Authentication authentication){
        String correo = obtenerCorreoAutenticado(authentication);
        if (correo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioService.obtenerUsuarioPorCorreo(correo)
                .map(UsuarioResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public UsuarioResponseDTO crearUsuario(@Valid @RequestBody CreateUsuarioDTO dto){
        return UsuarioResponseDTO.fromEntity(usuarioService.crearUsuario(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable UUID id, @Valid @RequestBody UpdateUsuarioDTO dto){
        try {
            Usuario usuario = usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuarioAutenticado(
            Authentication authentication,
            @Valid @RequestBody UpdateUsuarioDTO dto
    ) {
        String correo = obtenerCorreoAutenticado(authentication);
        if (correo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioService.obtenerUsuarioPorCorreo(correo)
                .map(Usuario::getId)
                .map(id -> ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuarioService.actualizarUsuario(id, dto))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable UUID id){
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarUsuarioAutenticado(Authentication authentication){
        String correo = obtenerCorreoAutenticado(authentication);
        if (correo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioService.obtenerUsuarioPorCorreo(correo)
                .map(Usuario::getId)
                .map(id -> {
                    usuarioService.eliminarUsuario(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String obtenerCorreoAutenticado(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return authentication.getName();
    }
}
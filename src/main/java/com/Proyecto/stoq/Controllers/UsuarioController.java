package com.Proyecto.stoq.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.Proyecto.stoq.Entities.Rol;
import com.Proyecto.stoq.Entities.Usuario;
import com.Proyecto.stoq.Repositories.UsuarioRepository;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.Repositories.RolRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody CreateUsuarioDTO dto) {

        Rol rol = rolRepository
                .findByNombre(dto.rol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario(
                dto.nombre,
                dto.correo,
                dto.contrasenaHash,
                dto.estado,
                rol
        );

        return usuarioRepository.save(usuario);
    }
}
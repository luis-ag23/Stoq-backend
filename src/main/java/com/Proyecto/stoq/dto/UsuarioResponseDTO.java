package com.Proyecto.stoq.dto;

import java.util.UUID;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.security.RoleCatalog;

public record UsuarioResponseDTO(
        UUID id,
        String nombre,
        String correo,
        String empresa,
        Boolean estado,
        String rol
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        String nombreRol = usuario.getRol() != null ? RoleCatalog.normalize(usuario.getRol().getNombre()) : null;
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getEmpresa(),
                usuario.getEstado(),
                nombreRol
        );
    }
}

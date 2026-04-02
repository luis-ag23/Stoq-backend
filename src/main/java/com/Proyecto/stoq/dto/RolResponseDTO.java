package com.Proyecto.stoq.dto;

import java.util.List;
import java.util.UUID;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.security.RoleCatalog;

public record RolResponseDTO(
        UUID id,
        String nombre,
        String descripcion,
        List<String> permisos
) {
    public static RolResponseDTO fromEntity(Rol rol) {
        return new RolResponseDTO(
                rol.getId(),
                RoleCatalog.normalize(rol.getNombre()),
                rol.getDescripcion(),
                RoleCatalog.permissionsFor(rol.getNombre())
        );
    }
}
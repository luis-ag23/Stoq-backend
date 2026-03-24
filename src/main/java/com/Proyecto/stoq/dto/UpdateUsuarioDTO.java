package com.Proyecto.stoq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUsuarioDTO(
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @Size(max = 120, message = "La empresa no puede superar 120 caracteres")
        String empresa,

        @Size(min = 6, max = 100, message = "La contrasena debe tener entre 6 y 100 caracteres")
        String contrasena,

        String rol,

        Boolean estado
) {
}

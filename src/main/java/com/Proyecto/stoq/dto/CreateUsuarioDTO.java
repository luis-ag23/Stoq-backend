package com.Proyecto.stoq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUsuarioDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @Size(max = 120, message = "La empresa no puede superar 120 caracteres")
        String empresa,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, max = 100, message = "La contrasena debe tener entre 6 y 100 caracteres")
        String contrasena,

        @NotBlank(message = "El rol es obligatorio")
        String rol
) {
}
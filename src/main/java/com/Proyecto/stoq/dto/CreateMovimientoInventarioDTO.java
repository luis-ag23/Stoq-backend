package com.Proyecto.stoq.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMovimientoInventarioDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productoId,

        @NotBlank(message = "El tipo de movimiento es obligatorio")
        String tipoMovimiento,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
}
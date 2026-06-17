package com.Proyecto.stoq.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSolicitudReposicionDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productoId,

        @NotNull(message = "La cantidad solicitada es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidadSolicitada
) {
}
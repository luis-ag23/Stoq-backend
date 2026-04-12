package com.Proyecto.stoq.dto;

public record LoginResponseDTO(
	String token,
	String rol,
	String nombre,
	String correo
) {
}

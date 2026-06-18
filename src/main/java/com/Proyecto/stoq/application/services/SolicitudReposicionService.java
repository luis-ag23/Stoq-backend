package com.Proyecto.stoq.application.services;

import java.util.List;
import java.util.UUID;
import com.Proyecto.stoq.dto.SolicitudReposicionResponseDTO;
import com.Proyecto.stoq.dto.CreateSolicitudReposicionDTO;

public interface SolicitudReposicionService {
    List<SolicitudReposicionResponseDTO> obtenerSolicitudesPorEmpresa(String emailUsuario);
    void generarSolicitudesAutomaticas(String emailUsuario);
    SolicitudReposicionResponseDTO actualizarEstadoSolicitud(UUID id, String emailUsuario, String nuevoEstado);
    SolicitudReposicionResponseDTO crearSolicitudManual(String correoUsuario, CreateSolicitudReposicionDTO dto);
}

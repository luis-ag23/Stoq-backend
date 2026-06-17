package com.Proyecto.stoq.application.services;

import java.util.List;
import com.Proyecto.stoq.dto.RecomendacionAutomaticaDTO;

public interface RecomendacionService {
    List<RecomendacionAutomaticaDTO> obtenerRecomendaciones(String emailUsuario);
}

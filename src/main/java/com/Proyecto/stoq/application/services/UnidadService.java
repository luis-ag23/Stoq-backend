package com.Proyecto.stoq.application.services;
import java.util.List;
import com.Proyecto.stoq.dto.CreateUnidadDTO;
import com.Proyecto.stoq.domain.model.Unidad;

public interface UnidadService {
    List<Unidad> obtenerUnidades();
    Unidad crearUnidad(CreateUnidadDTO dto);
}

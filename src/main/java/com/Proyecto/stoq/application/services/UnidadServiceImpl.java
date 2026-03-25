package com.Proyecto.stoq.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.dto.CreateUnidadDTO;

@Service
public class UnidadServiceImpl implements UnidadService {
    private final UnidadRepositoryPort unidadRepository;

    public UnidadServiceImpl(UnidadRepositoryPort unidadRepository) {
        this.unidadRepository = unidadRepository;
    }

    @Override
    public List<Unidad> obtenerUnidades() {
        return unidadRepository.findAll();
    }

    @Override
    public Unidad crearUnidad(CreateUnidadDTO dto) {
        Unidad unidad = new Unidad();
        unidad.setNombre(dto.nombre);
        unidad.setAbreviatura(dto.abreviatura);
        return unidadRepository.save(unidad);
    }
    
}

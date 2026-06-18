package com.Proyecto.stoq.adapters.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.dto.RolResponseDTO;
import com.Proyecto.stoq.security.RoleCatalog;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolRepositoryPort rolRepository;

    public RolController(RolRepositoryPort rolRepository) {
        this.rolRepository = rolRepository;
    }

    @GetMapping
    public List<RolResponseDTO> obtenerRoles() {
        return rolRepository.findAll().stream()
                .filter(rol -> RoleCatalog.isAllowed(rol.getNombre()))
                .map(RolResponseDTO::fromEntity)
                .sorted((a, b) -> a.nombre().compareToIgnoreCase(b.nombre()))
                .toList();
    }
}
package com.Proyecto.stoq.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepositoryPort rolRepository;

    public DataInitializer(RolRepositoryPort rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles básicos si no existen
        if (rolRepository.findByNombre("USER").isEmpty()) {
            Rol userRol = new Rol();
            userRol.setNombre("USER");
            userRol.setDescripcion("Usuario estándar");
            rolRepository.save(userRol);
            System.out.println("Rol USER creado");
        }

        if (rolRepository.findByNombre("ADMIN").isEmpty()) {
            Rol adminRol = new Rol();
            adminRol.setNombre("ADMIN");
            adminRol.setDescripcion("Administrador del sistema");
            rolRepository.save(adminRol);
            System.out.println("Rol ADMIN creado");
        }

        if (rolRepository.findByNombre("OPERADOR").isEmpty()) {
            Rol operadorRol = new Rol();
            operadorRol.setNombre("OPERADOR");
            operadorRol.setDescripcion("Operador del sistema");
            rolRepository.save(operadorRol);
            System.out.println("Rol OPERADOR creado");
        }
    }
}
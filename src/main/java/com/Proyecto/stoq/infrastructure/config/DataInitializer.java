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
        crearRolSiNoExiste("ADMIN", "Administrador del sistema");
        crearRolSiNoExiste("OPERADOR", "Operador de almacén");
        crearRolSiNoExiste("GERENTE", "Gerente o supervisor");
    }

    private void crearRolSiNoExiste(String nombre, String descripcion) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            rolRepository.save(rol);
            System.out.println("Rol " + nombre + " creado");
        }
    }
}
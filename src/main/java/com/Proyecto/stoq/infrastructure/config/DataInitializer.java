package com.Proyecto.stoq.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.security.RoleCatalog;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepositoryPort rolRepository;

    public DataInitializer(RolRepositoryPort rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo roles permitidos por negocio.
        crearOActualizarRol(RoleCatalog.ADMIN, "Administrador del sistema");
        crearOActualizarRol(RoleCatalog.OPERADOR, "Registra entradas y salidas, consulta stock");
        crearOActualizarRol(RoleCatalog.GERENTE, "Consulta dashboard, stock critico y reportes");
    }

    private void crearOActualizarRol(String nombre, String descripcion) {
        Rol rol = rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre(nombre);
            return nuevoRol;
        });

        rol.setDescripcion(descripcion);
        rolRepository.save(rol);
        logger.info("[STOQ-SEED] Rol sincronizado: {}", nombre);
    }
}
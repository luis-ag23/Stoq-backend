package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceImplTest {

    private UsuarioRepositoryPort usuarioRepository;
    private RolRepositoryPort rolRepository;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {

        usuarioRepository = Mockito.mock(UsuarioRepositoryPort.class);
        rolRepository = Mockito.mock(RolRepositoryPort.class);

        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository);
    }

    @Test
    void obtenerUsuarios_deberiaRetornarListaDeUsuarios() {

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        Usuario usuario = new Usuario(
                "Juan",
                "juan@email.com",
                "hash123",
                true,
                rol
        );

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.obtenerUsuarios();

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
    }

    @Test
    void crearUsuario_deberiaGuardarUsuario() {

        CreateUsuarioDTO dto = new CreateUsuarioDTO();
        dto.nombre = "Carlos";
        dto.correo = "carlos@email.com";
        dto.contrasenaHash = "123";
        dto.estado = true;
        dto.rol = "ADMIN";

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rol));

        Usuario usuarioGuardado = new Usuario(
                dto.nombre,
                dto.correo,
                dto.contrasenaHash,
                dto.estado,
                rol
        );

        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        Usuario resultado = usuarioService.crearUsuario(dto);

        assertEquals("Carlos", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorSiRolNoExiste() {

        CreateUsuarioDTO dto = new CreateUsuarioDTO();
        dto.rol = "ADMIN";

        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            usuarioService.crearUsuario(dto);
        });
    }
}
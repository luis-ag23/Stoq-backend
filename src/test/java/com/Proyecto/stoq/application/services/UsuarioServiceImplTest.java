package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.model.Rol;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.domain.ports.RolRepositoryPort;
import com.Proyecto.stoq.dto.CreateUsuarioDTO;
import com.Proyecto.stoq.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceImplTest {

    private UsuarioRepositoryPort usuarioRepository;
    private RolRepositoryPort rolRepository;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {

        usuarioRepository = Mockito.mock(UsuarioRepositoryPort.class);
        rolRepository = Mockito.mock(RolRepositoryPort.class);
        jwtService = Mockito.mock(JwtService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);

        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository, jwtService, passwordEncoder);
    }

    @Test
    void obtenerUsuarios_deberiaRetornarListaDeUsuarios() {

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        Usuario usuario = new Usuario(
                "Juan",
                "juan@email.com",
                "empresa1",
                "hash123",
                rol
        );

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.obtenerUsuarios();

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
    }

    @Test
    void crearUsuario_deberiaGuardarUsuario() {

        CreateUsuarioDTO dto = new CreateUsuarioDTO(
            "Carlos",
            "carlos@email.com",
            null,
            "123456",
            "ADMIN"
        );

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        when(usuarioRepository.findByCorreo(dto.correo())).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode(dto.contrasena())).thenReturn("hashed123");

        Usuario usuarioGuardado = new Usuario(
            dto.nombre(),
            dto.correo(),
            dto.empresa(),
                "hashed123",
                rol
        );

        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        Usuario resultado = usuarioService.crearUsuario(dto);

        assertEquals("Carlos", resultado.getNombre());

        verify(usuarioRepository,times(1)).findByCorreo(dto.correo());
        verify(passwordEncoder, times(1)).encode(dto.contrasena());
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorSiRolNoExiste() {

        CreateUsuarioDTO dto = new CreateUsuarioDTO(
            "Carlos",
            "carlos@email.com",
            null,
            "123456",
            "ADMIN"
        );

        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            usuarioService.crearUsuario(dto);
        });
    }

    @Test
    void crearUsuario_deberiaLanzarErrorSiCorreoYaExiste() {

        CreateUsuarioDTO dto = new CreateUsuarioDTO(
            "Carlos",
            "carlos@email.com",
            null,
            "123456",
            "ADMIN"
        );

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        Usuario usuarioExistente = new Usuario(
                "Otro",
            dto.correo(),
            dto.empresa(),
                "hash456",
                rol
        );

        when(usuarioRepository.findByCorreo(dto.correo())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(RuntimeException.class, () -> {
            usuarioService.crearUsuario(dto);
        });

        verify(usuarioRepository, never()).save(any());
    }
}
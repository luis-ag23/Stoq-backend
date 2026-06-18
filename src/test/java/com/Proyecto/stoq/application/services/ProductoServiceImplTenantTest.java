package com.Proyecto.stoq.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Proyecto.stoq.domain.model.Categoria;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.model.Unidad;
import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.domain.ports.CategoriaRepositoryPort;
import com.Proyecto.stoq.domain.ports.ProductosRepositoryPort;
import com.Proyecto.stoq.domain.ports.UnidadRepositoryPort;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;

public class ProductoServiceImplTenantTest {

    @Mock
    private ProductosRepositoryPort productoRepository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private UnidadRepositoryPort unidadRepository;

    @Mock
    private MovimientoInventarioService movimientoInventarioService;

    @Mock
    private AuditService auditService;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private AutoCloseable mocks;
    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        productoService = new ProductoServiceImpl(
                productoRepository,
                categoriaRepository,
                unidadRepository,
                movimientoInventarioService,
                auditService,
                usuarioRepository
        );

        setSecurityContext("usuario@empresa-a.com");
        doReturn(Optional.of(usuario("usuario@empresa-a.com", "Empresa A")))
                .when(usuarioRepository)
                .findByCorreo("usuario@empresa-a.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void obtenerProductosSoloDevuelveLosDeLaEmpresaAutenticada() {
        Producto productoPropio = producto("P-1", "Empresa A");
        Producto productoAjeno = producto("P-2", "Empresa B");

        doReturn(List.of(productoPropio, productoAjeno)).when(productoRepository).findAll();

        List<Producto> productos = productoService.obtenerProductos();

        assertEquals(1, productos.size());
        assertEquals("P-1", productos.get(0).getCodigo());
    }

    @Test
    void crearProductoAsignaLaEmpresaDelUsuarioAutenticado() {
        Categoria categoria = new Categoria();
        Unidad unidad = new Unidad();

        UUID categoriaId = UUID.randomUUID();
        UUID unidadId = UUID.randomUUID();
        setId(categoria, categoriaId);
        setId(unidad, unidadId);

        doReturn(Optional.of(categoria)).when(categoriaRepository).findById(categoriaId);
        doReturn(Optional.of(unidad)).when(unidadRepository).findById(unidadId);
        doReturn(Optional.empty()).when(productoRepository).findByCodigo("P-100");

        com.Proyecto.stoq.dto.CreateProductDTO dto = new com.Proyecto.stoq.dto.CreateProductDTO();
        dto.codigo = "P-100";
        dto.nombre = "Producto nuevo";
        dto.ubicacion = "Bodega 1";
        dto.categoriaId = categoriaId;
        dto.unidadId = unidadId;
        dto.stock_inicial = 0;
        dto.stock_minimo = 1;

        org.mockito.Mockito.when(productoRepository.save(org.mockito.ArgumentMatchers.any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto creado = productoService.crearProducto(dto, "usuario@empresa-a.com");

        assertEquals("Empresa A", creado.getEmpresa());
    }

    private void setSecurityContext(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of())
        );
    }

    private Producto producto(String codigo, String empresa) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(codigo + " nombre");
        producto.setEmpresa(empresa);
        return producto;
    }

    private Usuario usuario(String correo, String empresa) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setEmpresa(empresa);
        return usuario;
    }

    private void setId(Categoria categoria, UUID id) {
        try {
            var field = Categoria.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(categoria, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setId(Unidad unidad, UUID id) {
        try {
            var field = Unidad.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(unidad, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
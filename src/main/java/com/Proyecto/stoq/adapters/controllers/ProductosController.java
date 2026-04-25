package com.Proyecto.stoq.adapters.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Proyecto.stoq.application.services.ProductoService;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.UpdateProductDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    private final ProductoService productoService;

    public ProductosController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> obtenerProductos() {
        return productoService.obtenerProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable UUID id) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(Authentication authentication, @Valid @RequestBody CreateProductDTO dto) {
        String correo = authentication != null ? authentication.getName() : null;
        Producto producto = productoService.crearProducto(dto, correo);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable UUID id, @Valid @RequestBody UpdateProductDTO dto) {
        try {
            Producto producto = productoService.actualizarProducto(id, dto);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable UUID id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
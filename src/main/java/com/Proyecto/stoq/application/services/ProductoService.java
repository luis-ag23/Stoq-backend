package com.Proyecto.stoq.application.services;
import java.util.*;

import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.CreateProductDTO;


public interface ProductoService {
    
    List<Producto> obtenerProductos();
    Optional<Producto> obtenerProductoPorId(UUID id);
    
    void crearProducto(CreateProductDTO createProductDTO);
    Producto actualizarProducto(UUID id, CreateProductDTO createProductDTO);
    
    void eliminarProducto(UUID id);
}

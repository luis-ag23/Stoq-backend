package com.Proyecto.stoq.application.services;
import java.util.*;

import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.CreateProductDTO;
import com.Proyecto.stoq.dto.UpdateProductDTO;


public interface ProductoService {
    
    List<Producto> obtenerProductos();
    Optional<Producto> obtenerProductoPorId(UUID id);
    
    void crearProducto(CreateProductDTO dto);
    Producto actualizarProducto(UUID id, UpdateProductDTO dto);
    
    void eliminarProducto(UUID id);
}

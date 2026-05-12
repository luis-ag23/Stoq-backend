package com.Proyecto.stoq.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.Proyecto.stoq.domain.model.Alerta;
import com.Proyecto.stoq.domain.model.Producto;

public class AlertaResponseDTOTest {

    @Test
    void fromEntityNoFallaCuandoAlertaEsNula() {
        AlertaResponseDTO dto = AlertaResponseDTO.fromEntity(null);

        assertNull(dto.id());
        assertNull(dto.tipo());
        assertNull(dto.productoId());
    }

    @Test
    void fromEntityNoFallaCuandoProductoTieneStocksNulos() {
        Producto producto = new Producto();
        producto.setCodigo("SKU-1");
        producto.setNombre("Producto 1");

        Alerta alerta = new Alerta("STOCK_BAJO", "Mensaje", producto);

        AlertaResponseDTO dto = assertDoesNotThrow(() -> AlertaResponseDTO.fromEntity(alerta));

        assertEquals("STOCK_BAJO", dto.tipo());
        assertEquals("SKU-1", dto.productoCodigo());
        assertEquals(0, dto.stockActual());
        assertNull(dto.stockMinimo());
        assertNull(dto.diferencia());
    }
}
package com.Proyecto.stoq.infrastructure.persistence.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductosRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByNombre(String nombre);
    Optional<Producto> findByCodigo(String codigo);

    long countByEmpresaAndEstadoTrue(String empresa);

    @Query("""
        select count(p)
        from Producto p
        where p.empresa = :empresa
          and p.estado = true
          and p.stockMinimo is not null
          and p.stockActual <= p.stockMinimo
    """)
    long contarProductosBajoStock(@Param("empresa") String empresa);

    @Query("""
        select new com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO(
            c.id,
            c.nombre,
            count(p),
            coalesce(sum(coalesce(p.stockActual, 0)), cast(0 as long)),
            coalesce(sum(coalesce(p.stockMinimo, 0)), cast(0 as long)),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long)
        )
        from Producto p
        join p.categoria c
        where p.empresa = :empresa
          and p.estado = true
        group by c.id, c.nombre
        order by c.nombre asc
    """)
    List<ReporteCategoriaResumenDTO> obtenerResumenCategorias(@Param("empresa") String empresa);
}

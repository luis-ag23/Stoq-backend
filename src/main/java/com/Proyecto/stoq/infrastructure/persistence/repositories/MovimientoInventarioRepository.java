package com.Proyecto.stoq.infrastructure.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Proyecto.stoq.domain.model.Movimiento_Inventario;
import com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO;
import com.Proyecto.stoq.dto.ReporteMovimientoTotalesDTO;
import com.Proyecto.stoq.dto.ReporteProductoRotacionDTO;

public interface MovimientoInventarioRepository extends JpaRepository<Movimiento_Inventario, UUID> {
    List<Movimiento_Inventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento_Inventario> findAllByOrderByFechaMovimientoDesc();

        @Query("""
                select m
                from Movimiento_Inventario m
                join m.producto p
                where p.empresa = :empresa
                    and m.fechaMovimiento between :inicio and :fin
                order by m.fechaMovimiento desc
        """)
        List<Movimiento_Inventario> findByEmpresaAndFechaMovimientoBetween(
                        @Param("empresa") String empresa,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin
        );

        @Query("""
                select m
                from Movimiento_Inventario m
                join m.producto p
                where p.empresa = :empresa
                    and m.fechaMovimiento between :inicio and :fin
                order by m.fechaMovimiento desc
        """)
        List<Movimiento_Inventario> findRecentByEmpresaAndFechaMovimientoBetween(
                        @Param("empresa") String empresa,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        Pageable pageable
        );

        @Query("""
            select count(m)
            from Movimiento_Inventario m
            join m.producto p
            where p.empresa = :empresa
                and m.fechaMovimiento between :inicio and :fin
        """)
        Long contarMovimientosPorEmpresa(
                @Param("empresa") String empresa,
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin
        );

        @Query("""
                select new com.Proyecto.stoq.dto.ReporteMovimientoTotalesDTO(
                    coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then 1 else 0 end), 0),
                    coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1 else 0 end), 0),
                    coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then m.cantidad else 0 end), 0),
                    coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0 end), 0)
                )
                from Movimiento_Inventario m
                join m.producto p
                where p.empresa = :empresa
                    and m.fechaMovimiento between :inicio and :fin
        """)
        ReporteMovimientoTotalesDTO obtenerTotalesMovimientosPorEmpresa(
                        @Param("empresa") String empresa,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin
        );

    @Query("""
        select new com.Proyecto.stoq.dto.ReporteCategoriaResumenDTO(
            c.id,
            c.nombre,
            0L,
            0L,
            0L,
            count(m),
            coalesce(sum(m.cantidad), 0L),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then 1L else 0L end), 0L),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1L else 0L end), 0L),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then m.cantidad else 0L end), 0L),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0L end), 0L)
        )
        from Movimiento_Inventario m
        join m.producto p
        join p.categoria c
        where p.empresa = :empresa
          and m.fechaMovimiento between :inicio and :fin
        group by c.id, c.nombre
        order by c.nombre asc
    """)
    List<ReporteCategoriaResumenDTO> obtenerResumenMovimientosPorCategoria(
            @Param("empresa") String empresa,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

        @Query("""
                select new com.Proyecto.stoq.dto.ReporteProductoRotacionDTO(
                        p.id,
                        p.codigo,
                        p.nombre,
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1L else 0L end), 0L),
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0L end), 0L),
                        max(case when upper(m.tipoMovimiento) = 'SALIDA' then m.fechaMovimiento else null end),
                        c.nombre,
                        u.abreviatura
                )
                from Movimiento_Inventario m
                join m.producto p
                join p.categoria c
                join p.unidad u
                where p.empresa = :empresa
                    and m.fechaMovimiento between :inicio and :fin
                group by p.id, p.codigo, p.nombre, c.nombre, u.abreviatura
                order by coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0L end), 0L) desc,
                                 coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1L else 0L end), 0L) desc,
                                 p.nombre asc
        """)
        List<ReporteProductoRotacionDTO> obtenerTopRotacionPorEmpresa(
                        @Param("empresa") String empresa,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        Pageable pageable
        );

            @Query("""
            select coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0L end), 0L)
            from Movimiento_Inventario m
            join m.producto p
            where p.id = :productoId
              and m.fechaMovimiento between :inicio and :fin
            """)
            Long sumarCantidadSalidasPorProductoEntre(
                @Param("productoId") UUID productoId,
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin
            );

            @Query("""
            select count(m)
            from Movimiento_Inventario m
            join m.producto p
            where p.id = :productoId
              and upper(m.tipoMovimiento) = 'SALIDA'
              and m.fechaMovimiento between :inicio and :fin
            """)
            Long contarSalidasPorProductoEntre(
                @Param("productoId") UUID productoId,
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin
            );

            @Query("""
                select m
                from Movimiento_Inventario m
                where m.producto.id = :productoId
                  and upper(m.tipoMovimiento) = :tipoMovimiento
                order by m.fechaMovimiento asc
            """)
            List<Movimiento_Inventario> obtenerMovimientosPorProductoYTipo(
                @Param("productoId") UUID productoId,
                @Param("tipoMovimiento") String tipoMovimiento
            );
}
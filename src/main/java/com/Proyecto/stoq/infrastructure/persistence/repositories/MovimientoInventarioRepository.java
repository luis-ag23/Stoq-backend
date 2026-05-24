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
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then 1 else 0 end), cast(0 as long)),
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1 else 0 end), cast(0 as long)),
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then m.cantidad else 0 end), cast(0 as long)),
                        coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0 end), cast(0 as long))
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
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            count(m),
            coalesce(sum(m.cantidad), cast(0 as long)),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then 1 else 0 end), cast(0 as long)),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then 1 else 0 end), cast(0 as long)),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'ENTRADA' then m.cantidad else 0 end), cast(0 as long)),
            coalesce(sum(case when upper(m.tipoMovimiento) = 'SALIDA' then m.cantidad else 0 end), cast(0 as long))
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
}
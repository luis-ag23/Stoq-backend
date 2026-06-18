-- Tenant por empresa para productos
-- Asocia cada producto a la empresa de su primer movimiento registrado y
-- prepara la base para un filtrado estrictamente por empresa.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE productos
    ADD COLUMN IF NOT EXISTS empresa VARCHAR(120);

WITH primera_empresa AS (
    SELECT DISTINCT ON (m.producto_id)
        m.producto_id,
        NULLIF(BTRIM(u.empresa), '') AS empresa
    FROM movimientos_inventario m
    JOIN usuarios u ON u.id = m.usuario_id
    ORDER BY m.producto_id, m.fecha_movimiento ASC
)
UPDATE productos p
SET empresa = pe.empresa
FROM primera_empresa pe
WHERE p.id = pe.producto_id
  AND (p.empresa IS NULL OR BTRIM(p.empresa) = '');

ALTER TABLE productos
    ALTER COLUMN empresa SET NOT NULL;

DO $$
DECLARE
    unique_constraint_name text;
BEGIN
    SELECT tc.constraint_name
    INTO unique_constraint_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
     AND tc.table_schema = kcu.table_schema
    WHERE tc.table_name = 'productos'
      AND tc.constraint_type = 'UNIQUE'
      AND kcu.column_name = 'codigo'
    GROUP BY tc.constraint_name
    LIMIT 1;

    IF unique_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE productos DROP CONSTRAINT %I', unique_constraint_name);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_productos_empresa_codigo
    ON productos ((UPPER(BTRIM(empresa))), (UPPER(BTRIM(codigo))));

COMMIT;
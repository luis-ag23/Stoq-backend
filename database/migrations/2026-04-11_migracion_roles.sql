-- Migracion de roles (Supabase/PostgreSQL)
-- Objetivo: permitir solo ADMIN, OPERADOR y GERENTE.
-- Estrategia:
-- 1) Garantizar roles canonicos.
-- 2) Migrar usuarios con USER -> OPERADOR y ADMINISTRADOR -> ADMIN.
-- 3) Reasignar cualquier rol no permitido -> OPERADOR.
-- 4) Eliminar roles no permitidos.
-- 5) Enforzar unicidad y check de valores permitidos.

BEGIN;

-- Supabase/PostgreSQL: requerido para gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) Garantizar roles canonicos
INSERT INTO roles (id, nombre, descripcion)
SELECT gen_random_uuid(), 'ADMIN', 'Administrador del sistema'
WHERE NOT EXISTS (
    SELECT 1 FROM roles r WHERE UPPER(TRIM(r.nombre)) = 'ADMIN'
);

INSERT INTO roles (id, nombre, descripcion)
SELECT gen_random_uuid(), 'OPERADOR', 'Registra entradas y salidas, consulta stock'
WHERE NOT EXISTS (
    SELECT 1 FROM roles r WHERE UPPER(TRIM(r.nombre)) = 'OPERADOR'
);

INSERT INTO roles (id, nombre, descripcion)
SELECT gen_random_uuid(), 'GERENTE', 'Consulta dashboard, stock critico y reportes'
WHERE NOT EXISTS (
    SELECT 1 FROM roles r WHERE UPPER(TRIM(r.nombre)) = 'GERENTE'
);

-- 2) Migraciones explicitas de nomenclatura heredada
WITH rol_operador AS (
    SELECT id FROM roles WHERE UPPER(TRIM(nombre)) = 'OPERADOR' ORDER BY id LIMIT 1
), roles_user AS (
    SELECT id FROM roles WHERE UPPER(TRIM(nombre)) = 'USER'
)
UPDATE usuarios u
SET rol_id = (SELECT id FROM rol_operador)
WHERE u.rol_id IN (SELECT id FROM roles_user);

WITH rol_admin AS (
    SELECT id FROM roles WHERE UPPER(TRIM(nombre)) = 'ADMIN' ORDER BY id LIMIT 1
), roles_administrador AS (
    SELECT id FROM roles WHERE UPPER(TRIM(nombre)) = 'ADMINISTRADOR'
)
UPDATE usuarios u
SET rol_id = (SELECT id FROM rol_admin)
WHERE u.rol_id IN (SELECT id FROM roles_administrador);

-- 3) Fallback: cualquier rol no permitido pasa a OPERADOR
WITH rol_operador AS (
    SELECT id FROM roles WHERE UPPER(TRIM(nombre)) = 'OPERADOR' ORDER BY id LIMIT 1
)
UPDATE usuarios u
SET rol_id = (SELECT id FROM rol_operador)
WHERE EXISTS (
    SELECT 1
    FROM roles r
    WHERE r.id = u.rol_id
      AND UPPER(TRIM(r.nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE')
);

-- 4) Normalizar nombre y descripcion de roles permitidos
UPDATE roles
SET nombre = UPPER(TRIM(nombre))
WHERE UPPER(TRIM(nombre)) IN ('ADMIN', 'OPERADOR', 'GERENTE');

UPDATE roles
SET descripcion = CASE UPPER(TRIM(nombre))
    WHEN 'ADMIN' THEN 'Administrador del sistema'
    WHEN 'OPERADOR' THEN 'Registra entradas y salidas, consulta stock'
    WHEN 'GERENTE' THEN 'Consulta dashboard, stock critico y reportes'
    ELSE descripcion
END
WHERE UPPER(TRIM(nombre)) IN ('ADMIN', 'OPERADOR', 'GERENTE');

-- 5) Deduplicar roles permitidos (si hubiera multiples filas por mismo nombre)
WITH canon AS (
    SELECT UPPER(TRIM(nombre)) AS nombre_norm, (MIN(id::text))::uuid AS id_canon
    FROM roles
    WHERE UPPER(TRIM(nombre)) IN ('ADMIN', 'OPERADOR', 'GERENTE')
    GROUP BY UPPER(TRIM(nombre))
), duplicados AS (
    SELECT r.id, UPPER(TRIM(r.nombre)) AS nombre_norm
    FROM roles r
    JOIN canon c ON c.nombre_norm = UPPER(TRIM(r.nombre))
    WHERE r.id <> c.id_canon
)
UPDATE usuarios u
SET rol_id = c.id_canon
FROM roles r
JOIN duplicados d ON d.id = r.id
JOIN canon c ON c.nombre_norm = d.nombre_norm
WHERE u.rol_id = r.id;

DELETE FROM roles r
WHERE UPPER(TRIM(r.nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE')
   OR EXISTS (
       SELECT 1
       FROM roles r2
       WHERE UPPER(TRIM(r2.nombre)) = UPPER(TRIM(r.nombre))
                 AND r2.id::text < r.id::text
   );

-- 6) Restricciones para aceptar solo los 3 roles
ALTER TABLE roles
    DROP CONSTRAINT IF EXISTS chk_roles_nombre_permitido;

ALTER TABLE roles
    ADD CONSTRAINT chk_roles_nombre_permitido
    CHECK (UPPER(TRIM(nombre)) IN ('ADMIN', 'OPERADOR', 'GERENTE'));

CREATE UNIQUE INDEX IF NOT EXISTS ux_roles_nombre_normalizado
    ON roles ((UPPER(TRIM(nombre))));

-- 7) Validaciones duras: si algo queda inconsistente, aborta la migracion
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM roles
        WHERE UPPER(TRIM(nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE')
    ) THEN
        RAISE EXCEPTION 'Migracion invalida: existen roles fuera del catalogo permitido';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios u
        LEFT JOIN roles r ON r.id = u.rol_id
        WHERE r.id IS NULL
    ) THEN
        RAISE EXCEPTION 'Migracion invalida: existen usuarios con rol_id inexistente';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios u
        JOIN roles r ON r.id = u.rol_id
        WHERE UPPER(TRIM(r.nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE')
    ) THEN
        RAISE EXCEPTION 'Migracion invalida: existen usuarios asociados a roles no permitidos';
    END IF;
END $$;

COMMIT;

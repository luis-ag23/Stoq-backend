-- Validacion post-migracion de roles (Supabase/PostgreSQL)
-- Ejecutar despues de 2026-04-11_migracion_roles.sql

-- 1) Debe haber solo los 3 roles permitidos
SELECT
    CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END AS estado,
    COUNT(*) AS roles_invalidos
FROM roles
WHERE UPPER(TRIM(nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE');

-- 2) No debe haber usuarios con rol inexistente
SELECT
    CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END AS estado,
    COUNT(*) AS usuarios_huerfanos
FROM usuarios u
LEFT JOIN roles r ON r.id = u.rol_id
WHERE r.id IS NULL;

-- 3) No debe haber usuarios con rol fuera del catalogo permitido
SELECT
    CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END AS estado,
    COUNT(*) AS usuarios_con_rol_invalido
FROM usuarios u
JOIN roles r ON r.id = u.rol_id
WHERE UPPER(TRIM(r.nombre)) NOT IN ('ADMIN', 'OPERADOR', 'GERENTE');

-- 4) Vista final de roles existentes
SELECT id, nombre, descripcion
FROM roles
ORDER BY nombre;

-- 5) Distribucion de usuarios por rol
SELECT r.nombre, COUNT(*) AS usuarios
FROM usuarios u
JOIN roles r ON r.id = u.rol_id
GROUP BY r.nombre
ORDER BY r.nombre;

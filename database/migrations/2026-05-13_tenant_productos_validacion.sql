-- Validacion post-migracion de tenant por empresa en productos

SELECT
    CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END AS estado,
    COUNT(*) AS productos_sin_empresa
FROM productos
WHERE empresa IS NULL OR BTRIM(empresa) = '';

SELECT empresa, COUNT(*) AS productos
FROM productos
GROUP BY empresa
ORDER BY empresa;
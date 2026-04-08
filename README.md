# STOQ - Sistema de Gestión de Inventario

## Descripción

STOQ es un sistema de información web diseñado para optimizar la gestión de inventarios en pequeñas y medianas empresas.

El sistema permite digitalizar y centralizar el control de productos, entradas y salidas de inventario, facilitando el monitoreo del stock en tiempo real y la generación de reportes para la toma de decisiones.

Muchas organizaciones gestionan su inventario mediante hojas de cálculo, registros manuales o sistemas incompletos, lo que puede generar desorden, falta de visibilidad del stock real, errores humanos y pérdidas económicas.

STOQ busca solucionar estos problemas proporcionando una plataforma moderna que mejore la eficiencia operativa y permita mantener un control confiable del inventario.

## 📋 Características Principales

* **Autenticación y Seguridad:** Control de acceso mediante Login y Registro de usuarios.
* **Gestión de Catálogo:** Administración completa de productos y categorización jerárquica.
* **Control de Stock Proactivo:** Registro automatizado de entradas y salidas con actualización en tiempo real.
* **Auditoría de Movimientos:** Historial detallado de operaciones, permitiendo trazabilidad (quién, cuándo y qué se movió), tanto en inventario como en eventos de API.
* **Auditoría de API y Trazabilidad Técnica:** Registro de llamadas REST con usuario autenticado, IP de origen, User-Agent, tiempos de respuesta y errores.
* **Monitoreo y Observabilidad:** Endpoints de salud y métricas mediante Spring Boot Actuator y exportación para Prometheus.
* **Dashboard Operativo:** Panel principal con indicadores visuales sobre el estado actual del inventario.

---

## 🛠️ Stack Tecnológico

### **Backend**
* **Framework:** Spring Boot
* **Lenguaje:** Java
* **Gestión de Dependencias:** Gradle (Kotlin DSL)
* **Persistencia:** Spring Data JPA
* **Seguridad:** Spring Security + JWT
* **Observabilidad:** Spring Boot Actuator + Micrometer Prometheus

### **Infraestructura & Base de Datos**
* **Motor:** PostgreSQL
* **Hosting de Datos:** Supabase

---

## 🗄️ Arquitectura de Datos

El sistema utiliza un modelo relacional robusto para garantizar la integridad de la información:

### **Entidades Principales**
1.  **Usuarios:** Gestión de credenciales y perfiles.
2.  **Categorías:** Clasificación lógica de los artículos.
3.  **Productos:** Información detallada de los activos de la empresa.
4.  **Movimientos de Inventario:** Registro de transacciones (Kardex).
5.  **AuditLog:** Registro centralizado de eventos de auditoría (operación, usuario, endpoint, IP, cambios antes/después).

### **Relaciones (ER)**
* `Usuarios (1) ──── (N) Movimientos_Inventario`
* `Categorías (1) ──── (N) Productos`
* `Productos (1) ──── (N) Movimientos_Inventario`
* `Usuarios (1) ──── (N) AuditLog`

---

## 🔎 Auditoría y Monitoreo (Resumen Operativo)

### **Componentes implementados**
* **ApiLoggingAspect:** Intercepta todas las APIs REST bajo `/api/**` y registra entrada/salida, usuario, IP, endpoint y tiempo de respuesta.
* **AuditService + AuditLogRepository:** Persisten y consultan auditoría de eventos de negocio (CREATE, UPDATE, DELETE, LOGIN).
* **AuditLogController:** Exposición de endpoints para consultar historial de auditoría.
* **Actuator:** Exposición de estado de la aplicación y métricas de runtime.

### **Endpoints principales**
* **Autenticación:** `POST /api/auth/login`, `POST /api/auth/register`
* **Usuarios:** `GET/POST/PUT/DELETE /api/usuarios`
* **Auditoría:** `GET /api/audit-logs` y filtros por entidad, usuario, registro y rango de fechas
* **Monitoreo:** `GET /actuator/health`, `GET /actuator/metrics`, `GET /actuator/prometheus`

### **Guía breve de logs (monitoreo + auditoría)**
* **Log de monitoreo HTTP global:** se hace en `ApiLoggingAspect` y aparece con etiqueta `[STOQ-MONITOR]` para cada request/respuesta de todas las APIs.
* **Log de negocio por operación:** se hace en servicios (`UsuarioServiceImpl`, `ProductoServiceImpl`, `CategoriaServiceImpl`, `UnidadServiceImpl`, `MovimientoInventarioServiceImpl`) con etiqueta `[STOQ-BIZ]`.
* **Log de auditoría persistida:** se hace con `auditService.registrarAuditoria(...)` y se confirma en consola con etiqueta `[STOQ-AUDIT]`; además queda guardado en tabla `audit_logs`.

Ejemplo mínimo en servicios:

```java
auditService.registrarAuditoria("Producto", "UPDATE", id, estadoAnterior, estadoNuevo);
```

Con esto, al ejecutar localmente o en Render verás logs llamativos en consola y también trazabilidad histórica en base de datos.

---

## Equipo

-Robert Ortiz
-Harold Sejas
-Osthin Colque
-Luis Aguilar

## Compilación del Proyecto

./gradlew build

## Pruebas unitarias

./gradlew test

## Ejecución del Servidor (Desarrollo)

./gradlew bootRun
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
* **Auditoría de Movimientos:** Historial detallado de operaciones, permitiendo trazabilidad (quién, cuándo y qué se movió).
* **Dashboard Operativo:** Panel principal con indicadores visuales sobre el estado actual del inventario.

---

## 🛠️ Stack Tecnológico

### **Backend**
* **Framework:** Spring Boot
* **Lenguaje:** Java
* **Gestión de Dependencias:** Gradle (Kotlin DSL)
* **Persistencia:** Spring Data JPA

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

### **Relaciones (ER)**
* `Usuarios (1) ──── (N) Movimientos_Inventario`
* `Categorías (1) ──── (N) Productos`
* `Productos (1) ──── (N) Movimientos_Inventario`

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
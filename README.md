STOQ - Sistema de Gestión de Inventario
Descripción

STOQ es un sistema de información web diseñado para optimizar la gestión de inventarios en pequeñas y medianas empresas.

El sistema permite digitalizar y centralizar el control de productos, entradas y salidas de inventario, facilitando el monitoreo del stock en tiempo real y la generación de reportes para la toma de decisiones.

Muchas organizaciones gestionan su inventario mediante hojas de cálculo, registros manuales o sistemas incompletos, lo que puede generar desorden, falta de visibilidad del stock real, errores humanos y pérdidas económicas.

STOQ busca solucionar estos problemas proporcionando una plataforma moderna que mejore la eficiencia operativa y permita mantener un control confiable del inventario.

Características

Autenticación

Registro y login de usuarios.

Control de acceso seguro al sistema.

Gestión de Productos

Registro y administración de productos.

Asociación de productos con categorías.

Control de Inventario

Registro de entradas y salidas de productos.

Actualización automática del stock.

Historial de Movimientos

Registro de movimientos de inventario.

Seguimiento de quién realizó cada operación.

Panel Principal

Vista general del estado del inventario

Tecnologías
Frontend

Angular

TypeScript

HTML

CSS

Bootstrap

Para iniciar el servidor de desarrollo local:
ng serve

Backend

El backend está desarrollado con Spring Boot y utiliza Supabase (PostgreSQL) como base de datos.

Tecnologías Backend

Java

Spring Boot

Spring Data JPA

Gradle

Kotlin DSL (build.gradle.kts)

PostgreSQL

Supabase
Base de Datos

La base de datos está alojada en Supabase, que utiliza PostgreSQL.

Principales entidades del sistema:

usuarios

categorias

productos

movimientos_inventario

Relaciones:
usuarios (1) ──── (N) movimientos_inventario

categorias (1) ──── (N) productos

productos (1) ──── (N) movimientos_inventario

Equipo

Robert Ortiz

Harold Sejas

Ostin Colque

Luis Aguilar

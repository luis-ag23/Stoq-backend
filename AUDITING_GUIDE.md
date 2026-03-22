# 📋 Guía de Auditoría y Monitoreo - STOQ Backend

## 📌 Resumen de Implementación

Se ha completado la implementación de un sistema integral de auditoría y monitoreo para la API de STOQ:

### ✅ Componentes Implementados

1. **ApiLoggingAspect** - Intercepta todos los endpoints REST
2. **AuditLog Entity** - Base de datos centralizada de auditoría
3. **AuditService** - Servicio reutilizable para registrar cambios
4. **AuditLogController** - API REST para consultar auditoría
5. **Spring Boot Actuator** - Endpoints de salud y métricas
6. **Prometheus Integration** - Recopilación de métricas
7. **JPA Auditing** - Captura automática de usuario y fecha

---

## 🔍 1. Logging Automático de API

### Ubicación
`src/main/java/com/Proyecto/stoq/application/aspects/ApiLoggingAspect.java`

### Características
- ✅ Intercepta TODOS los endpoints REST automáticamente
- ✅ Registra usuario autenticado (extraído del JWT)
- ✅ Captura IP, User-Agent, timestamp
- ✅ Mide tiempo de ejecución
- ✅ Registra errores con stack trace

### Ejemplo de Log
```
[API AUDIT] 2026-03-22 14:30:45 | POST /api/usuarios | User: user@example.com | IP: 192.168.1.1 | User-Agent: Mozilla/5.0...
[API PERFORMANCE] POST /api/usuarios completed in 125 ms
```

---

## 📊 2. Auditoría de Base de Datos

### Ubicación
`src/main/java/com/Proyecto/stoq/domain/model/AuditLog.java`

### Campos Registrados
| Campo | Descripción |
|-------|-------------|
| `id` | UUID único del evento |
| `entidad` | Nombre de la entidad (Usuario, Producto, etc.) |
| `operacion` | Tipo de operación (CREATE, UPDATE, DELETE) |
| `idRegistro` | ID del registro afectado |
| `cambiosAnterior` | JSON con valores anteriores |
| `cambiosNuevo` | JSON con valores nuevos |
| `createdBy` | Usuario que realizó la acción |
| `createdDate` | Timestamp automático |
| `ipOrigen` | IP desde donde se hizo la solicitud |
| `userAgent` | Navegador/cliente |
| `endpoint` | Método HTTP + URI |

### Estructura JSON en Base de Datos
```sql
SELECT * FROM audit_logs;
```

---

## 🔧 3. Cómo Usar AuditService en Tus Servicios

### Inyectar en el Servicio
```java
@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    private final AuditService auditService;
    
    public UsuarioServiceImpl(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @Override
    public Usuario crearUsuario(CreateUsuarioDTO dto) {
        // Lógica de creación...
        Usuario usuario = new Usuario(...);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        // Registrar en auditoría
        auditService.registrarAuditoria(
            "Usuario",              // Entidad
            "CREATE",              // Operación
            savedUsuario.getId(),  // ID del registro
            null,                  // Antes (null para CREATE)
            savedUsuario           // Después
        );
        
        return savedUsuario;
    }
    
    @Override
    public Usuario actualizarUsuario(UUID id, UpdateUsuarioDTO dto) {
        Usuario usuarioAnterior = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Guardar estado anterior
        Usuario usuarioAntes = new Usuario(usuarioAnterior); // Copia
        
        // Actualizar...
        usuarioAnterior.setNombre(dto.nombre);
        // ... más cambios
        
        Usuario usuarioActualizado = usuarioRepository.save(usuarioAnterior);
        
        // Registrar cambios
        auditService.registrarAuditoria(
            "Usuario",
            "UPDATE",
            usuarioActualizado.getId(),
            usuarioAntes,          // Estado anterior
            usuarioActualizado     // Estado nuevo
        );
        
        return usuarioActualizado;
    }
    
    @Override
    public void eliminarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuarioRepository.deleteById(id);
        
        // Registrar eliminación
        auditService.registrarAuditoria(
            "Usuario",
            "DELETE",
            id,
            usuario  // Guardar el registro eliminado
        );
    }
}
```

---

## 📡 4. API REST de Auditoría

### Endpoints Disponibles

#### Obtener todos los logs
```bash
GET /api/audit-logs
```

#### Logs por entidad
```bash
GET /api/audit-logs/entidad/Usuario
GET /api/audit-logs/entidad/Producto
```

#### Logs por usuario
```bash
GET /api/audit-logs/usuario/user@example.com
```

#### Cambios en un registro específico
```bash
GET /api/audit-logs/registro/Usuario/{idUsuario}
```

#### Logs por rango de fechas
```bash
GET /api/audit-logs/rango?inicio=2026-03-22%2000:00:00&fin=2026-03-22%2023:59:59
```

#### Log específico
```bash
GET /api/audit-logs/{idLog}
```

### Response Ejemplo
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "entidad": "Usuario",
  "operacion": "UPDATE",
  "idRegistro": "123e4567-e89b-12d3-a456-426614174000",
  "cambiosAnterior": "{\"nombre\":\"Juan\",\"correo\":\"juan@example.com\"}",
  "cambiosNuevo": "{\"nombre\":\"Juan Actualizado\",\"correo\":\"juan@example.com\"}",
  "createdBy": "user@example.com",
  "createdDate": "2026-03-22T14:30:45",
  "ipOrigen": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "endpoint": "PUT /api/usuarios/123e4567-e89b-12d3-a456-426614174000"
}
```

---

## 📊 5. Spring Boot Actuator - Salud y Métricas

### Endpoints Habilitados

#### Health Check
```bash
GET /actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

#### Métricas
```bash
GET /actuator/metrics
```

#### Prometheus Metrics
```bash
GET /actuator/prometheus
```

#### Variables de Entorno
```bash
GET /actuator/env
```

#### Beans Registrados
```bash
GET /actuator/beans
```

#### Thread Dump
```bash
GET /actuator/threaddump
```

---

## 🔐 6. Configuración en application.properties

```properties
# Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus,env,beans,threaddump,loggers
management.endpoint.health.show-details=always
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true

# Logging
logging.level.com.Proyecto.stoq=DEBUG
logging.level.com.Proyecto.stoq.application.aspects.ApiLoggingAspect=INFO
logging.file.name=logs/stoq-application.log
```

---

## 📁 7. Archivos Nuevos Creados

```
src/main/java/com/Proyecto/stoq/
├── infrastructure/
│   ├── AuditingConfig.java          // Configuración JPA Auditing
│   └── JacksonConfig.java           // ObjectMapper para JSON
├── domain/model/
│   └── AuditLog.java                // Entidad de auditoría
├── adapters/
│   ├── repositories/
│   │   └── AuditLogRepository.java  // Acceso a datos
│   └── controllers/
│       └── AuditLogController.java  // API REST
├── application/services/
│   └── AuditService.java            // Servicio de auditoría
└── dto/
    └── AuditLogDTO.java             // DTO de respuesta

src/main/resources/
└── logs/                            // Directorio de logs
```

---

## 🚀 8. Flujo Completo de Auditoría

```
Cliente REST API
       │
       ▼
JwtFilter (Autenticación)
       │
       ▼
ApiLoggingAspect (Intercepta)
  ├─ Extrae usuario del JWT
  ├─ Registra en logs
  └─ Captura IP y User-Agent
       │
       ▼
Endpoint del Controlador
       │
       ▼
Servicio (AuditService.registrarAuditoria)
  ├─ Serializa cambios a JSON
  ├─ Obtiene información del contexto
  └─ Guarda en BD (AuditLog)
       │
       ▼
Base de Datos PostgreSQL
```

---

## 📈 9. Monitoreo en Tiempo Real

### Ver logs en vivo
```bash
tail -f logs/stoq-application.log
```

### Filtrar logs de auditoría
```bash
grep "\[API AUDIT\]" logs/stoq-application.log
```

### Filtrar errores
```bash
grep "\[API ERROR\]" logs/stoq-application.log
```

### Acceder a Prometheus
```
http://localhost:8080/actuator/prometheus
```

---

## ✅ Checklist de Auditoría

- [x] Logging automático de todos los endpoints
- [x] Extracción de usuario autenticado en logs
- [x] Registro de cambios en base de datos
- [x] API REST para consultar auditoría
- [x] Métricas y salud del sistema
- [x] Timestamps automáticos
- [x] Captura de IP y User-Agent
- [x] Manejo de errores sin interrumpir flujo

---

## 🔍 Próximas Mejoras Opcionales

1. **Jaeger Tracing** - Trazabilidad distribuida
2. **Grafana** - Visualización de métricas
3. **ELK Stack** - Análisis centralizado de logs
4. **Alertas automáticas** - Notificaciones en eventos críticos
5. **Encriptación de auditoría** - Archivos de auditoría firmados

---

## 📞 Soporte

Para más información sobre los componentes:
- ApiLoggingAspect: Aspectos de Spring
- AuditLog: Jpa Entities
- Actuator: Spring Boot Actuator Docs
- Prometheus: Micrometer Prometheus Registry

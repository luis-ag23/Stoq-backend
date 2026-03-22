# 📖 EJEMPLOS PRÁCTICOS DE INTEGRACIÓN AUDITORÍA

## 1️⃣ Ejemplo: Servicio de Productos con Auditoría

### Paso 1: Inyectar AuditService

```java
package com.Proyecto.stoq.application.services;

import com.Proyecto.stoq.application.services.AuditService;
import com.Proyecto.stoq.domain.model.Producto;
import com.Proyecto.stoq.domain.ports.ProductoRepositoryPort;
import com.Proyecto.stoq.dto.CreateProductoDTO;
import com.Proyecto.stoq.dto.UpdateProductoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepositoryPort productoRepository;
    private final AuditService auditService;  // ← INJECTOR AQUÍ

    public ProductoServiceImpl(
        ProductoRepositoryPort productoRepository,
        AuditService auditService) {  // ← CONSTRUCTOR
        this.productoRepository = productoRepository;
        this.auditService = auditService;
    }

    // ... resto del servicio
}
```

### Paso 2: Registrar en CREAR

```java
@Override
public Producto crearProducto(CreateProductoDTO dto) {
    // Validar que no exista
    if (productoRepository.findByNombre(dto.nombre).isPresent()) {
        throw new RuntimeException("Producto ya existe");
    }

    // Crear producto
    Producto producto = new Producto();
    producto.setNombre(dto.nombre);
    producto.setDescripcion(dto.descripcion);
    producto.setPrecio(dto.precio);
    producto.setStock(dto.stock);

    // Guardar en BD
    Producto productoGuardado = productoRepository.save(producto);

    // ✅ REGISTRAR EN AUDITORÍA
    auditService.registrarAuditoria(
        "Producto",                    // Entidad
        "CREATE",                      // Operación
        productoGuardado.getId(),      // ID del registro
        null,                          // Antes (null para CREATE)
        productoGuardado               // Después
    );

    return productoGuardado;
}
```

**Resultado en Base de Datos:**
```json
{
  "entidad": "Producto",
  "operacion": "CREATE",
  "idRegistro": "550e8400-e29b-41d4-...",
  "cambiosAnterior": null,
  "cambiosNuevo": "{\"nombre\":\"Laptop\",\"precio\":999.99,\"stock\":5}",
  "createdBy": "admin@stoq.com",
  "createdDate": "2026-03-22T10:30:45",
  "ipOrigen": "192.168.1.1",
  "userAgent": "Postman/10.0",
  "endpoint": "POST /api/productos"
}
```

### Paso 3: Registrar en ACTUALIZAR

```java
@Override
public Producto actualizarProducto(UUID id, UpdateProductoDTO dto) {
    // Buscar producto
    Producto producto = productoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    // 📸 GUARDAR ESTADO ANTERIOR para auditoría
    Producto productoAnterior = copiarProducto(producto);

    // Actualizar campos
    if (dto.nombre != null) {
        producto.setNombre(dto.nombre);
    }
    if (dto.precio != null) {
        producto.setPrecio(dto.precio);
    }
    if (dto.stock != null) {
        producto.setStock(dto.stock);
    }

    // Guardar cambios
    Producto productoActualizado = productoRepository.save(producto);

    // ✅ REGISTRAR EN AUDITORÍA
    auditService.registrarAuditoria(
        "Producto",                    // Entidad
        "UPDATE",                      // Operación
        productoActualizado.getId(),   // ID del registro
        productoAnterior,              // Antes (estado anterior)
        productoActualizado            // Después (estado nuevo)
    );

    return productoActualizado;
}

// Método auxiliar para copiar objeto
private Producto copiarProducto(Producto original) {
    Producto copia = new Producto();
    copia.setId(original.getId());
    copia.setNombre(original.getNombre());
    copia.setPrecio(original.getPrecio());
    copia.setStock(original.getStock());
    return copia;
}
```

**Resultado en Base de Datos:**
```json
{
  "entidad": "Producto",
  "operacion": "UPDATE",
  "idRegistro": "550e8400-e29b-41d4-...",
  "cambiosAnterior": "{\"nombre\":\"Laptop\",\"precio\":999.99}",
  "cambiosNuevo": "{\"nombre\":\"Laptop Pro\",\"precio\":1299.99}",
  "createdBy": "admin@stoq.com",
  "createdDate": "2026-03-22T11:45:30",
  "ipOrigen": "192.168.1.1",
  "userAgent": "Postman/10.0",
  "endpoint": "PUT /api/productos/550e8400-e29b-41d4-..."
}
```

### Paso 4: Registrar en ELIMINAR

```java
@Override
public void eliminarProducto(UUID id) {
    // Buscar producto
    Producto producto = productoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    // Eliminar de BD
    productoRepository.deleteById(id);

    // ✅ REGISTRAR EN AUDITORÍA (guardar lo que se eliminó)
    auditService.registrarAuditoria(
        "Producto",                    // Entidad
        "DELETE",                      // Operación
        id,                            // ID del registro
        producto,                      // Antes (el producto eliminado)
        null                           // Después (null para DELETE)
    );
}
```

**Resultado en Base de Datos:**
```json
{
  "entidad": "Producto",
  "operacion": "DELETE",
  "idRegistro": "550e8400-e29b-41d4-...",
  "cambiosAnterior": "{\"nombre\":\"Laptop Pro\",\"precio\":1299.99,\"stock\":2}",
  "cambiosNuevo": null,
  "createdBy": "admin@stoq.com",
  "createdDate": "2026-03-22T12:15:00",
  "ipOrigen": "192.168.1.1",
  "userAgent": "Postman/10.0",
  "endpoint": "DELETE /api/productos/550e8400-e29b-41d4-..."
}
```

---

## 2️⃣ Ejemplo: Consultar Auditoría con REST API

### Obtener todos los cambios de un producto

```bash
curl -X GET "http://localhost:8080/api/audit-logs/registro/Producto/550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6",
    "entidad": "Producto",
    "operacion": "CREATE",
    "idRegistro": "550e8400-e29b-41d4-a716-446655440000",
    "cambiosAnterior": null,
    "cambiosNuevo": "{\"nombre\":\"Laptop\",\"precio\":999.99,\"stock\":5}",
    "createdBy": "admin@stoq.com",
    "createdDate": "2026-03-22T10:30:45",
    "ipOrigen": "192.168.1.1",
    "userAgent": "Postman/10.0",
    "endpoint": "POST /api/productos"
  },
  {
    "id": "b2c3d4e5-f6g7-48h9-i0j1-k2l3m4n5o6p7",
    "entidad": "Producto",
    "operacion": "UPDATE",
    "idRegistro": "550e8400-e29b-41d4-a716-446655440000",
    "cambiosAnterior": "{\"nombre\":\"Laptop\",\"precio\":999.99}",
    "cambiosNuevo": "{\"nombre\":\"Laptop Pro\",\"precio\":1299.99}",
    "createdBy": "admin@stoq.com",
    "createdDate": "2026-03-22T11:45:30",
    "ipOrigen": "192.168.1.1",
    "userAgent": "Postman/10.0",
    "endpoint": "PUT /api/productos/550e8400-e29b-41d4-..."
  }
]
```

### Ver cambios hechos por un usuario específico

```bash
curl -X GET "http://localhost:8080/api/audit-logs/usuario/admin@stoq.com" \
  -H "Authorization: Bearer ..."
```

### Ver cambios en un rango de fechas

```bash
curl -X GET "http://localhost:8080/api/audit-logs/rango?inicio=2026-03-22%2000:00:00&fin=2026-03-22%2023:59:59" \
  -H "Authorization: Bearer ..."
```

---

## 3️⃣ Ejemplo: Ver Logs en Archivo

### Archivo generado: `logs/stoq-application.log`

```
2026-03-22 10:30:45 [http-nio-8080-exec-1] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API AUDIT] 2026-03-22 10:30:45 | POST /api/productos | User: admin@stoq.com | IP: 192.168.1.1 | User-Agent: Postman/10.0

2026-03-22 10:30:46 [http-nio-8080-exec-1] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API PERFORMANCE] POST /api/productos completed in 145 ms

2026-03-22 11:45:30 [http-nio-8080-exec-2] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API AUDIT] 2026-03-22 11:45:30 | PUT /api/productos/550e8400-e29b-41d4-a716-446655440000 | User: admin@stoq.com | IP: 192.168.1.1 | User-Agent: Postman/10.0

2026-03-22 11:45:31 [http-nio-8080-exec-2] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API PERFORMANCE] PUT /api/productos/550e8400-e29b-41d4-a716-446655440000 completed in 89 ms

2026-03-22 12:15:00 [http-nio-8080-exec-3] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API AUDIT] 2026-03-22 12:15:00 | DELETE /api/productos/550e8400-e29b-41d4-a716-446655440000 | User: admin@stoq.com | IP: 192.168.1.1 | User-Agent: Postman/10.0

2026-03-22 12:15:01 [http-nio-8080-exec-3] INFO  com.Proyecto.stoq.application.aspects.ApiLoggingAspect - [API PERFORMANCE] DELETE /api/productos/550e8400-e29b-41d4-a716-446655440000 completed in 67 ms
```

### Filtrar solo auditorías en Linux/Mac

```bash
tail -f logs/stoq-application.log | grep "\[API AUDIT\]"
```

### Windows PowerShell

```powershell
Get-Content logs/stoq-application.log -Wait | Select-String "\[API AUDIT\]"
```

---

## 4️⃣ Ejemplo: Monitorear Salud del Sistema

### Endpoint de salud

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
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
        "total": 1099511627776,
        "free": 549755813888,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

### Métricas disponibles

```bash
curl http://localhost:8080/actuator/metrics
```

**Response parcial:**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.gc.pause",
    "process.cpu.usage",
    "system.cpu.usage",
    "http.server.requests",
    "http.server.requests.seconds"
  ]
}
```

### Métrica específica (requests HTTP)

```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## 5️⃣ Patrón Recomendado para Otros Servicios

```java
@Service
public class TuServicioImpl implements TuServicio {

    private final TuRepositorio repository;
    private final AuditService auditService;

    public TuServicioImpl(TuRepositorio repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    // CREATE
    public TuEntidad crear(CreateDTO dto) {
        TuEntidad entidad = mapper.toEntity(dto);
        TuEntidad guardada = repository.save(entidad);
        auditService.registrarAuditoria("TuEntidad", "CREATE", 
            guardada.getId(), null, guardada);
        return guardada;
    }

    // UPDATE
    public TuEntidad actualizar(UUID id, UpdateDTO dto) {
        TuEntidad antes = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("No encontrado"));
        
        TuEntidad copia = copiar(antes);
        
        mapper.updateEntity(dto, antes);
        TuEntidad actualizada = repository.save(antes);
        
        auditService.registrarAuditoria("TuEntidad", "UPDATE", 
            actualizada.getId(), copia, actualizada);
        return actualizada;
    }

    // DELETE
    public void eliminar(UUID id) {
        TuEntidad entidad = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("No encontrado"));
        
        repository.deleteById(id);
        auditService.registrarAuditoria("TuEntidad", "DELETE", 
            id, entidad, null);
    }

    // Método auxiliar
    private TuEntidad copiar(TuEntidad original) {
        TuEntidad copia = new TuEntidad();
        // Copiar todos los campos
        return copia;
    }
}
```

---

## 🎯 Flujo Completo de Una Solicitud

```
1. Cliente HTTP POST /api/productos
   ↓
2. JwtFilter valida JWT
   {
     "sub": "admin@stoq.com",
     "iat": 1710915045,
     "exp": 1711001445
   }
   ↓
3. ApiLoggingAspect intercepta
   - Extrae: admin@stoq.com (del JWT)
   - Captura: IP, User-Agent, timestamp
   - Log: [API AUDIT] ... User: admin@stoq.com
   ↓
4. ProductoController.crear() → ProductoService.crear()
   ↓
5. ProductoRepository.save(producto)
   → INSERTAR en BD
   ↓
6. AuditService.registrarAuditoria()
   - Obtiene usuario actual: admin@stoq.com
   - Obtiene IP: 192.168.1.1
   - Obtiene endpoint: POST /api/productos
   - Serializa cambios a JSON
   - INSERTAR en audit_logs
   ↓
7. Log: [API PERFORMANCE] ... completed in 145 ms
   ↓
8. Response 200 OK al cliente

BD FINAL:
- Tabla productos: 1 nuevo registro
- Tabla audit_logs: 1 nuevo evento completo
- Archivo logs/stoq-application.log: 2 líneas de audit
```

---

## ✅ Checklist de Implementación en tu Servicio

- [ ] Inyectar `AuditService` en el constructor
- [ ] En CREATE: `auditService.registrarAuditoria(..., null, guardada)`
- [ ] En UPDATE: Copiar estado anterior antes de actualizar
- [ ] En UPDATE: `auditService.registrarAuditoria(..., antes, actualizada)`
- [ ] En DELETE: `auditService.registrarAuditoria(..., eliminada, null)`
- [ ] Testear con Postman
- [ ] Verificar logs en: http://localhost:8080/api/audit-logs

---

**¡Tu servicio ahora está auditado completamente!**

# Sistema de Onboarding y Offboarding de Empleados

Sistema distribuido basado en microservicios para la gestión del ciclo de vida de empleados (onboarding y offboarding), con seguridad JWT, comunicación asincrónica por eventos y pruebas BDD automatizadas.

---

## Arquitectura

```
Cliente
  │
  ▼
API Gateway (:8085)          ← único punto de entrada, valida JWT
  ├── /auth/**       ──────► auth-service (:8082)           [público]
  ├── /empleados/**  ──────► empleados-service (:8080)       [protegido]
  ├── /departamentos/** ───► departamentos-service (:8081)   [protegido]
  ├── /perfiles/**   ──────► perfiles-service (:8083)       [protegido]
  └── /notificaciones/** ──► notificaciones-service (:8084)  [protegido]

empleados-service ──(RabbitMQ)──► auth-service ──► crea credenciales USER
                   ──(RabbitMQ)──► notificaciones-service ──► email bienvenida
                   ──(RabbitMQ)──► perfiles-service ──► perfil por defecto
```

## Resumen de Retos

| Reto | Tema | Estado |
|------|------|--------|
| 1 | Servidor web CRUD empleados | ✅ Completado |
| 2 | Microservicios independientes + Docker Compose | ✅ Completado |
| 3 | Comunicación asincrónica con RabbitMQ (Fan-Out) | ✅ Completado |
| 4 | Seguridad JWT centralizada + RBAC | ✅ Completado |
| 5 | Pruebas BDD con Cucumber (21 escenarios) | ✅ Completado |

---

## Evolución de la arquitectura

### Reto 1 — Servidor Web Básico
Un solo servidor Java con endpoints CRUD para empleados (en memoria).

### Reto 2 — Microservicios + Docker Compose
Separación en 5 microservicios independientes con persistencia PostgreSQL cada uno:

| Servicio | Responsabilidad |
|----------|----------------|
| **empleados-service** | CRUD de empleados, valida que el departamento exista |
| **departamentos-service** | CRUD de departamentos |
| **auth-service** | Login, registro de usuarios, recuperación de contraseña |
| **perfiles-service** | Perfiles de empleados (creado automáticamente al onboarding) |
| **notificaciones-service** | Registro de notificaciones por evento |
| **gateway** | API Gateway con routing y validación JWT |

### Reto 3 — Comunicación Asincrónica (RabbitMQ)
El servicio de empleados publica eventos a un `TopicExchange` en RabbitMQ:

- **empleado.creado** → auth-service crea credenciales, notificaciones envía bienvenida, perfiles crea perfil
- **empleado.eliminado** → notificaciones registra desvinculación

### Reto 4 — Seguridad JWT + RBAC
Validación centralizada en el Gateway:
- **ADMIN**: acceso total (GET, POST, DELETE)
- **USER**: solo lectura (GET)

### Reto 5 — Pruebas BDD Automatizadas
Suite de 21 escenarios en Gherkin (español) con Cucumber-JVM y Rest Assured que validan onboarding, offboarding, seguridad y comunicación asincrónica.

---

## Control de Acceso (RBAC)

| Rol | Permisos |
|-----|----------|
| ADMIN | GET, POST, DELETE (todo) |
| USER | Solo GET |

- **401 Unauthorized** — token ausente, inválido o expirado
- **403 Forbidden** — rol sin permiso para el método HTTP

## Cómo obtener un token

```bash
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@empresa.com", "password": "admin123"}'
```

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "rol": "ADMIN"
}
```

Usarlo en peticiones protegidas:

```bash
curl http://localhost:8085/empleados \
  -H "Authorization: Bearer <token>"
```

## Flujo de eventos de autenticación

```
empleados-service
  │── empleado.creado ──► auth-service  → crea usuario (rol USER)
  │                     → publica usuario.creado
  │                           └──► notificaciones-service
  │                                (email: establecer contraseña)
  │
  └── empleado.eliminado ► auth-service → desactiva usuario

auth-service
  └── usuario.recuperacion ──► notificaciones-service
                                (email: recuperar contraseña)
```

## Swagger UI

Cada servicio expone Swagger en su puerto directo (solo desarrollo):

| Servicio | URL |
|----------|-----|
| Auth | http://localhost:8082/swagger-ui.html |
| Empleados | http://localhost:8080/swagger-ui.html |
| Departamentos | http://localhost:8081/swagger-ui.html |
| Perfiles | http://localhost:8083/swagger-ui.html |
| Notificaciones | http://localhost:8084/swagger-ui.html |

## Variables de entorno

```bash
cp .env.example .env
```

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `JWT_SECRET` | Clave HMAC-SHA256 (mín. 32 chars) | *(requerido)* |
| `JWT_EXPIRATION_MS` | Duración token acceso | `86400000` (24h) |
| `JWT_RECOVERY_EXPIRATION_MS` | Duración token recuperación | `3600000` (1h) |
| `RABBITMQ_HOST` | Host del broker | `message-broker` |
| `RABBITMQ_USER` | Usuario RabbitMQ | `admin` |
| `RABBITMQ_PASS` | Contraseña RabbitMQ | `admin` |

## Servicios y puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| gateway | 8085 | Punto de entrada único |
| auth-service | 8082 | Autenticación y usuarios |
| empleados-service | 8080 | Gestión de empleados |
| departamentos-service | 8081 | Gestión de departamentos |
| perfiles-service | 8083 | Perfiles de empleados |
| notificaciones-service | 8084 | Registro de notificaciones |
| RabbitMQ Management | 15672 | UI del broker |

---

## Pruebas BDD (Reto 5)

### ¿Qué es BDD?

**Behavior-Driven Development (BDD)** es una metodología que enfoca las pruebas en el **comportamiento** del sistema desde la perspectiva del usuario, usando lenguaje natural (Gherkin). Los archivos `.feature` son **documentación viva**: si la prueba pasa, el comportamiento documentado es real.

### Escenarios implementados (21 total)

#### 1. Verificación del sistema (2 escenarios)
- Sistema responde correctamente sin token (401)
- API Gateway accesible

#### 2. Seguridad y control de acceso (8 escenarios)
- Acceso denegado sin token (401)
- Acceso denegado con token inválido (401)
- Acceso denegado con token malformado (401)
- USER no puede crear empleados (403)
- USER no puede eliminar empleados (403)
- USER puede consultar empleados (200)
- ADMIN puede crear empleados (201)
- ADMIN puede eliminar empleados (204)

#### 3. Onboarding de empleados (6 escenarios)
- Registro exitoso → credenciales creadas vía evento (polling)
- Registro exitoso → notificación "bienvenida" (polling)
- Nuevo empleado establece contraseña y hace login (polling)
- Registro con departamento inexistente → error 400
- Registro con campos faltantes → error 400
- Listar empleados después de registro exitoso

#### 4. Offboarding de empleados (5 escenarios)
- Desvinculación completa → notificación "desvinculación" (polling)
- Empleado desvinculado no puede hacer login (polling)
- Recuperación de contraseña falla para usuario desvinculado
- Eliminar empleado que no existe → 404
- Offboarding múltiple de empleados independientes

### Estrategia de Polling

El sistema usa **eventual consistencia** (RabbitMQ). No se puede verificar inmediatamente el resultado de un evento. Se usa polling con reintentos:

| Parámetro | Valor | Justificación |
|-----------|-------|---------------|
| Intentos máximos | 10 | Suficiente para procesamiento de eventos |
| Intervalo | 2000ms | Balance entre velocidad y carga |
| Timeout total | ~20s | Límite razonable para asincronía |

**No usamos `Thread.sleep()` fijo** porque el polling termina más rápido si el evento se procesa antes y tolera variaciones sin fallar.

### Cómo ejecutar las pruebas

#### Paso 1: Levantar el sistema

```bash
docker-compose up --build -d
```

Esperar ~2-3 minutos a que todos los servicios estén healthy. Verificar:

```bash
docker-compose ps
```

#### Paso 2: Ejecutar las pruebas

```bash
cd e2e-tests
mvn verify
```

#### Paso 3: Interpretar resultados

**Consola**: `Tests run: 21, Failures: 0, Errors: 0`

**Reportes en `e2e-tests/target/`**:

| Archivo | Descripción |
|---------|-------------|
| `cucumber-report.html` | Reporte visual en navegador |
| `cucumber-report.json` | Formato JSON para CI/CD |

### Herramientas utilizadas

| Herramienta | Propósito | Justificación |
|-------------|-----------|---------------|
| **Cucumber-JVM** | Framework BDD | Estándar de la industria, soporte Gherkin en español |
| **Rest Assured** | Cliente HTTP | API fluida, ideal para pruebas REST en Java |
| **JUnit** | Runner | Integración nativa con Cucumber, reportes XML |

**Por qué Java**: Los microservicios están en Java/Spring Boot. Mantener consistencia tecnológica simplifica el desarrollo y mantenimiento.

### Aislamiento entre escenarios

- Cada escenario usa **IDs únicos** basados en timestamps
- Emails: `tipo.test.{timestamp}@test.com`
- Hook `@After` limpia `TestContext` después de cada escenario
- No hay dependencias entre escenarios

### Simular un fallo

Cambiar un código esperado en un archivo `.feature`:

```gherkin
# En 02_seguridad.feature
Entonces la respuesta debe tener código 200  # era 401
```

El resultado mostrará un mensaje descriptivo:

```
Expected: 200
Actual: 401
```

---

## Guía rápida de demostración

Para una presentación completa del sistema, consultar [`GUIA_PRESENTACION.md`](GUIA_PRESENTACION.md).

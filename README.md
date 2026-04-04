# Reto 4 — Seguridad y Control de Acceso con JWT

## Arquitectura

```
Cliente
  │
  ▼
API Gateway (:8085)          ← único punto de entrada, valida JWT
  ├── /auth/**       ──────► auth-service (:8082)        [público]
  ├── /empleados/**  ──────► empleados-service (:8080)   [protegido]
  ├── /departamentos/** ───► departamentos-service (:8081)[protegido]
  ├── /perfiles/**   ──────► perfiles-service (:8083)    [protegido]
  └── /notificaciones/** ──► notificaciones-service (:8084)[protegido]
```

## Estrategia de validación: API Gateway centralizado

Se optó por el **API Gateway** (Spring Cloud Gateway) como punto único de validación JWT.
Cada petición entrante pasa por el filtro `JwtAuthFilter` antes de ser enrutada al microservicio destino.
Los microservicios internos no necesitan Spring Security — confían en que el gateway ya validó el token.

**Ventaja clave para el crecimiento:** agregar un nuevo microservicio solo requiere una ruta en `application.yml` del gateway. No hay que tocar el nuevo servicio para protegerlo.

## Control de Acceso (RBAC)

| Rol   | Permisos                          |
|-------|-----------------------------------|
| ADMIN | GET, POST, PUT, DELETE (todo)     |
| USER  | Solo GET (solo lectura)           |

- `401 Unauthorized` — no se envió token o el token es inválido/expirado
- `403 Forbidden` — el rol no tiene permiso para el método HTTP solicitado

## Cómo obtener un token para pruebas

### 1. Login con el usuario ADMIN (creado automáticamente al iniciar)

```bash
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@empresa.com", "password": "admin123"}'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "rol": "ADMIN"
}
```

### 2. Usar el token en peticiones protegidas

```bash
curl http://localhost:8085/empleados \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 3. Probar desde Swagger

Cada servicio expone Swagger en su puerto directo (solo para desarrollo):
- Auth:           http://localhost:8082/swagger-ui.html
- Empleados:      http://localhost:8080/swagger-ui.html
- Departamentos:  http://localhost:8081/swagger-ui.html
- Perfiles:       http://localhost:8083/swagger-ui.html
- Notificaciones: http://localhost:8084/swagger-ui.html

En Swagger, haz clic en **Authorize** e ingresa el token obtenido del login.

## Variables de entorno

Copia `.env.example` a `.env` y ajusta los valores:

```bash
cp .env.example .env
```

| Variable                    | Descripción                                      | Valor por defecto          |
|-----------------------------|--------------------------------------------------|----------------------------|
| `JWT_SECRET`                | Clave simétrica HMAC-SHA256 (mín. 32 caracteres) | *(requerido)*              |
| `JWT_EXPIRATION_MS`         | Duración del token de acceso en ms               | `86400000` (24h)           |
| `JWT_RECOVERY_EXPIRATION_MS`| Duración del token de recuperación en ms         | `3600000` (1h)             |
| `RABBITMQ_HOST`             | Host del broker                                  | `message-broker`           |
| `RABBITMQ_USER`             | Usuario RabbitMQ                                 | `admin`                    |
| `RABBITMQ_PASS`             | Contraseña RabbitMQ                              | `admin`                    |

> **Importante:** Nunca subas el archivo `.env` real al repositorio. Está incluido en `.gitignore`.

## Flujo de eventos de autenticación

```
empleados-service
  │── empleado.creado ──► auth-service  → crea usuario (rol USER)
  │                                     → publica usuario.creado
  │                                           └──► notificaciones-service
  │                                                (email: establecer contraseña)
  │
  └── empleado.eliminado ► auth-service → desactiva usuario

auth-service
  └── usuario.recuperacion ──► notificaciones-service
                                (email: recuperar contraseña)
```

## Levantar el sistema

```bash
docker-compose up --build -d
```

## Servicios y puertos

| Servicio             | Puerto externo | Descripción                    |
|----------------------|----------------|--------------------------------|
| gateway              | 8085           | Punto de entrada único         |
| auth-service         | 8082           | Autenticación y usuarios       |
| empleados-service    | 8080           | Gestión de empleados           |
| departamentos-service| 8081           | Gestión de departamentos       |
| perfiles-service     | 8083           | Perfiles de empleados          |
| notificaciones-service| 8084          | Registro de notificaciones     |
| RabbitMQ Management  | 15672          | UI del broker                  |

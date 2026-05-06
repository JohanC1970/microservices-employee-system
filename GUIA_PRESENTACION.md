# Guía de Presentación - Sistema Completo (Retos 1-5)

Esta guía contiene el paso a paso para presentar el sistema de microservicios completo ante tu profesor.

---

## 1. Explicación de la Arquitectura

**Objetivo:** Mostrar la evolución del sistema desde un servidor simple hasta microservicios con seguridad y pruebas automatizadas.

> "Profesor, este es un sistema de onboarding y offboarding de empleados construido progresivamente en 5 retos. Comenzamos con un servidor web simple (Reto 1), lo separamos en microservicios con Docker Compose (Reto 2), agregamos comunicación asincrónica con RabbitMQ (Reto 3), implementamos seguridad JWT con RBAC centralizado en el Gateway (Reto 4) y finalmente automatizamos todas las pruebas con BDD usando Cucumber (Reto 5)."

### Componentes actuales

| Componente | Función |
|-----------|---------|
| **API Gateway** | Punto de entrada único, valida JWT y controla acceso por rol |
| **empleados-service** | CRUD de empleados, valida departamentos |
| **departamentos-service** | CRUD de departamentos |
| **auth-service** | Login, registro de usuarios, recuperación de contraseña |
| **perfiles-service** | Perfiles (creado automáticamente al onboarding) |
| **notificaciones-service** | Registro de notificaciones por evento |
| **RabbitMQ** | Broker de mensajes para comunicación asincrónica |

---

## 2. Demostración: Eventos Asincrónicos (Retos 2 y 3)

*Asegúrate de tener todos los contenedores corriendo (`docker-compose ps`)*.

### Paso 2.1: Crear la dependencia (Departamento)

```bash
curl -s -X POST http://localhost:8081/departamentos \
     -H "Content-Type: application/json" \
     -d '{"id":"DEMO","nombre":"Demo","descripcion":"Demostración"}'
```

### Paso 2.2: Crear el Empleado (detonante del evento)

```bash
curl -s -X POST http://localhost:8080/empleados \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Carlos Lopez","email":"carlos.demo@empresa.com","departamentoId":"DEMO","fechaIngreso":"2023-05-10"}'
```

> "El servicio de Empleados guarda el registro y **de forma asíncrona** publica `empleado.creado` en RabbitMQ."

### Paso 2.3: Verificar los consumidores

```bash
# Perfil creado automáticamente
curl -s http://localhost:8083/perfiles | python -m json.tool | grep -A 5 "carlos"

# Notificación de bienvenida
curl -s http://localhost:8084/notificaciones | python -m json.tool | grep "BIENVENIDA"
```

### Paso 2.4: Eliminar el empleado

```bash
curl -s -X DELETE http://localhost:8080/empleados/E005
curl -s http://localhost:8084/notificaciones | python -m json.tool | grep "DESVINCULACION"
```

### Paso 2.5: Mostrar RabbitMQ Management

1. Abrir [http://localhost:15672](http://localhost:15672) (admin/admin)
2. Ir a **Exchanges** → `empleados.exchange`
3. Mostrar **Bindings**: `empleado.creado` → notificaciones + perfiles, `empleado.eliminado` → solo notificaciones

---

## 3. Demostración: Seguridad JWT (Reto 4)

### 3.1: Login como ADMIN

```bash
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@empresa.com", "password": "admin123"}'
```

### 3.2: Acceder sin token (401)

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/empleados
# Expected: 401
```

### 3.3: Acceder con token ADMIN (200)

```bash
TOKEN=$(curl -s -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@empresa.com", "password": "admin123"}' \
  | python -c "import sys, json; print(json.load(sys.stdin)['token'])")

curl -s http://localhost:8085/empleados \
  -H "Authorization: Bearer $TOKEN"
```

---

## 4. Demostración: Pruebas BDD Automatizadas (Reto 5)

### 4.1: Explicar qué es BDD

> "Profesor, en lugar de verificar manualmente cada funcionalidad con curl, implementamos un sistema de pruebas automatizadas usando **BDD (Behavior-Driven Development)**. Los escenarios están escritos en **Gherkin**, un lenguaje natural en español que cualquiera puede entender. Si las pruebas pasan, sabemos que todo el sistema funciona correctamente."

### 4.2: Mostrar los archivos de especificación

```bash
# Mostrar la estructura
ls e2e-tests/src/test/resources/features/

# Mostrar un ejemplo de escenario
cat e2e-tests/src/test/resources/features/03_onboarding.feature
```

> "Estos archivos `.feature` describen el comportamiento esperado. Son documentación viva que siempre está actualizada."

### 4.3: Ejecutar la suite completa

```bash
cd e2e-tests
mvn verify
```

**Lo que debe verse en consola:**

```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 4.4: Abrir el reporte visual

```bash
# En Windows, abrir con el navegador
start e2e-tests/target/cucumber-report.html

# En Mac
open e2e-tests/target/cucumber-report.html

# En Linux
xdg-open e2e-tests/target/cucumber-report.html
```

> "El reporte HTML muestra cada escenario con sus pasos, indicando cuáles pasaron (verde) y cuáles fallaron (rojo)."

### 4.5: Simular un fallo (prueba de concepto)

**Acción:** Modificar temporalmente un escenario para demostrar que las pruebas fallan con mensajes descriptivos.

```bash
# Editar el archivo de seguridad
nano e2e-tests/src/test/resources/features/02_seguridad.feature

# Cambiar esta línea (buscar "la respuesta debe tener código 401" y cambiar a 200):
#   Entonces la respuesta debe tener código 200
# En lugar de:
#   Entonces la respuesta debe tener código 401
```

```bash
# Ejecutar la prueba
mvn verify
```

**Resultado esperado:**

```
java.lang.AssertionError: Código de respuesta debe ser 200 but was: 401
Tests run: 21, Failures: 1, Errors: 0, Skipped: 0
BUILD FAILURE
```

> "El mensaje es claro: esperábamos 200 pero obtuvimos 401. Esto ayuda a identificar rápidamente qué falló."

**Revertir el cambio después de la demostración:**

```bash
git checkout -- e2e-tests/src/test/resources/features/02_seguridad.feature
```

### 4.6: Explicar el polling

> "El sistema usa comunicación asincrónica (RabbitMQ). Cuando se crea un empleado, otros servicios procesan el evento de forma independiente. No podemos verificar inmediatamente el resultado. Por eso implementamos **polling con reintentos**: consultamos repetidamente hasta que la condición se cumple o expira el timeout. Esto es superior a un `sleep` fijo porque termina más rápido si el evento se procesa antes."

```bash
# Mostrar el código de PollingUtils
cat e2e-tests/src/test/java/support/PollingUtils.java
```

### 4.7: Ejecutar 3 veces consecutivas (consistencia)

```bash
for i in 1 2 3; do
  echo "=== Ejecución $i ==="
  mvn verify -q
  echo ""
done
```

> "Ejecutar 3 veces confirma que no hay pruebas intermitentes (flaky tests)."

---

## 5. Mostrar el Código Clave

Si el profesor pide ver código, mostrar estos archivos:

### RabbitMQ (Reto 3)

| Archivo | Qué muestra |
|---------|-------------|
| `empleados/.../RabbitMQConfig.java` | Definición de TopicExchange y bindings |
| `empleados/.../EmpleadoEventPublisher.java` | Publicación de eventos sin bloquear |
| `notificaciones/.../NotificacionConsumer.java` | Escucha con `@RabbitListener` |

### Seguridad (Reto 4)

| Archivo | Qué muestra |
|---------|-------------|
| `gateway/.../JwtAuthFilter.java` | Validación JWT y RBAC en Gateway |
| `gateway/src/main/resources/application.yml` | Routing de servicios |
| `auth/.../AuthService.java` | Login con bcrypt y JWT |

### Pruebas BDD (Reto 5)

| Archivo | Qué muestra |
|---------|-------------|
| `e2e-tests/.../features/03_onboarding.feature` | Escenarios Gherkin en español |
| `e2e-tests/.../stepdefs/EmpleadoStepDefs.java` | Step definitions con Rest Assured |
| `e2e-tests/.../support/PollingUtils.java` | Polling para eventual consistencia |
| `e2e-tests/.../support/TestContext.java` | Contexto compartido entre pasos |

---

## 6. Cierre: Resumen de logros

| Reto | Logro principal |
|------|-----------------|
| 1 | Servidor web CRUD funcional |
| 2 | 5 microservicios independientes con Docker Compose |
| 3 | Comunicación asincrónica con RabbitMQ (Fan-Out) |
| 4 | Seguridad JWT centralizada + RBAC (ADMIN/USER) |
| 5 | 21 escenarios BDD automatizados, consistentes y documentados |

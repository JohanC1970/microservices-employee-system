# Reto 5 - Automatización de Pruebas Funcionales con BDD

## ¿Qué es BDD?

**Behavior-Driven Development (BDD)** es una metodología que extiende el desarrollo guiado por pruebas (TDD) al enfocar las pruebas en el **comportamiento** del sistema desde la perspectiva del usuario, no en la implementación técnica.

| Aspecto | Pruebas Tradicionales | BDD |
|---------|----------------------|-----|
| **Perspectiva** | Técnica (funciones, clases, módulos) | Negocio (flujos, comportamiento, reglas) |
| **Lenguaje** | Código del lenguaje de programación | Lenguaje natural estructurado (Gherkin) |
| **Audiencia** | Desarrolladores | Desarrolladores, QA, stakeholders |
| **Documentación** | Se desactualiza fácilmente | **Documentación viva**: si la prueba pasa, el comportamiento documentado es real |

### ¿Por qué se eligió este enfoque?

1. **Lenguaje accesible**: Los escenarios están escritos en español (Gherkin), lo que permite que cualquier persona del equipo entienda qué se está probando sin conocer código.

2. **Documentación viva**: Los archivos `.feature` son ejecutables. Si pasan, el comportamiento documentado es real y actualizado.

3. **Separación de preocupaciones**: Los archivos `.feature` definen el "qué" (comportamiento esperado), mientras que los step definitions definen el "cómo" (implementación técnica).

4. **Colaboración equipo**: Permite que analistas de negocio, QA y desarrolladores colaboren en la definición de criterios de aceptación.

---

## Prerrequisitos

### Herramientas necesarias

| Herramienta | Versión mínima | Descripción |
|-------------|----------------|-------------|
| **Java** | 17 | JDK para compilar y ejecutar las pruebas |
| **Maven** | 3.8+ | Gestor de dependencias y build |
| **Docker** | 20.10+ | Contenedores para el sistema |
| **Docker Compose** | 1.29+ | Orquestación de servicios |

### Verificar instalación

```bash
java -version
mvn -version
docker --version
docker-compose --version
```

---

## Estructura del Proyecto

```
e2e-tests/
├── pom.xml                                          # Dependencias Maven
├── README.md                                        # Este archivo
└── src/
    └── test/
        ├── java/
        │   ├── stepdefs/                            # Implementación de pasos
        │   │   ├── AuthStepDefs.java                # Autenticación
        │   │   ├── EmpleadoStepDefs.java            # CRUD Empleados
        │   │   ├── OnboardingStepDefs.java          # Onboarding (polling)
        │   │   ├── OffboardingStepDefs.java         # Offboarding
        │   │   └── CucumberRunner.java              # Runner de pruebas
        │   └── support/                             # Utilidades共享
        │       ├── TestContext.java                 # Contexto compartido entre pasos
        │       ├── PollingUtils.java                # Funciones de polling
        │       └── Hooks.java                       # Before/After hooks
        └── resources/
            └── features/                            # Archivos Gherkin (.feature)
                ├── 01_sistema.feature               # Escenario de humo
                ├── 02_seguridad.feature            # Seguridad y RBAC
                ├── 03_onboarding.feature            # Onboarding con async
                └── 04_offboarding.feature           # Offboarding
```

---

## Descripción de Escenarios

### 1. Verificación del sistema (01_sistema.feature)

| Escenario | Descripción |
|-----------|-------------|
| Sistema responde correctamente | Verifica que el API Gateway responde 401 sin token |
| API Gateway accesible | Verifica que el sistema está desplegado |

### 2. Seguridad y control de acceso (02_seguridad.feature)

| Escenario | Descripción |
|-----------|-------------|
| Acceso denegado sin token | GET /empleados sin Authorization → 401 |
| Acceso denegado con token inválido | Token "token_invalido_12345" → 401 |
| Acceso denegado con token malformado | Token sin formato JWT → 401 |
| USER no puede crear empleados | POST /empleados con rol USER → 403 |
| USER no puede eliminar empleados | DELETE con rol USER → 403 |
| USER puede consultar empleados | GET /empleados con rol USER → 200 |
| ADMIN puede crear empleados | POST /empleados con rol ADMIN → 201 |
| ADMIN puede eliminar empleados | DELETE con rol ADMIN → 204 |

### 3. Onboarding de empleados (03_onboarding.feature)

| Escenario | Descripción |
|-----------|-------------|
| Registro exitoso → credenciales | Registro empleado + polling hasta que usuario exista en auth-service |
| Registro exitoso → notificación | Verifica notificación de "bienvenida" con polling |
| Login con nuevo empleado | Polling hasta que login sea exitoso con contraseña temporal |
| Registro con departamento inválido | POST con dept inexistente → 400 |
| Registro con campos faltantes | POST sin datos requeridos → 400 |
| Listar empleados post-registro | Verifica que el empleado aparece en la lista |

### 4. Offboarding de empleados (04_offboarding.feature)

| Escenario | Descripción |
|-----------|-------------|
| Desvinculación completa | DELETE → polling verifica notificación "desvinculación" |
| Login tras offboarding | Polling hasta que login falle (usuario desactivado) |
| Recuperación fallida offboarding | POST /auth/recover-password → falla para usuario inactivo |
| Eliminar empleado inexistente | DELETE con ID inexistente → 404 |
| Múltiples empleados independientes | Crea y elimina varios empleados sin dependencias |

---

## Herramientas y Frameworks

| Herramienta | Propósito | Justificación |
|-------------|-----------|----------------|
| **Cucumber-JVM** | Framework BDD | Estándar de la industria, amplia documentación, integración con JUnit |
| **Rest Assured** | Cliente HTTP | API fluida en Java, ideal para pruebas REST, integración nativa con BDD |
| **Gherkin** | Lenguaje de especificación | Sintaxis legible en español (# language: es) |
| **JUnit** | Runner de pruebas | Integración directa con Cucumber, reportes HTML/JSON |

### Justificación de la elección Java + Cucumber + Rest Assured

1. **Consistencia con el proyecto**: Los microservicios están desarrollados en Java/Spring Boot. Usar Java para pruebas mantiene consistencia tecnológica.

2. **Rest Assured**: librería especializada para pruebas de API REST con sintaxis fluida que se integra naturalmente con step definitions de Cucumber.

3. **Cucumber-JVM**: madura y bien mantenida, soporta múltiples idiomas (español con `# language: es`), genera reportes visuales.

---

## Instrucciones de Ejecución

### Paso 1: Levantar el sistema

```bash
# En la raíz del proyecto (donde está docker-compose.yml)
docker-compose up --build -d
```

**Importante**: Esperar a que todos los servicios esténhealthy (~2-3 minutos).

Verificar con:
```bash
docker-compose ps
```

Deberían ver todos los servicios en estado "Up" o "running".

### Paso 2: Configurar variables de entorno (opcional)

Las pruebas usan valores por defecto que funcionan con la configuración estándar:

```bash
# Opcional: personalizar si es necesario
export BASE_URL=http://localhost:8085
export ADMIN_EMAIL=admin@empresa.com
export ADMIN_PASSWORD=admin123
```

### Paso 3: Ejecutar las pruebas BDD

```bash
cd e2e-tests
mvn test
```

### Paso 4: Interpretar los resultados

#### Consola
- Los escenarios pasando muestran ✓ o PASSED
- Los escenarios fallando muestran ✗ o FAILED con mensaje de error
- Cada step definition imprime logs con el status de las peticiones HTTP

#### Reportes
Se generan en la carpeta `target/`:

| Reporte | Descripción |
|---------|-------------|
| `cucumber-report.html` | Reporte visual en HTML |
| `cucumber-report.json` | Reporte en JSON para integración CI/CD |
| `cucumber-results.xml` | Resultados en formato JUnit XML |

---

## Parámetros de Polling

### Justificación técnica

El sistema usa **eventual consistencia** (comunicación asincrónica vía RabbitMQ). Cuando se crea un empleado:
1. empleados-service crea el registro
2. Publica evento `empleado.creado`
3. auth-service consume el evento y crea el usuario
4. notificaciones-service consume el evento y crea la notificación

Estos procesos son **asíncronos**, por lo que no se puede verificar inmediatamente el resultado.

### Estrategia: Polling con reintentos

```
esperarHastaQue(condición, maxIntentos, intervaloMs)
```

| Parámetro | Valor | Justificación |
|-----------|-------|----------------|
| **Intentos máximos** | 10 | Suficiente para la mayoría de procesamiento de eventos |
| **Intervalo** | 2000ms (2s) | Balance entre velocidad y carga del sistema |
| **Timeout total** | ~20 segundos | Tiempo razonable para procesamiento asincrónico |

**Importante**: No usamos `Thread.sleep()` fijo porque:
- El polling termina más rápido si el evento se procesa antes
- Tolera variaciones de tiempo sin fallar de forma intermitente
- Es más eficiente energéticamente

---

## Verificación de Consistencia

Ejecutar la suite completa al menos **3 veces consecutivas** para verificar que no hay pruebas intermitentes (*flaky tests*):

```bash
cd e2e-tests
mvn clean test
mvn clean test
mvn clean test
```

Si alguna prueba falla de forma inconsistente, revisar:
1. Timeouts de polling (aumentar si el sistema está lento)
2. Datos contaminados de ejecuciones anteriores
3. Race conditions en pasos paralelos

---

## Simular un Fallo (Prueba de concepto)

Para verificar que los mensajes de error son descriptivos, modificar un escenario:

```gherkin
# En 02_seguridad.feature, cambiar:
Entonces la respuesta debe tener código 401
# Por:
Entonces la respuesta debe tener código 200
```

Al ejecutar, mostrará:
```
Expected: 200
Actual: 401
```

Esto confirma que los assertions son claros y descriptivos.

---

## Notas Adicionales

### Aislamiento entre escenarios

Cada escenario usa **IDs únicos** basados en timestamps para evitar conflictos:
- Emails: `empleado.test.{timestamp}@test.com`
- IDs: generados automáticamente por el sistema

El hook `@After` limpia el contexto (`TestContext.clear()`) después de cada escenario.

### Manejo de precondiciones complejas

- **Onboarding**: Los Antecedentes crean el departamento IT si no existe
- **Offboarding**: Se crea un empleado + se espera que tenga credenciales activas antes de eliminarlo

### Autenticación

- **ADMIN**: `admin@empresa.com` / `admin123` (creado automáticamente por DataInitializer)
- **USER**: Se crea dinámicamente mediante el flujo de onboarding (rol asignado automáticamente)

---

## Troubleshooting

### "Connection refused" al API Gateway
- Verificar que los contenedores están corriendo: `docker-compose ps`
- Verificar logs: `docker-compose logs gateway`

### "401 Unauthorized" en todos los endpoints
- El sistema requiere que todos los endpoints pasen por el gateway
- Verificar que el admin existe: revisar logs de auth-service

### Timeout en polling
- Aumentar `POLLING_MAX_ATTEMPTS` en los step definitions
- Verificar que RabbitMQ está funcionando: `docker-compose logs message-broker`

---

## Licencia

Este proyecto es parte del sistema de microservices-employee-system desarrollado para el Reto 5 de Microservicios.
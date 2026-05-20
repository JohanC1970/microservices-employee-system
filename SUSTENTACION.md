# Guía de Ejecución y Sustentación — Reto 7: Observabilidad

## ¿Cómo ejecutar el proyecto?

### 1. Requisitos previos
```bash
docker --version        # Docker 20.10+
docker compose version  # Docker Compose 2.x+
```

### 2. Levantar el sistema completo
```bash
cd /Users/jeyson/Documents/MIcroServicios/microservices-employee-system
docker compose up -d
```
Espera ~2 minutos a que todos los servicios estén `healthy`.

### 3. Verificar que todo está corriendo
```bash
docker compose ps
```
Deberías ver ~22 contenedores en estado `Up`.

### 4. Ejecutar la demostración de observabilidad
```bash
bash demo-observabilidad.sh
```
Este script:
- Verifica todos los servicios
- Genera tráfico (peticiones normales + errores)
- Hace un stress test de 30 peticiones concurrentes
- Simula una caída de servicio (prueba de caos)
- Muestra los resultados en consola

---

## Acceso a los dashboards

| Herramienta | URL | Credenciales |
|-------------|-----|--------------|
| **Prometheus** | http://localhost:9090 | — |
| **Grafana** | http://localhost:3001 | admin / admin |
| **Zipkin** | http://localhost:9411 | — |
| **RabbitMQ** | http://localhost:15672 | guest / guest |
| **Jenkins** | http://localhost:9091 | — |

---

## Guía de sustentación

### Pregunta 1: ¿Qué es la observabilidad y cuáles son sus tres pilares?

**Respuesta:**
La observabilidad es la capacidad de entender el estado interno de un sistema a partir de sus salidas externas. Los tres pilares son:

- **Métricas**: Datos numéricos agregados en el tiempo. Ejemplo: cuántas peticiones por segundo recibe el API Gateway. Se ven en **Prometheus** y **Grafana**.
- **Logs**: Registros de eventos discretos. Ejemplo: "Usuario admin inició sesión a las 10:32". Se ven en **Loki** desde Grafana > Explore.
- **Trazas distribuidas**: El recorrido completo de una petición a través de todos los microservicios. Ejemplo: API Gateway → Auth Service → Empleados Service → DB. Se ven en **Zipkin**.

> *Demostración en vivo*: Abrir Zipkin → Find Traces → seleccionar una traza → mostrar el árbol de spans.

---

### Pregunta 2: ¿Cómo funciona Prometheus? ¿Qué es el modelo Pull?

**Respuesta:**
Prometheus usa un modelo **Pull**: cada 15 segundos va a buscar las métricas a cada microservicio consultando su endpoint `/metrics` (o `/actuator/prometheus` en Spring Boot). Los microservicios no envían datos — Prometheus los va a buscar.

```
Prometheus ──(GET /metrics cada 15s)──► api-gateway:8000/metrics
Prometheus ──(GET /actuator/prometheus)──► departamentos-service:8080
```

> *Demostración en vivo*: Abrir http://localhost:9090 → Status → Targets → mostrar todos los targets en verde (UP).

---

### Pregunta 3: ¿Cómo funciona Zipkin? ¿Qué es el modelo Push?

**Respuesta:**
Zipkin usa un modelo **Push**: cada microservicio, cuando procesa una petición, envía automáticamente la información de la traza a Zipkin. Esto lo hace el SDK de OpenTelemetry configurado en cada servicio.

```
api-gateway ──(POST /api/v2/spans)──► zipkin:9411
auth-service ──(POST /api/v2/spans)──► zipkin:9411
empleados-service ──(POST /api/v2/spans)──► zipkin:9411
```

El `traceId` viaja en el header HTTP `traceparent` (estándar W3C Trace Context) entre servicios, permitiendo correlacionar todos los spans de una misma petición.

> *Demostración en vivo*: Abrir http://localhost:9411 → Find Traces → seleccionar una traza → mostrar cómo el mismo `traceId` aparece en api-gateway, auth-service y empleados-service.

---

### Pregunta 4: ¿Qué es OpenTelemetry?

**Respuesta:**
OpenTelemetry (OTel) es un estándar abierto de la CNCF (Cloud Native Computing Foundation) para instrumentar aplicaciones distribuidas. Su ventaja principal es que es **agnóstico al backend**: el mismo código de instrumentación funciona con Zipkin, Jaeger, Datadog, etc.

En este proyecto cada servicio tiene configurado:
```
OTEL_SERVICE_NAME=nombre-del-servicio
OTEL_EXPORTER_ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
OTEL_PROPAGATORS=tracecontext,baggage
```

---

### Pregunta 5: ¿Cómo viaja el traceId entre servicios de distintos lenguajes?

**Respuesta:**
Mediante el header HTTP estándar **W3C Trace Context**:
```
traceparent: 00-{traceId}-{spanId}-{flags}
```

Ejemplo real:
```
traceparent: 00-abc123def456abc123def456abc12345-span001-01
```

Cuando el API Gateway (Python) llama al Empleados Service (Python) o al Departamentos Service (Java), pasa este header. Cada servicio lo lee, crea su propio span hijo, y lo envía a Zipkin con el mismo `traceId`. Así Zipkin puede reconstruir el árbol completo.

---

### Pregunta 6: ¿Qué alertas están configuradas?

**Respuesta:**
En Grafana hay 3 alertas configuradas automáticamente:

1. **Servicio Caído**: Se activa cuando `probe_success < 1` durante 1 minuto. Significa que el health check de un servicio falló.
2. **Base de Datos Caída**: Se activa cuando `servicio_saludable < 1` durante 1 minuto.
3. **Alta Tasa de Errores 5xx**: Se activa cuando más del 10% de las peticiones retornan error 5xx durante 1 minuto.

> *Demostración en vivo*: Abrir http://localhost:3001 → Alerting → Alert Rules → mostrar las reglas configuradas.

---

### Pregunta 7: ¿Qué servicio tardó más en responder y cómo lo identificaron?

**Respuesta:**
Se identifica en Zipkin buscando las trazas con mayor duración. El proceso es:

1. Abrir http://localhost:9411
2. Find Traces → ordenar por duración (mayor a menor)
3. Abrir la traza más lenta
4. El span más largo indica el servicio que tardó más

Generalmente `departamentos-service` (Java/Spring Boot) tiene mayor latencia en el arranque por el tiempo de inicialización de la JVM. En operación normal, la latencia es comparable a los demás servicios.

> *Demostración en vivo*: Ejecutar `bash demo-observabilidad.sh` → ir a Zipkin → buscar la traza más larga.

---

### Pregunta 8: ¿Qué es Loki y cómo se diferencia de ELK?

**Respuesta:**
Loki es el sistema de agregación de logs de Grafana Labs. A diferencia de Elasticsearch (ELK Stack):

| Aspecto | Loki | Elasticsearch |
|---------|------|---------------|
| Indexación | Solo labels (servicio, nivel) | Indexa todo el contenido |
| Uso de RAM | Bajo (~128MB) | Alto (~1-2GB) |
| Integración | Nativa con Grafana | Requiere Kibana |
| Consultas | LogQL | Lucene/KQL |

En este proyecto Promtail recolecta los logs de todos los contenedores Docker y los envía a Loki. Desde Grafana > Explore se pueden consultar con LogQL.

> *Demostración en vivo*: Grafana → Explore → seleccionar Loki → query: `{service="api-gateway"}` → mostrar logs en tiempo real.

---

## Queries útiles para la sustentación

### En Prometheus (http://localhost:9090/graph)
```promql
# Estado de todos los servicios (1=UP, 0=DOWN)
up

# Request rate por servicio
rate(http_requests_total[1m])

# Tasa de errores 4xx/5xx
sum by(job)(rate(http_requests_total{status_code=~"[45].."}[1m]))

# Health checks (Blackbox Exporter)
probe_success

# Latencia percentil 95
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
```

### En Loki (Grafana > Explore > Loki)
```logql
# Logs del API Gateway
{service="api-gateway"}

# Solo errores de todos los servicios
{service=~".+"} |= "error"

# Logs con traceId específico
{service=~".+"} |= "traceId"
```

---

## Checklist antes de la sustentación

- [ ] `docker compose ps` → todos los servicios en `Up (healthy)`
- [ ] http://localhost:9090 → Status > Targets → todos en verde
- [ ] http://localhost:3001 → Dashboard "Microservicios — Observabilidad" cargado
- [ ] http://localhost:9411 → Find Traces → hay trazas recientes
- [ ] Ejecutar `bash demo-observabilidad.sh` para generar datos frescos
- [ ] Grafana > Alerting > Alert Rules → mostrar las 3 alertas configuradas
- [ ] Grafana > Explore > Loki → query `{service="api-gateway"}` muestra logs

---

## Estructura del proyecto

```
microservices-employee-system/
├── docker-compose.yml              ← Orquesta todos los servicios
├── demo-observabilidad.sh          ← Script de demostración
├── SUSTENTACION.md                 ← Esta guía
├── api-gateway/                    ← Python/FastAPI — punto de entrada
├── auth-service/                   ← Python/FastAPI — autenticación JWT
├── empleados-service/              ← Python/FastAPI — gestión de empleados
├── departamentos-service/          ← Java/Spring Boot — departamentos
├── notificaciones-service/         ← Node.js — notificaciones async
├── perfiles-service/               ← Node.js — perfiles de usuario
├── reportes-service/               ← Go — generación de reportes
└── observability/
    ├── prometheus/prometheus.yml   ← Configuración de scraping
    ├── grafana/provisioning/       ← Dashboards y alertas auto-configurados
    ├── loki/loki-config.yml        ← Configuración de Loki
    ├── promtail/promtail-config.yml← Recolector de logs Docker
    └── blackbox/blackbox.yml       ← Sondeo de health checks
```

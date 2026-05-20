# 🔍 Testing de Observabilidad - Reto 7

## Descripción

Este documento describe cómo usar el suite de pruebas de observabilidad para validar que el stack completo (Prometheus, Grafana, Zipkin, Loki) está funcionando correctamente y correlacionando datos entre métricas, logs y trazas distribuidas.

---

## 📋 Tabla de Contenidos

1. [Quick Start](#quick-start)
2. [Modos de Prueba](#modos-de-prueba)
3. [Qué Esperar en Cada Dashboard](#qué-esperar-en-cada-dashboard)
4. [Validación Paso a Paso](#validación-paso-a-paso)
5. [Troubleshooting](#troubleshooting)
6. [Interpretación de Resultados](#interpretación-de-resultados)

---

## 🚀 Quick Start

```bash
# Ir a la carpeta del proyecto
cd /Users/jeyson/Documents/MIcroServicios/microservices-employee-system

# Ejecutar suite completa de pruebas
bash observability-tests.sh full

# Esperar a que termine (~3-5 minutos)
# Luego acceder a los dashboards indicados
```

---

## 🧪 Modos de Prueba

### 1. `health-check` - Verificación Rápida (30 segundos)

Verifica que todos los servicios estén UP y Prometheus está scrapeando.

```bash
bash observability-tests.sh health-check
```

**Valida:**
- ✓ API Gateway responde
- ✓ Auth Service responde
- ✓ Empleados Service responde
- ✓ Departamentos Service responde
- ✓ Reportes Service responde
- ✓ Prometheus tiene targets UP

**Usa este modo para:** Verificación rápida antes de empezar

---

### 2. `basic` - Pruebas Básicas (2 minutos)

Verifica observabilidad básica: health, métricas simples y primeras trazas.

```bash
bash observability-tests.sh basic
```

**Valida:**
- ✓ Servicios UP
- ✓ Prometheus scrapeando
- ✓ Zipkin recibe trazas
- ✓ Generación de métricas HTTP

**Usa este modo para:** Validar que observabilidad está habilitada

**Qué verás:**
- En Prometheus: `api_requests_total` incrementa
- En Zipkin: Primeras traces de `api-gateway`
- En Grafana: Gráficos comienzan a mostrar datos

---

### 3. `advanced` - Pruebas Avanzadas (3-4 minutos)

Pruebas de trazas distribuidas y generación de errores.

```bash
bash observability-tests.sh advanced
```

**Valida:**
- ✓ Trazas distribuidas (cross-service)
- ✓ Propagación de Trace Context
- ✓ Generación de errores
- ✓ Correlación de métricas con errores

**Usa este modo para:** Validar tracing distribuido

**Qué verás:**
- En Zipkin: Trazas con múltiples spans (api-gateway → empleados → db)
- En Prometheus: Error rate incrementa durante las pruebas
- En Grafana: Alertas se activan por error rate alto

---

### 4. `stress` - Test de Carga (20 segundos + análisis)

Genera carga sostenida para ver picos en métricas.

```bash
bash observability-tests.sh stress
```

**Genera:**
- 200 requests en 20 segundos (10 req/seg)
- Picos de latencia observables

**Usa este modo para:** Demostrar escalabilidad y monitoreo de picos

**Qué verás:**
- En Prometheus: `rate(api_requests_total[1m])` muestra picos
- En Grafana: Gráficos muestran aumento de latencia (p95, p99)
- En Zipkin: Múltiples traces con diferentes duraciones

---

### 5. `full` - Suite Completa (5-7 minutos) ⭐ RECOMENDADO

Ejecuta todas las pruebas para demostración completa.

```bash
bash observability-tests.sh full
```

**Incluye:**
- Health checks
- Trazas distribuidas
- Carga de stress
- Errores y alertas
- Validación de Loki
- Integración Grafana
- Correlación de datos

**Usa este modo para:** Demostración completa del Reto 7

---

## 📊 Qué Esperar en Cada Dashboard

### 1. Prometheus (http://localhost:9090)

#### Panel: Status > Targets

```
Endpoint                              State
────────────────────────────────────────────
api-gateway:8000/metrics              UP ✓
auth-service:8085/metrics             UP ✓
departamentos-service:8080/metrics    UP ✓
empleados-service:8080/metrics        UP ✓
reportes-service:3000/metrics         UP ✓
```

**Si ves DOWN:** Verificar que el servicio está corriendo
**Si ves UP:** Todo bien, las métricas se están recolectando

#### Panel: Graph - Consultas Útiles

**Query 1: Request Rate (requests por segundo)**
```
rate(api_requests_total[1m])
```

Después de `bash observability-tests.sh full`:
- Verás picos durante la prueba de stress
- Línea baja (~0) en reposo
- Picos de hasta 10-20 req/seg durante stress test

**Query 2: Latency (percentiles)**
```
histogram_quantile(0.95, rate(api_request_duration_seconds_bucket[5m]))
```

Esperado:
- P50 (mediana): 50-100ms
- P95: 100-300ms
- P99: 200-500ms

**Query 3: Error Rate**
```
increase(http_requests_total{status=~"4..|5.."}[5m])
```

Esperado:
- ~0 errores normalmente
- Picos durante test de errores (404, 401, 400)

#### Panel: Alerts

Debería ver alertas como:
- `HighLatency` (si p95 > 1000ms)
- `HighErrorRate` (si error_rate > 5%)

---

### 2. Grafana (http://localhost:3001)

#### Acceso

```
URL: http://localhost:3001
Usuario: admin
Contraseña: admin
```

#### Dashboards Pre-configurados

**Dashboard: Microservices Overview**

Paneles que deberías ver:

1. **Request Rate Graph**
   - Muestra `rate(api_requests_total[1m])`
   - Picos durante stress test
   - Vuelve a 0 en reposo

2. **Error Rate Graph**
   - Muestra `http_requests_errors_total`
   - Picos durante test de errores
   - Vuelve a 0 después

3. **P95 Latency**
   - Muestra percentil 95
   - Incremento durante stress test
   - Vuelve a normal después

4. **Service Health Status**
   - Todos los servicios en GREEN
   - Rojo si alguno está DOWN

5. **RabbitMQ Queue Depth**
   - Mensaje pendientes en cola
   - Debería ser bajo (< 100)

6. **Database Connection Pool**
   - Conexiones activas a BD
   - Picos durante load test

#### Crear Query Personalizada

Para ver datos en tiempo real:

1. Click `+` > Panel
2. Seleccionar "Prometheus" como datasource
3. Query: `up{job="api-gateway"}`
4. Click "Run Query"

---

### 3. Zipkin (http://localhost:9411)

#### Búsqueda de Trazas

Después de ejecutar pruebas:

1. Ir a http://localhost:9411
2. Click en "Find Traces"
3. Debería listar trazas recientes

#### Estructura de Traza

Ejemplo de traza distribuida:

```
Trace: abc123def456
Duration: 245ms
Spans:
├── api-gateway  (100ms)
│   ├── auth-service  (50ms)
│   └── empleados-service  (80ms)
│       └── database-empleados  (40ms)
└── Return to api-gateway  (15ms)
```

#### Qué Buscar

**Trace ID:** Cada request tiene un ID único que viaja entre servicios

**Spans:** Cada servicio adiciona un span (bloque de tiempo)

**Critical Path:** El camino más largo determina latencia total

#### Filtros Útiles

```
Service: api-gateway
Span name: HTTP GET /health
Min Duration: 100ms
Max Duration: 1000ms
```

#### Después de `bash observability-tests.sh full`

Deberías ver:

1. **Trazas de health checks** (muy rápidas ~5ms)
2. **Trazas de empleados creation** (lentas ~200-300ms)
   - Incluyen múltiples servicios
3. **Trazas de errores** (variables)
   - 404s
   - 401s (sin autenticación)
4. **Trazas del stress test** (variadas)
   - Algunas rápidas
   - Algunas lentas por competencia

---

### 4. Loki (acceso vía Grafana)

#### Cómo Acceder

1. Grafana > Explore (lado izquierdo)
2. Seleccionar "Loki" (dropdown arriba)
3. Ver logs en tiempo real

#### Consultas Útiles

**Query: Logs de API Gateway**
```
{service="api-gateway"}
```

**Query: Logs de Error**
```
{level="ERROR"}
```

**Query: Logs de un servicio específico**
```
{service="empleados-service"} | json
```

#### Qué Esperar

Logs de:
- Requests exitosas
- Errores 404
- Errores 401
- Conexiones a BD
- Mensajes de RabbitMQ

---

## ✅ Validación Paso a Paso

### Paso 1: Ejecutar Pruebas

```bash
cd /Users/jeyson/Documents/MIcroServicios/microservices-employee-system
bash observability-tests.sh full
```

Esperar output:
```
✓ Todos los servicios están UP
✓ Prometheus está scrapeando targets
✓ Zipkin está recibiendo trazas de api-gateway
✓ Generadas 50 requests exitosas
✓ Escenarios de error generados
✓ Stress test completado (200 requests)
✓ Logs generados
✓ Grafana tiene datasource de Prometheus configurado
✓ Correlación de datos completa

🎉 TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE 🎉
```

### Paso 2: Validar Prometheus

```bash
# Ejecutar en otra terminal
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'

# Esperado: Todos con health: "up"
```

### Paso 3: Verificar Zipkin

```bash
# Consultar servicios que Zipkin ve
curl -s http://localhost:9411/api/v1/services | jq .

# Esperado: ["api-gateway", "auth-service", "empleados-service", ...]
```

### Paso 4: Verificar Grafana

```bash
# Verificar que Prometheus está como datasource
curl -s http://localhost:3001/api/datasources | jq '.[0]'

# Debería mostrar Prometheus en la lista
```

### Paso 5: Acceder a Dashboards

| Dashboard | URL |
|-----------|-----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |
| Zipkin | http://localhost:9411 |

---

## 🔧 Troubleshooting

### Problema: Prometheus no tiene targets

**Síntoma:** En Status > Targets, todos están DOWN

**Solución:**
```bash
# Verificar que los servicios están UP
docker compose ps | grep -E "api-gateway|prometheus"

# Reiniciar Prometheus
docker compose restart prometheus

# Verificar configuración
cat observability/prometheus/prometheus.yml
```

### Problema: Zipkin no muestra trazas

**Síntoma:** "No traces found" en Zipkin

**Solución:**
```bash
# Ejecutar pruebas de nuevo
bash observability-tests.sh basic

# Esperar 5 segundos
sleep 5

# Volver a Zipkin
# Las trazas deberían aparecer
```

### Problema: Grafana no ve Prometheus

**Síntoma:** Error "Bad Gateway" o "Unable to connect"

**Solución:**
1. Ir a Grafana > Configuration > Data Sources
2. Click "Prometheus"
3. Cambiar URL a: `http://prometheus:9090`
4. Click "Save & Test"

### Problema: Alto uso de CPU durante pruebas

**Síntoma:** Machine lenta durante `stress` mode

**Solución:**
```bash
# Usar modo "basic" en lugar de "full"
bash observability-tests.sh basic

# O reducir carga modificando el script
# (cambiar 200 requests a 100)
```

### Problema: Errores de conexión a localhost

**Síntoma:** `curl: (7) Failed to connect`

**Solución:**
```bash
# Verificar que Docker Desktop está corriendo
# Verificar puertos con:
netstat -an | grep 9090
netstat -an | grep 3001
netstat -an | grep 9411
```

---

## 📈 Interpretación de Resultados

### Métrica: Request Rate Sube

```
✓ CORRECTO: El stack está recibiendo tráfico
✓ CORRECTO: Prometheus lo está midiendo
✓ CORRECTO: Se ve en Grafana en tiempo real
```

### Métrica: Latency Aumenta During Stress

```
✓ CORRECTO: El sistema está bajo carga
✓ CORRECTO: Las métricas muestran degradación
✓ CORRECTO: Las alertas se deberían activar si exceden threshold
```

### Zipkin Muestra Traces con Múltiples Servicios

```
✓ CORRECTO: La propagación de trace context funciona
✓ CORRECTO: OpenTelemetry está instrumentado
✓ CORRECTO: Se pueden ver cuellos de botella en la cadena
```

### Logs Correlacionados con Métricas

```
✓ CORRECTO: En Grafana, logs y métricas están del mismo timestamp
✓ CORRECTO: Se pueden correlacionar eventos con picos
✓ CORRECTO: Debugging distribuido es posible
```

---

## 🎯 Checklist Final para Reto 7

Después de ejecutar pruebas, deberías poder marcar:

- [ ] **Prometheus** - Todos los targets están UP
- [ ] **Prometheus** - Queries devuelven datos (request rate, latency, error rate)
- [ ] **Grafana** - Accesible en http://localhost:3001
- [ ] **Grafana** - Tiene Prometheus como datasource
- [ ] **Grafana** - Dashboards muestran gráficos con datos
- [ ] **Grafana** - Alertas se activan durante stress test
- [ ] **Zipkin** - Accesible en http://localhost:9411
- [ ] **Zipkin** - Muestra trazas de servicios
- [ ] **Zipkin** - Trazas muestran múltiples spans (distribuidas)
- [ ] **Loki** - Logs visibles en Grafana > Explore
- [ ] **Logs** - Correlacionados con traces (mismo timestamp)
- [ ] **OpenTelemetry** - W3C Trace Context headers visibles en trazas
- [ ] **Todo** - Datos correlacionados entre Prometheus, Grafana, Zipkin y Loki

---

## 📚 Documentación Relacionada

- [RETO7_ARQUITECTURA.md](../RETO7_ARQUITECTURA.md) - Explicación arquitectónica
- [RETO7_DEPLOYMENT.md](../RETO7_DEPLOYMENT.md) - Guía de despliegue
- [prometheus/README.md](./prometheus/README.md) - Config de Prometheus
- [grafana/README.md](./grafana/README.md) - Config de Grafana
- [docker-compose.yml](../docker-compose.yml) - Orquestación completa

---

## 🚀 Próximos Pasos

1. **Customizar Dashboards**
   - Crear dashboard personalizado en Grafana
   - Agregar métricas específicas de negocio

2. **Alertas Avanzadas**
   - Integrar notificaciones a Slack
   - Configurar PagerDuty para alertas críticas

3. **SLOs y SLIs**
   - Definir objetivos de nivel de servicio
   - Implementar alertas basadas en SLO

4. **Análisis de Datos Históricos**
   - Consultar datos de días/semanas pasadas
   - Identificar patrones

5. **Integración con CI/CD**
   - Fallar deploy si observabilidad está degradada
   - Incluir healthchecks en pipeline

---

**¡Listo para demostrar el Reto 7! 🎉**

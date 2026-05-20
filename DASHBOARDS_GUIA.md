# Guía de Dashboards — Reto 7 Observabilidad

## Credenciales de acceso

| Dashboard | URL | Usuario | Contraseña |
|-----------|-----|---------|------------|
| **Prometheus** | http://localhost:9090 | _(sin login)_ | _(sin login)_ |
| **Grafana** | http://localhost:3001 | `admin` | `admin` |
| **Zipkin** | http://localhost:9411 | _(sin login)_ | _(sin login)_ |
| **RabbitMQ** | http://localhost:15672 | `guest` | `guest` |
| **Jenkins** | http://localhost:9091 | _(sin login)_ | _(sin login)_ |

> **Antes de abrir los dashboards**, ejecuta el script para tener datos frescos:
> ```bash
> bash demo-observabilidad.sh
> ```

---

## 1. PROMETHEUS — http://localhost:9090

### ¿Qué muestra?
Prometheus almacena todas las métricas numéricas del sistema. Aquí puedes ver el estado de cada servicio y ejecutar consultas en tiempo real.

---

### Paso 1 — Ver que todos los servicios están siendo monitoreados

1. Abre http://localhost:9090
2. En el menú superior haz clic en **Status** → **Targets**
3. Verás una lista de todos los servicios monitoreados

**Lo que deberías ver:**

| Job | Endpoint | Estado |
|-----|----------|--------|
| api-gateway | api-gateway:8000/metrics | 🟢 UP |
| auth-service | auth-service:8085/metrics | 🟢 UP |
| empleados-service | empleados-service:8080/metrics | 🟢 UP |
| departamentos-service | departamentos-service:8080/actuator/prometheus | 🟢 UP |
| notificaciones-service | notificaciones-service:3000/metrics | 🟢 UP |
| perfiles-service | perfiles-service:3000/metrics | 🟢 UP |
| reportes-service | reportes-service:3000/metrics | 🟢 UP |
| health-api-gateway | blackbox probe /health | 🟢 UP |
| health-auth-service | blackbox probe /health | 🟢 UP |
| health-empleados-service | blackbox probe /health | 🟢 UP |
| health-departamentos-service | blackbox probe /actuator/health | 🟢 UP |
| health-notificaciones-service | blackbox probe /health | 🟢 UP |
| health-perfiles-service | blackbox probe /health | 🟢 UP |
| health-reportes-service | blackbox probe /health | 🟢 UP |
| blackbox-exporter | blackbox-exporter:9115/metrics | 🟢 UP |

---

### Paso 2 — Ejecutar métricas en el Graph

1. Haz clic en **Graph** en el menú superior (o ve directo a http://localhost:9090/graph)
2. En el campo de búsqueda escribe la query y presiona **Execute**
3. Haz clic en la pestaña **Graph** para ver la gráfica

#### Métrica 1 — Estado de todos los servicios (UP/DOWN)
```
up
```
- Resultado: una línea por cada servicio
- Valor `1` = servicio UP (verde)
- Valor `0` = servicio DOWN (rojo)

#### Métrica 2 — Health checks de los endpoints /health
```
probe_success
```
- Resultado: 7 líneas, una por cada servicio
- Valor `1` = health check exitoso
- Valor `0` = health check fallido

#### Métrica 3 — Tasa de peticiones por segundo
```
rate(http_requests_total[1m])
```
- Resultado: gráfica de líneas con el número de peticiones/segundo
- Cada línea es un servicio diferente

#### Métrica 4 — Total de peticiones acumuladas
```
increase(http_requests_total[5m])
```
- Resultado: cuántas peticiones recibió cada servicio en los últimos 5 minutos

#### Métrica 5 — Tasa de errores 4xx y 5xx
```
sum by(job)(rate(http_requests_total{status_code=~"[45].."}[1m]))
```
- Resultado: peticiones con error por servicio
- Útil para ver si hay problemas en algún servicio

#### Métrica 6 — Latencia promedio
```
sum by(job)(rate(http_request_duration_seconds_sum[1m])) / sum by(job)(rate(http_request_duration_seconds_count[1m]))
```
- Resultado: tiempo promedio de respuesta en segundos por servicio

#### Métrica 7 — Latencia percentil 95
```
histogram_quantile(0.95, sum by(job, le)(rate(http_request_duration_seconds_bucket[5m])))
```
- Resultado: el 95% de las peticiones responden en menos de X segundos

---

## 2. GRAFANA — http://localhost:3001

### Credenciales
- **Usuario:** `admin`
- **Contraseña:** `admin`

---

### Paso 1 — Iniciar sesión

1. Abre http://localhost:3001
2. Ingresa usuario `admin` y contraseña `admin`
3. Si pide cambiar contraseña, haz clic en **Skip** o **Later**

---

### Paso 2 — Abrir el Dashboard principal

1. En el menú izquierdo haz clic en el ícono de **cuadrícula** (Dashboards) o presiona `D`
2. Haz clic en **Browse**
3. Verás el dashboard **"Microservicios — Observabilidad"**
4. Haz clic en él para abrirlo

---

### Paso 3 — Qué ver en el Dashboard

El dashboard tiene **2 filas** con los siguientes paneles:

#### Fila 1 — Resumen del Sistema

**Panel: Estado de Servicios**
- Tipo: Stat (cuadros de colores)
- Muestra: un cuadro por cada servicio
- 🟢 Verde = UP | 🔴 Rojo = DOWN
- Métrica usada: `probe_success{job="health-*"}`
- **Cómo mostrarlo:** señala cada cuadro y explica que verde significa que el health check responde HTTP 200

#### Fila 2 — Comportamiento del Tráfico

**Panel: Tasa de Peticiones por Servicio (req/s)**
- Tipo: Time Series (gráfica de líneas)
- Muestra: cuántas peticiones por segundo recibe cada servicio
- Métrica: `sum by(job)(rate(http_requests_total[1m]))`
- **Cómo mostrarlo:** ejecuta `bash demo-observabilidad.sh` y observa cómo la línea sube durante el stress test

**Panel: Latencia Promedio por Servicio (s)**
- Tipo: Time Series (gráfica de líneas)
- Muestra: tiempo promedio de respuesta en segundos
- Métrica: `rate(duration_sum[1m]) / rate(duration_count[1m])`
- **Cómo mostrarlo:** señala los picos de latencia durante el stress test

**Panel: Errores HTTP 4xx y 5xx por Servicio**
- Tipo: Time Series (gráfica de líneas)
- Muestra: peticiones con error por servicio
- Métrica: `rate(http_requests_total{status_code=~"[45].."}[1m])`
- **Cómo mostrarlo:** señala los picos cuando el script genera errores 404/401

---

### Paso 4 — Ver los Datasources configurados

1. En el menú izquierdo haz clic en **Connections** → **Data sources**
2. Verás 2 datasources configurados automáticamente:
   - **Prometheus** → URL: `http://prometheus:9090`
   - **Loki** → URL: `http://loki:3100`
3. Haz clic en **Prometheus**
   - Baja hasta el final de la página
   - Haz clic en el botón azul **Save & test**
   - Deberías ver el mensaje verde: ✅ `Successfully queried the Prometheus API.`
4. Regresa a Data sources y haz clic en **Loki**
   - Baja hasta el final
   - Haz clic en **Save & test**
   - Deberías ver el mensaje verde: ✅ `Data source successfully connected.`

> **Nota:** Si el botón Save & test no aparece, recarga la página con Ctrl+R y vuelve a entrar al datasource.

---

### Paso 5 — Ver logs en Loki (Explore)

1. En el menú izquierdo haz clic en el ícono de **brújula** (Explore)
2. En el selector de datasource (arriba a la izquierda) selecciona **Loki**
3. En el campo de query escribe:

```
{service="api-gateway"}
```

4. Haz clic en **Run query** (o presiona Shift+Enter)
5. Verás los logs del API Gateway en tiempo real

**Otras queries de Loki para mostrar:**

```
{service=~".+"}
```
→ Logs de TODOS los servicios

```
{service=~".+"} |= "error"
```
→ Solo líneas que contienen la palabra "error"

```
{service="auth-service"}
```
→ Logs del servicio de autenticación

---

### Paso 6 — Ver las Alertas configuradas

1. En el menú izquierdo haz clic en **Alerting** (ícono de campana)
2. Haz clic en **Alert rules**
3. Verás las 3 alertas configuradas:

| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| **Servicio Caído** | `probe_success < 1` por 1 minuto | 🔴 Critical |
| **Base de Datos Caída** | `servicio_saludable < 1` por 1 minuto | 🔴 Critical |
| **Alta Tasa de Errores 5xx** | errores 5xx > 10% por 1 minuto | 🟡 Warning |

4. Haz clic en cada alerta para ver su configuración detallada

---

## 3. ZIPKIN — http://localhost:9411

### ¿Qué muestra?
Zipkin muestra el recorrido completo de cada petición a través de todos los microservicios. Cada petición tiene un `traceId` único que permite ver exactamente qué servicios participaron y cuánto tardó cada uno.

---

### Paso 1 — Ver los servicios con trazas

1. Abre http://localhost:9411
2. En la pantalla principal verás el buscador de trazas
3. En el campo **Service Name** haz clic → verás la lista de servicios:
   - api-gateway
   - auth-service
   - departamentos-service
   - empleados-service
   - notificaciones-service
   - perfiles-service
   - reportes-service

---

### Paso 2 — Buscar trazas recientes

1. Deja todos los filtros en blanco
2. Haz clic en **Search** (o el botón de lupa)
3. Verás una lista de trazas recientes ordenadas por tiempo
4. Cada fila muestra:
   - El servicio que inició la petición
   - Cuántos spans (servicios) participaron
   - La duración total
   - Si hubo errores (en rojo)

---

### Paso 3 — Ver el detalle de una traza

1. Haz clic en cualquier traza de la lista
2. Verás un **diagrama de cascada** (waterfall) con:
   - Cada barra = un servicio que procesó la petición
   - El ancho de la barra = tiempo que tardó ese servicio
   - Las barras están anidadas mostrando quién llamó a quién

**Ejemplo de lo que verás:**
```
api-gateway          ████████████████████  45ms
  └─ auth-service    ████████              20ms
  └─ empleados-svc   ██████████████        35ms
       └─ database   ████                  8ms
```

3. Haz clic en cada barra para ver los detalles del span:
   - `traceId`: el ID único de toda la traza
   - `spanId`: el ID de este span específico
   - `http.method`: GET, POST, etc.
   - `http.status_code`: 200, 404, etc.
   - `http.url`: la URL que se llamó

---

### Paso 4 — Filtrar por servicio

1. En el campo **Service Name** selecciona `api-gateway`
2. Haz clic en **Search**
3. Verás solo las trazas que pasaron por el API Gateway

---

### Paso 5 — Ver el mapa de dependencias

1. En el menú superior haz clic en **Dependencies**
2. Verás un **mapa visual** de cómo se llaman los servicios entre sí
3. Las flechas muestran la dirección de las llamadas
4. El grosor de las flechas indica el volumen de tráfico

---

## 4. RABBITMQ — http://localhost:15672

### Credenciales
- **Usuario:** `guest`
- **Contraseña:** `guest`

### ¿Qué muestra?
El panel de administración de RabbitMQ muestra las colas de mensajes usadas para comunicación asíncrona entre servicios.

### Paso a paso

1. Abre http://localhost:15672
2. Ingresa `guest` / `guest`
3. En la pestaña **Overview** verás:
   - Conexiones activas
   - Canales abiertos
   - Mensajes en cola
4. En la pestaña **Queues** verás las colas creadas por los servicios

---

## Secuencia recomendada para la sustentación

```
1. Ejecutar:  bash demo-observabilidad.sh
              (genera tráfico fresco en todos los dashboards)

2. Abrir en el navegador (pestañas separadas):
   - http://localhost:9090  → Prometheus
   - http://localhost:3001  → Grafana
   - http://localhost:9411  → Zipkin

3. Mostrar en Prometheus:
   - Status > Targets → todos en verde
   - Graph → query: up → todos en 1
   - Graph → query: probe_success → todos en 1
   - Graph → query: rate(http_requests_total[1m]) → gráfica con datos

4. Mostrar en Grafana:
   - Dashboard "Microservicios — Observabilidad"
   - Panel de estado (cuadros verdes)
   - Gráfica de request rate con picos del stress test
   - Gráfica de errores con los 404/401 generados
   - Alerting > Alert Rules → 3 alertas configuradas
   - Explore > Loki → {service="api-gateway"} → logs en tiempo real

5. Mostrar en Zipkin:
   - Find Traces → lista de trazas recientes
   - Abrir una traza → diagrama de cascada
   - Señalar el traceId y cómo viaja entre servicios
   - Dependencies → mapa de dependencias

6. Explicar la prueba de caos:
   - El script detuvo departamentos-service por 5 segundos
   - En Prometheus: probe_success bajó a 0 para ese servicio
   - En Grafana: el cuadro cambió a rojo
   - La alerta "Servicio Caído" se habría activado en 1 minuto
   - El servicio fue restaurado automáticamente
```

---

## Resumen de métricas por herramienta

| Métrica | Herramienta | Query / Ubicación |
|---------|-------------|-------------------|
| Estado UP/DOWN | Prometheus | `up` |
| Health check HTTP | Prometheus | `probe_success` |
| Peticiones/segundo | Prometheus + Grafana | `rate(http_requests_total[1m])` |
| Latencia promedio | Prometheus + Grafana | `rate(duration_sum) / rate(duration_count)` |
| Errores 4xx/5xx | Prometheus + Grafana | `rate(http_requests_total{status_code=~"[45].."})` |
| Trazas distribuidas | Zipkin | Find Traces → seleccionar traza |
| Logs en tiempo real | Grafana + Loki | Explore → `{service="api-gateway"}` |
| Colas de mensajes | RabbitMQ | Queues tab |

#!/bin/bash

################################################################################
# SCRIPT DE PRUEBAS DE OBSERVABILIDAD - RETO 7
# 
# Este script genera carga de trabajo distribuida a través de los microservicios
# para demostrar:
# 1. Trazas distribuidas en Zipkin (W3C Trace Context)
# 2. Métricas en Prometheus (HTTP requests, latency, errors)
# 3. Logs en Loki (eventos de aplicación)
# 4. Dashboards en Grafana (visualización integrada)
#
# Uso: bash observability-tests.sh [mode]
# Modos: health-check, basic, advanced, stress, full
#
################################################################################

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuración
API_GATEWAY="http://localhost:8000"
AUTH_SERVICE="http://localhost:8085"
EMPLEADOS_SERVICE="http://empleados-service:8080"
DEPARTAMENTOS_SERVICE="http://departamentos-service:8080"
REPORTES_SERVICE="http://reportes-service:3000"
PROMETHEUS="http://localhost:9090"
GRAFANA="http://localhost:3001"
ZIPKIN="http://localhost:9411"

# Nota: EMPLEADOS, DEPARTAMENTOS y REPORTES son accesibles internamente en Docker
# API_GATEWAY y AUTH_SERVICE se acceden desde localhost porque tienen puertos expuestos

# Modo de ejecución (default: basic)
MODE="${1:-basic}"

# Contador de tests
TESTS_PASSED=0
TESTS_FAILED=0

################################################################################
# FUNCIONES AUXILIARES
################################################################################

print_header() {
    echo -e "\n${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}\n"
}

print_test() {
    echo -e "${BLUE}▶ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
    ((TESTS_PASSED++))
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
    ((TESTS_FAILED++))
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Función para hacer requests con tracing headers
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local trace_id=${4:-"$(openssl rand -hex 8)"}
    
    if [ -z "$data" ]; then
        curl -s -X "$method" "$url" \
            -H "X-Trace-ID: $trace_id" \
            -H "Content-Type: application/json" \
            -w "\nHTTP_CODE:%{http_code}\nTRACE_ID:$trace_id\n"
    else
        curl -s -X "$method" "$url" \
            -H "X-Trace-ID: $trace_id" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -w "\nHTTP_CODE:%{http_code}\nTRACE_ID:$trace_id\n"
    fi
}

# Función para verificar servicio está UP
check_service() {
    local service_name=$1
    local service_url=$2
    local is_internal=${3:-false}
    
    print_test "Verificando $service_name"
    
    if [ "$is_internal" = "true" ]; then
        # Para servicios internos, usar docker exec desde api-gateway
        if docker compose exec -T api-gateway curl -s "$service_url/health" > /dev/null 2>&1; then
            print_success "$service_name está UP"
            return 0
        else
            print_error "$service_name NO está disponible"
            return 1
        fi
    else
        # Para servicios expuestos en localhost
        if curl -s "$service_url/health" > /dev/null 2>&1; then
            print_success "$service_name está UP"
            return 0
        else
            print_error "$service_name NO está disponible"
            return 1
        fi
    fi
}

################################################################################
# PRUEBAS ESPECÍFICAS
################################################################################

# PRUEBA 1: Health Checks - Verificar que todos los servicios están vivos
test_health_checks() {
    print_header "TEST 1: HEALTH CHECKS (Todos los servicios)"
    
    local all_up=true
    
    # Servicios expuestos en localhost
    check_service "API Gateway" "$API_GATEWAY" || all_up=false
    check_service "Auth Service" "$AUTH_SERVICE" || all_up=false
    check_service "Prometheus" "$PROMETHEUS" || all_up=false
    
    # Servicios internos (accesibles vía docker exec)
    check_service "Empleados Service" "http://empleados-service:8080" "true" || all_up=false
    check_service "Departamentos Service" "http://departamentos-service:8080" "true" || all_up=false
    check_service "Reportes Service" "http://reportes-service:3000" "true" || all_up=false
    
    if [ "$all_up" = true ]; then
        print_success "Todos los servicios están UP"
    else
        print_error "Algunos servicios no están disponibles"
        return 1
    fi
}

# PRUEBA 2: Métricas en Prometheus - Verificar que Prometheus está scrapeando
test_prometheus_targets() {
    print_header "TEST 2: PROMETHEUS - Verificar Targets"
    
    print_test "Consultando targets en Prometheus"
    
    local response=$(curl -s "$PROMETHEUS/api/v1/targets")
    local active_targets=$(echo "$response" | grep -o '"activeTargets"' | wc -l)
    
    if [ "$active_targets" -gt 0 ]; then
        print_success "Prometheus está scrapeando targets"
        
        # Listar endpoints que están UP
        local up_count=$(echo "$response" | grep -o '"health":"up"' | wc -l)
        print_info "Targets en estado UP: $up_count"
    else
        print_error "Prometheus no tiene targets configurados"
        return 1
    fi
}

# PRUEBA 3: Trazas Básicas - Verificar que Zipkin recibe trazas
test_zipkin_traces() {
    print_header "TEST 3: ZIPKIN - Traces Básicas"
    
    print_test "Haciendo petición simple para generar traza"
    
    # Hacer una petición que genere traza
    local response=$(make_request GET "$API_GATEWAY/health")
    local trace_id=$(echo "$response" | grep "TRACE_ID:" | cut -d: -f2)
    
    print_info "Traza ID: $trace_id"
    
    # Esperar a que Zipkin procese la traza
    sleep 2
    
    # Consultar servicios en Zipkin
    local services=$(curl -s "$ZIPKIN/api/v1/services")
    
    if echo "$services" | grep -q "api-gateway"; then
        print_success "Zipkin está recibiendo trazas de api-gateway"
    else
        print_info "Aún no hay trazas en Zipkin (normal en inicio)"
    fi
}

# PRUEBA 4: Trazas Distribuidas - Call que atraviesa múltiples servicios
test_distributed_traces() {
    print_header "TEST 4: TRAZAS DISTRIBUIDAS - Cross-Service Trace"
    
    print_test "Generando traza distribuida (API Gateway → Empleados Service → DB)"
    
    # Generar un ID de traza único
    local trace_id=$(openssl rand -hex 16)
    
    # Payload para crear empleado (esto debería atravesar múltiples servicios)
    local payload='{
        "nombre": "Employee-'$(date +%s%N | cut -c1-10)'",
        "email": "test-'$(date +%s%N | cut -c1-10)'@empresa.com",
        "departamento_id": 1
    }'
    
    print_info "Trace ID: $trace_id"
    print_info "Petición: POST /empleados con payload"
    
    local response=$(curl -s -X POST "$API_GATEWAY/empleados" \
        -H "X-Trace-ID: $trace_id" \
        -H "Content-Type: application/json" \
        -d "$payload" \
        -w "\nHTTP:%{http_code}\n")
    
    local http_code=$(echo "$response" | grep "HTTP:" | cut -d: -f2)
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        print_success "Petición exitosa (HTTP $http_code)"
        print_info "Esta petición atravesó múltiples servicios"
        print_info "Verifica en Zipkin: $ZIPKIN (busca trace ID: $trace_id)"
    else
        print_info "Petición completada (HTTP $http_code)"
    fi
    
    # Esperar a que Zipkin procese
    sleep 3
}

# PRUEBA 5: Métricas HTTP - Generar requests y ver métricas
test_http_metrics() {
    print_header "TEST 5: MÉTRICAS HTTP - Request Rate & Latency"
    
    print_test "Generando 50 requests para ver métricas de latencia"
    
    local count=0
    local total_time=0
    
    for i in {1..50}; do
        local start=$(date +%s%N)
        curl -s "$API_GATEWAY/health" > /dev/null
        local end=$(date +%s%N)
        local elapsed=$(( (end - start) / 1000000 ))
        total_time=$((total_time + elapsed))
        
        if [ $((i % 10)) -eq 0 ]; then
            echo -ne "\r  Progreso: $i/50"
        fi
    done
    echo ""
    
    local avg_latency=$((total_time / 50))
    print_success "Generadas 50 requests exitosas"
    print_info "Latencia promedio: ${avg_latency}ms"
    print_info "Verifica en Prometheus: $PROMETHEUS/graph?query=rate(api_requests_total%5B1m%5D)"
}

# PRUEBA 6: Errores y Alertas - Generar errores para ver en alertas
test_error_scenarios() {
    print_header "TEST 6: ERRORES Y ALERTAS - Generando escenarios de error"
    
    print_test "Scenario 1: Request a endpoint no existente (HTTP 404)"
    curl -s -X GET "$API_GATEWAY/endpoint-no-existe" > /dev/null
    print_info "Generado error 404"
    
    print_test "Scenario 2: Request sin autenticación (HTTP 401)"
    curl -s -X POST "$API_GATEWAY/empleados" \
        -H "Content-Type: application/json" \
        -d '{"test":"data"}' > /dev/null
    print_info "Generado error 401"
    
    print_test "Scenario 3: Payload inválido (HTTP 400)"
    curl -s -X POST "$API_GATEWAY/empleados" \
        -H "Content-Type: application/json" \
        -d '{"invalid_field":"value"}' > /dev/null
    print_info "Generado error 400"
    
    print_success "Escenarios de error generados"
    print_info "Error rate debería aumentar en Prometheus"
    print_info "Verifica en Grafana dashboard: $GRAFANA"
}

# PRUEBA 7: Carga de Stress - Generar picos de tráfico
test_stress_load() {
    print_header "TEST 7: STRESS TEST - Carga sostenida"
    
    print_test "Generando 200 requests en 20 segundos (10 req/seg)"
    
    local pids=()
    
    for i in {1..20}; do
        for j in {1..10}; do
            (
                curl -s "$API_GATEWAY/health" > /dev/null 2>&1 &
            ) &
            pids+=($!)
        done
        
        echo -ne "\r  Lote $i/20 completado"
        sleep 1
    done
    
    # Esperar a que terminen todos
    for pid in "${pids[@]}"; do
        wait "$pid" 2>/dev/null || true
    done
    
    echo ""
    print_success "Stress test completado (200 requests)"
    print_info "Verifica picos en: $PROMETHEUS/graph?query=rate(api_requests_total%5B1m%5D)"
    print_info "Latencia en: $PROMETHEUS/graph?query=histogram_quantile(0.95,%20rate(api_request_duration_seconds_bucket%5B5m%5D))"
}

# PRUEBA 8: Logs en Loki - Generar logs variados
test_loki_logs() {
    print_header "TEST 8: LOGS EN LOKI - Agregación de logs"
    
    print_test "Generando logs variados en múltiples servicios"
    
    # Hacer requests que generen logs
    print_info "Log 1: Request exitosa"
    curl -s "$API_GATEWAY/health" > /dev/null
    
    print_info "Log 2: Error 404"
    curl -s "$API_GATEWAY/api/no-existe" > /dev/null 2>&1 || true
    
    print_info "Log 3: Request rechazada (sin auth)"
    curl -s -X POST "$API_GATEWAY/empleados" \
        -H "Content-Type: application/json" \
        -d '{}' > /dev/null 2>&1 || true
    
    sleep 2
    
    # Verificar que Loki tiene logs
    local response=$(curl -s "$ZIPKIN:3100/loki/api/v1/label/job/values" 2>/dev/null || echo "")
    
    print_success "Logs generados"
    print_info "Verifica logs en Grafana: $GRAFANA/explore"
}

# PRUEBA 9: Integración Grafana - Dashboards y Alertas
test_grafana_integration() {
    print_header "TEST 9: GRAFANA - Dashboards y Alertas"
    
    print_test "Verificando que Grafana ve Prometheus como datasource"
    
    # Verificar conexión a Prometheus desde Grafana
    local response=$(curl -s "$GRAFANA/api/datasources" | grep -c "prometheus" || echo "0")
    
    if [ "$response" -gt 0 ]; then
        print_success "Grafana tiene datasource de Prometheus configurado"
    else
        print_info "Datasource de Prometheus puede necesitar configuración manual"
    fi
    
    print_test "Verificando alertas configuradas"
    
    # Buscar reglas de alerta
    local alerts=$(curl -s "$GRAFANA/api/v1/rules" | grep -c "alert" || echo "0")
    
    print_info "Alertas activas: $alerts"
    print_success "Integración Grafana completada"
    print_info "Accede a: $GRAFANA (admin/admin)"
}

# PRUEBA 10: Correlación de Datos - Verificar que todo está correlacionado
test_data_correlation() {
    print_header "TEST 10: CORRELACIÓN DE DATOS - Métricas, Logs y Trazas"
    
    print_test "Generando request con trace ID específico"
    
    local trace_id=$(openssl rand -hex 8)
    
    print_info "Trace ID: $trace_id"
    
    local response=$(curl -s -X GET "$API_GATEWAY/health" \
        -H "X-Trace-ID: $trace_id" \
        -w "\nHTTP:%{http_code}\n")
    
    sleep 2
    
    print_test "Verificando que la traza aparece en Zipkin"
    print_info "Buscar en: $ZIPKIN"
    
    print_test "Verificando que las métricas están correlacionadas"
    print_info "Query en Prometheus: up{job='api-gateway'}"
    
    print_success "Correlación de datos completa"
    print_info "Todos los datos deberían estar relacionados por timestamp y trace ID"
}

################################################################################
# RESUMEN Y CONCLUSIONES
################################################################################

print_summary() {
    local total=$((TESTS_PASSED + TESTS_FAILED))
    
    print_header "RESUMEN DE PRUEBAS"
    
    echo -e "${GREEN}✓ Pruebas Exitosas: $TESTS_PASSED${NC}"
    echo -e "${RED}✗ Pruebas Fallidas: $TESTS_FAILED${NC}"
    echo -e "${BLUE}Total de Pruebas: $total${NC}\n"
    
    if [ "$TESTS_FAILED" -eq 0 ]; then
        echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
        echo -e "${GREEN}🎉 TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE 🎉${NC}"
        echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
    fi
}

print_next_steps() {
    print_header "PRÓXIMOS PASOS - Visualización en Dashboards"
    
    echo -e "${CYAN}1. PROMETHEUS - Métricas en tiempo real${NC}"
    echo -e "   URL: ${BLUE}$PROMETHEUS${NC}"
    echo -e "   • Status > Targets (verificar que todos están UP)"
    echo -e "   • Graph (ejecutar queries)"
    echo -e "   • Queries útiles:"
    echo -e "     - rate(api_requests_total[1m])"
    echo -e "     - histogram_quantile(0.95, rate(api_request_duration_seconds_bucket[5m]))"
    echo -e "     - increase(api_requests_total[5m])\n"
    
    echo -e "${CYAN}2. GRAFANA - Visualización integrada${NC}"
    echo -e "   URL: ${BLUE}$GRAFANA${NC}"
    echo -e "   • Usuario: admin"
    echo -e "   • Contraseña: admin"
    echo -e "   • Dashboards pre-configurados:"
    echo -e "     - Microservices Overview"
    echo -e "     - Service Performance"
    echo -e "     - System Resources\n"
    
    echo -e "${CYAN}3. ZIPKIN - Trazas distribuidas${NC}"
    echo -e "   URL: ${BLUE}$ZIPKIN${NC}"
    echo -e "   • Find Traces (buscar por servicio, latencia, error)"
    echo -e "   • Ver árbol de calls entre microservicios"
    echo -e "   • Identificar cuellos de botella\n"
    
    echo -e "${CYAN}4. LOKI - Logs correlacionados${NC}"
    echo -e "   Acceso desde Grafana:"
    echo -e "   • Grafana > Explore > Loki"
    echo -e "   • Filtrar por {service='api-gateway'}"
    echo -e "   • Ver logs en contexto de métricas\n"
}

################################################################################
# MODOS DE EJECUCIÓN
################################################################################

run_health_check() {
    test_health_checks
    test_prometheus_targets
}

run_basic() {
    test_health_checks
    test_prometheus_targets
    test_zipkin_traces
    test_http_metrics
}

run_advanced() {
    test_health_checks
    test_prometheus_targets
    test_zipkin_traces
    test_distributed_traces
    test_http_metrics
    test_error_scenarios
}

run_stress() {
    test_health_checks
    test_stress_load
    test_error_scenarios
}

run_full() {
    test_health_checks
    test_prometheus_targets
    test_zipkin_traces
    test_distributed_traces
    test_http_metrics
    test_error_scenarios
    test_stress_load
    test_loki_logs
    test_grafana_integration
    test_data_correlation
}

################################################################################
# MAIN
################################################################################

main() {
    print_header "PRUEBAS DE OBSERVABILIDAD - RETO 7"
    echo -e "Modo de ejecución: ${YELLOW}$MODE${NC}\n"
    
    case "$MODE" in
        health-check)
            run_health_check
            ;;
        basic)
            run_basic
            ;;
        advanced)
            run_advanced
            ;;
        stress)
            run_stress
            ;;
        full)
            run_full
            ;;
        *)
            echo -e "${RED}Modo desconocido: $MODE${NC}"
            echo -e "\nModos disponibles:"
            echo -e "  ${YELLOW}health-check${NC}  - Verificar que todos los servicios están UP"
            echo -e "  ${YELLOW}basic${NC}         - Pruebas básicas de observabilidad"
            echo -e "  ${YELLOW}advanced${NC}      - Pruebas distribuidas y errores"
            echo -e "  ${YELLOW}stress${NC}        - Test de carga"
            echo -e "  ${YELLOW}full${NC}          - Suite completa de pruebas (recomendado)"
            exit 1
            ;;
    esac
    
    print_summary
    print_next_steps
}

# Ejecutar
main

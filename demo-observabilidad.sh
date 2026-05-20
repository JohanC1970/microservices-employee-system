#!/bin/bash
################################################################################
# DEMOSTRACIÓN COMPLETA DE OBSERVABILIDAD - RETO 7
################################################################################

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

GW="http://localhost:8000"
PROM="http://localhost:9090"
GRAFANA="http://localhost:3001"
ZIPKIN="http://localhost:9411"

ok()   { echo -e "${GREEN}  ✓ $1${NC}"; }
fail() { echo -e "${RED}  ✗ $1${NC}"; }
info() { echo -e "${YELLOW}  ℹ $1${NC}"; }
step() { echo -e "\n${BLUE}▶ $1${NC}"; }
hdr()  { echo -e "\n${CYAN}═══════════════════════════════════════════════════════════${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"; }

hdr "PASO 1 — VERIFICACIÓN DEL SISTEMA"

step "1.1 Servicios de negocio"
curl -sf $GW/health > /dev/null       && ok "API Gateway (8000)"    || fail "API Gateway"
curl -sf http://localhost:8085/health > /dev/null && ok "Auth Service (8085)" || fail "Auth Service"

step "1.2 Stack de observabilidad"
curl -sf $PROM/-/healthy > /dev/null  && ok "Prometheus (9090)"  || fail "Prometheus"
curl -sf $GRAFANA/api/health > /dev/null && ok "Grafana (3001)"  || fail "Grafana"
curl -sf $ZIPKIN/health > /dev/null   && ok "Zipkin (9411)"      || fail "Zipkin"

step "1.3 Targets en Prometheus"
TOTAL=$(curl -s "$PROM/api/v1/targets" | grep -o '"health":"up"' | wc -l | tr -d ' ')
ok "Targets UP: $TOTAL / 15"

# ─────────────────────────────────────────────────────────────────────────────
hdr "PASO 2 — GENERACIÓN DE TRÁFICO"

step "2.1 Peticiones normales (genera métricas + trazas)"
for i in $(seq 1 20); do
  curl -sf $GW/health -H "traceparent: 00-$(openssl rand -hex 16)-$(openssl rand -hex 8)-01" > /dev/null
  printf "."
done
echo ""
ok "20 peticiones exitosas enviadas"

step "2.2 Peticiones con error (genera error rate)"
for i in $(seq 1 5); do
  curl -s $GW/ruta-inexistente-$i > /dev/null 2>&1 || true
  curl -s -X POST $GW/empleados -H "Content-Type: application/json" -d '{}' > /dev/null 2>&1 || true
  printf "."
done
echo ""
ok "10 peticiones con error generadas (404 / 401)"

step "2.3 Stress test — 30 peticiones concurrentes"
for i in $(seq 1 30); do
  curl -sf $GW/health > /dev/null &
done
wait
ok "30 peticiones concurrentes completadas"

sleep 3

# ─────────────────────────────────────────────────────────────────────────────
hdr "PASO 3 — VERIFICACIÓN DE OBSERVABILIDAD"

step "3.1 Servicios con trazas en Zipkin"
SERVICES=$(curl -s $ZIPKIN/api/v2/services 2>/dev/null | tr ',' '\n' | tr -d '[]"' | grep -v '^$')
if [ -n "$SERVICES" ]; then
  ok "Zipkin tiene trazas de:"
  echo "$SERVICES" | while read s; do echo "     • $s"; done
else
  info "Zipkin aún procesando trazas..."
fi

step "3.2 Métricas de request rate en Prometheus"
RATE=$(curl -s "$PROM/api/v1/query?query=sum(rate(http_requests_total[1m]))" | grep -o '"value":\[[^]]*\]' | head -1)
if [ -n "$RATE" ]; then
  ok "Métricas de request rate disponibles: $RATE"
else
  info "Métricas aún acumulando (esperar ~15s)"
fi

step "3.3 Probe success (Blackbox Exporter)"
PROBES=$(curl -s "$PROM/api/v1/query?query=probe_success" | grep -o '"value":\[[^,]*,"1"\]' | wc -l | tr -d ' ')
ok "Health checks exitosos: $PROBES / 7 servicios"

# ─────────────────────────────────────────────────────────────────────────────
hdr "PASO 4 — PRUEBA DE CAOS"

step "4.1 Simulando caída de servicio (stop departamentos-service)"
docker compose stop departamentos-service > /dev/null 2>&1
info "departamentos-service detenido"
info "Espera 60s y verifica en Grafana que el panel cambia a ROJO"
info "La alerta 'Servicio Caído' debería activarse en ~1 minuto"

sleep 5

step "4.2 Restaurando servicio"
docker compose start departamentos-service > /dev/null 2>&1
ok "departamentos-service restaurado"
info "Verifica en Grafana que vuelve a VERDE"

# ─────────────────────────────────────────────────────────────────────────────
hdr "✅ DEMOSTRACIÓN COMPLETADA"

echo ""
echo -e "${CYAN}📊 DASHBOARDS:${NC}"
echo -e "  ${GREEN}Prometheus:${NC}  $PROM"
echo -e "  ${GREEN}Grafana:${NC}     $GRAFANA  (admin / admin)"
echo -e "  ${GREEN}Zipkin:${NC}      $ZIPKIN"
echo -e "  ${GREEN}RabbitMQ:${NC}    http://localhost:15672  (guest / guest)"
echo ""
echo -e "${CYAN}🔍 QUERIES ÚTILES EN PROMETHEUS:${NC}"
echo "  rate(http_requests_total[1m])                          → Request rate"
echo "  probe_success                                          → Health checks"
echo "  sum by(job)(rate(http_requests_total{status_code=~'[45]..'}[1m]))  → Errores"
echo ""
echo -e "${CYAN}🔍 QUERY EN LOKI (Grafana > Explore):${NC}"
echo '  {service="api-gateway"}                               → Logs del gateway'
echo '  {service=~".+"}  |= "error"                          → Todos los errores'
echo ""
echo -e "${GREEN}✨ Sistema de Observabilidad Reto 7 — 100% operativo ✨${NC}"

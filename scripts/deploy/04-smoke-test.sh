#!/usr/bin/env bash
# Verificação pós-deploy: saúde da API (com Oracle), CORS do front, login e leitura autenticada.
# Uso: bash scripts/deploy/04-smoke-test.sh   (usa ADMIN_EMAIL/ADMIN_PASSWORD do .env, se houver)
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars RESOURCE_GROUP WEB_APP_NAME FRONT_WEB_APP_NAME
FRONT_URL="https://$(front_host)"
FAIL=0
check() { # descrição, condição (0 = ok)
  if [ "$2" = "0" ]; then echo "  OK    $1"; else echo "  FALHA $1"; FAIL=1; fi
}

echo "API:   $API_URL"
echo "Front: $FRONT_URL"

CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 20 "$API_URL/actuator/health")
check "GET /actuator/health = 200 (HTTP $CODE)" "$([ "$CODE" = 200 ] && echo 0 || echo 1)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 20 "$API_URL/swagger-ui/index.html")
check "Swagger UI acessível (HTTP $CODE)" "$([ "$CODE" = 200 ] && echo 0 || echo 1)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 20 "$FRONT_URL/")
check "Front / = 200 (HTTP $CODE)" "$([ "$CODE" = 200 ] && echo 0 || echo 1)"
CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 20 "$FRONT_URL/login")
check "Front /login (rota SPA) = 200 (HTTP $CODE)" "$([ "$CODE" = 200 ] && echo 0 || echo 1)"

ALLOW=$(curl -s -D - -o /dev/null --max-time 20 -X OPTIONS "$API_URL/auth/login" \
  -H "Origin: $FRONT_URL" -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type,authorization" | tr -d '\r' \
  | awk -F': ' 'tolower($1)=="access-control-allow-origin"{print $2}')
check "CORS permite a origem do front (allow-origin='$ALLOW')" "$([ "$ALLOW" = "$FRONT_URL" ] && echo 0 || echo 1)"

if [ -n "${ADMIN_EMAIL:-}" ] && [ -n "${ADMIN_PASSWORD:-}" ]; then
  BODY=$(ADMIN_EMAIL="$ADMIN_EMAIL" ADMIN_PASSWORD="$ADMIN_PASSWORD" python3 -c \
    'import json,os;print(json.dumps({"email":os.environ["ADMIN_EMAIL"],"password":os.environ["ADMIN_PASSWORD"]}))')
  RESP=$(curl -s --max-time 30 -X POST "$API_URL/auth/login" -H 'Content-Type: application/json' -d "$BODY")
  TOKEN=$(printf '%s' "$RESP" | python3 -c 'import json,sys
try: print(json.load(sys.stdin).get("token",""))
except Exception: print("")')
  check "POST /auth/login (admin) devolve token" "$([ -n "$TOKEN" ] && echo 0 || echo 1)"
  if [ -n "$TOKEN" ]; then
    CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 30 "$API_URL/plans" -H "Authorization: Bearer $TOKEN")
    check "GET /plans autenticado = 200, lendo o Oracle (HTTP $CODE)" "$([ "$CODE" = 200 ] && echo 0 || echo 1)"
    CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 30 "$API_URL/plans")
    check "GET /plans sem token = 401/403 (HTTP $CODE)" "$([ "$CODE" = 401 ] || [ "$CODE" = 403 ] && echo 0 || echo 1)"
  fi
else
  echo "  (ADMIN_EMAIL/ADMIN_PASSWORD ausentes: login não testado)"
fi

echo
[ "$FAIL" = 0 ] && echo "Tudo certo." || { echo "Há falhas acima."; exit 1; }

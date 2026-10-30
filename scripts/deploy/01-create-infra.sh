#!/usr/bin/env bash
# Cria (ou reaproveita) a infraestrutura do PetFlow na Azure:
#   Resource Group -> App Service Plan (Linux B1) -> Web App da API (Java 21) + Web App do front (Node 22)
# NÃO cria banco: a API se conecta ao Oracle da FIAP (DB_URL do .env).
# Uso: bash scripts/deploy/01-create-infra.sh
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars LOCATION RESOURCE_GROUP APP_SERVICE_PLAN WEB_APP_NAME FRONT_WEB_APP_NAME \
             DB_URL DB_USERNAME DB_PASSWORD JWT_SECRET

echo "==> Assinatura Azure em uso"
require_az

echo "==> 1/5 Resource Group $RESOURCE_GROUP ($LOCATION)"
az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --output none

echo "==> 2/5 App Service Plan $APP_SERVICE_PLAN (Linux B1)"
az appservice plan show --resource-group "$RESOURCE_GROUP" --name "$APP_SERVICE_PLAN" --output none 2>/dev/null \
  || az appservice plan create --resource-group "$RESOURCE_GROUP" --name "$APP_SERVICE_PLAN" \
       --location "$LOCATION" --is-linux --sku B1 --output none

echo "==> 3/5 Web App $WEB_APP_NAME (Java 21)"
az webapp show --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" --output none 2>/dev/null \
  || az webapp create --resource-group "$RESOURCE_GROUP" --plan "$APP_SERVICE_PLAN" --name "$WEB_APP_NAME" \
       --runtime "JAVA:21-java21" --https-only true --output none

echo "==> 4/5 Web App do front $FRONT_WEB_APP_NAME (Node 22, serve o dist/ como SPA; mesmo plano, sem custo extra)"
az webapp show --resource-group "$RESOURCE_GROUP" --name "$FRONT_WEB_APP_NAME" --output none 2>/dev/null \
  || az webapp create --resource-group "$RESOURCE_GROUP" --plan "$APP_SERVICE_PLAN" --name "$FRONT_WEB_APP_NAME" \
       --runtime "NODE:22-lts" --https-only true --output none
az webapp config set --resource-group "$RESOURCE_GROUP" --name "$FRONT_WEB_APP_NAME" --always-on true \
  --startup-file "pm2 serve /home/site/wwwroot --no-daemon --spa" --output none
FRONT_URL="https://$(front_host)"
echo "    front: $FRONT_URL"

echo "==> 5/5 Variáveis de ambiente da API (valores não são exibidos)"
SETTINGS_FILE="$(mktemp)"
trap 'rm -f "$SETTINGS_FILE"' EXIT
chmod 600 "$SETTINGS_FILE"
FRONT_URL="$FRONT_URL" python3 - "$SETTINGS_FILE" <<'PY'
import json, os, sys
e = os.environ
settings = {
    "DB_URL": e["DB_URL"],
    "DB_USERNAME": e["DB_USERNAME"],
    "DB_PASSWORD": e["DB_PASSWORD"],
    "JWT_SECRET": e["JWT_SECRET"],
    "JWT_EXPIRATION_MS": e.get("JWT_EXPIRATION_MS", "86400000"),
    # A API só aceita requisições do front publicado.
    "CORS_ALLOWED_ORIGINS": e["FRONT_URL"],
    # O Spring escuta na 8080 (PORT/padrão); o App Service encaminha para ela.
    "WEBSITES_PORT": "8080",
    "WEBSITES_CONTAINER_START_TIME_LIMIT": "600",
}
with open(sys.argv[1], "w") as f:
    json.dump(settings, f)
PY
az webapp config appsettings set --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" \
  --settings "@$SETTINGS_FILE" --output none
az webapp config set --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" --always-on true --output none
az webapp log config --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" \
  --docker-container-logging filesystem --output none

echo
echo "Infraestrutura pronta."
echo "  API   : $API_URL"
echo "  Front : $FRONT_URL"
echo "Próximos passos: 02-deploy-api.sh, 03-deploy-front.sh, 04-smoke-test.sh"
az resource list --resource-group "$RESOURCE_GROUP" --query "[].{recurso:name, tipo:type}" --output table

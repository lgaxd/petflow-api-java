#!/usr/bin/env bash
# Compila (se preciso) e publica o JAR da API no App Service, esperando o /actuator/health.
# Uso: bash scripts/deploy/02-deploy-api.sh [caminho/do/petflow-api.jar]
#   Sem argumento: usa target/petflow-api.jar, gerando-o com Maven (precisa de JDK 21)
#   se ele não existir. SKIP_BUILD=1 impede a compilação.
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars RESOURCE_GROUP WEB_APP_NAME
JAR="${1:-$PROJECT_DIR/target/petflow-api.jar}"

if [ ! -f "$JAR" ]; then
  [ "${SKIP_BUILD:-0}" = "1" ] && { echo "ERRO: JAR não encontrado: $JAR"; exit 1; }
  echo "==> 0/2 Compilando (testes já rodam na revisão; aqui -DskipTests)"
  (cd "$PROJECT_DIR" && bash ./mvnw -B -q clean package -DskipTests)
fi
[ -f "$JAR" ] || { echo "ERRO: JAR não encontrado após o build: $JAR"; exit 1; }

echo "==> 1/2 Enviando $(basename "$JAR") para $WEB_APP_NAME"
az webapp deploy --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" \
  --src-path "$JAR" --type jar --track-status false --output none

echo "==> 2/2 Aguardando $API_URL/actuator/health responder UP (até 10 minutos)"
for i in $(seq 1 60); do
  CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$API_URL/actuator/health" || true)
  if [ "$CODE" = "200" ]; then
    echo "API saudável (aplicação + Oracle): $API_URL"
    echo "Swagger: $API_URL/swagger-ui.html"
    exit 0
  fi
  echo "  tentativa $i/60: HTTP $CODE"
  sleep 10
done

echo "ERRO: a API não ficou saudável."
echo "  503 = aplicação no ar, mas sem acesso ao Oracle (rede, usuário ou senha)."
echo "  000/502/503 logo após o deploy = ainda iniciando ou falha ao subir."
echo "Logs: az webapp log tail --resource-group $RESOURCE_GROUP --name $WEB_APP_NAME"
exit 1

#!/usr/bin/env bash
# Compila (se preciso) e publica o JAR da API no App Service, esperando o /actuator/health.
# Uso: bash scripts/deploy/02-deploy-api.sh [caminho/do/petflow-api.jar]
#   Sem argumento: recompila com Maven (JDK 21) e envia target/petflow-api.jar.
#   SKIP_BUILD=1 envia o JAR que já está em target/ sem recompilar.
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars RESOURCE_GROUP WEB_APP_NAME
JAR="${1:-$PROJECT_DIR/target/petflow-api.jar}"

# Sempre recompila (um JAR antigo em target/ seria enviado sem aviso), exceto quando
# um JAR é informado como argumento ou SKIP_BUILD=1.
if [ -z "${1:-}" ] && [ "${SKIP_BUILD:-0}" != "1" ]; then
  echo "==> 0/2 Compilando (testes já rodam na revisão; aqui -DskipTests)"
  (cd "$PROJECT_DIR" && bash ./mvnw -B -q clean package -DskipTests)
fi
[ -f "$JAR" ] || { echo "ERRO: JAR não encontrado após o build: $JAR"; exit 1; }

echo "==> 1/2 Enviando $(basename "$JAR") para $WEB_APP_NAME"
az webapp deploy --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" \
  --src-path "$JAR" --type jar --track-status false --output none

echo "==> 2/2 Aguardando $API_URL/actuator/health responder UP (até 10 minutos)"
# Atenção: enquanto o Spring não sobe, o App Service responde 200 com a página padrão
# "Welcome" em qualquer rota. Por isso o critério é o JSON {"status":"UP"}, não o código HTTP.
# Na primeira subida a aplicação pode levar vários minutos (plano B1).
for i in $(seq 1 60); do
  BODY=$(curl -s --max-time 10 "$API_URL/actuator/health" || true)
  if printf '%s' "$BODY" | grep -q '"status":"UP"'; then
    echo "API saudável (aplicação + Oracle): $API_URL"
    echo "Swagger: $API_URL/swagger-ui.html"
    exit 0
  fi
  case "$BODY" in
    *'"status":"DOWN"'*) echo "  tentativa $i/60: aplicação no ar, mas DOWN (provável falha de acesso ao Oracle)";;
    *"<html"*|*"<!DOCTYPE"*) echo "  tentativa $i/60: ainda iniciando (página padrão do App Service)";;
    *) echo "  tentativa $i/60: sem resposta";;
  esac
  sleep 10
done

echo "ERRO: a API não ficou saudável."
echo "  DOWN = aplicação no ar, mas sem acesso ao Oracle (rede, usuário ou senha)."
echo "  página padrão/sem resposta = ainda iniciando ou falha ao subir (veja os logs)."
echo "Logs: az webapp log tail --resource-group $RESOURCE_GROUP --name $WEB_APP_NAME"
exit 1

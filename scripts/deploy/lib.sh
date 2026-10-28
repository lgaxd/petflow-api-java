#!/usr/bin/env bash
# Funções comuns dos scripts de deploy (use com: source "$(dirname "$0")/lib.sh").
# Lê o .env da API e o scripts/deploy/.env.deploy SEM executá-los como shell,
# então valores com espaços, '&' ou '$' (ex.: senhas) chegam intactos.

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$DEPLOY_DIR/../.." && pwd)"

load_env_file() {
  local file="$1" line key value
  [ -f "$file" ] || return 0
  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    case "$line" in ''|\#*) continue ;; esac
    key="${line%%=*}"
    value="${line#*=}"
    [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
    # Aspas externas opcionais.
    if [[ "$value" =~ ^\"(.*)\"$ || "$value" =~ ^\'(.*)\'$ ]]; then value="${BASH_REMATCH[1]}"; fi
    # Variáveis já definidas no ambiente têm prioridade.
    [ -n "${!key+x}" ] || export "$key=$value"
  done < "$file"
}

load_env_file "$DEPLOY_DIR/.env.deploy"
load_env_file "$PROJECT_DIR/.env"

# Nomes derivados: só existem se o .env.deploy os definiu.
API_URL=""
[ -n "${WEB_APP_NAME:-}" ] && API_URL="https://${WEB_APP_NAME}.azurewebsites.net"

require_vars() {
  local v
  for v in "$@"; do
    [ -n "${!v:-}" ] || { echo "ERRO: defina $v em scripts/deploy/.env.deploy (ou no .env)"; exit 1; }
  done
}

require_az() {
  command -v az >/dev/null || { echo "ERRO: Azure CLI (az) não encontrado."; exit 1; }
  az account show --query "{assinatura:name, id:id}" --output table \
    || { echo "ERRO: faça 'az login' antes."; exit 1; }
}

# Hostname padrão do Web App do front (vazio se ainda não existir).
front_host() {
  az webapp show --name "$FRONT_WEB_APP_NAME" --resource-group "$RESOURCE_GROUP" \
    --query defaultHostName --output tsv 2>/dev/null || true
}

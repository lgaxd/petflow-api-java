#!/usr/bin/env bash
# APAGA o Resource Group (API, plano e front). O Oracle da FIAP não é afetado.
# Use só após a entrega e a nota. Uso: bash scripts/deploy/99-delete-infra.sh
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars RESOURCE_GROUP
read -r -p "Isto apaga TUDO em '$RESOURCE_GROUP'. Digite o nome do Resource Group para confirmar: " CONFIRMACAO
if [ "$CONFIRMACAO" != "$RESOURCE_GROUP" ]; then
  echo "Nome diferente. Nada foi apagado."
  exit 1
fi

az group delete --name "$RESOURCE_GROUP" --yes --no-wait
echo "Exclusão de '$RESOURCE_GROUP' iniciada (leva alguns minutos)."

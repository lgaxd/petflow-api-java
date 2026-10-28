#!/usr/bin/env bash
# Compila o front (React/Vite) apontando para a API publicada e envia ao Web App do front.
# Uso: bash scripts/deploy/03-deploy-front.sh   (FRONT_DIR vem do .env.deploy; precisa de Node 20+)
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

require_vars RESOURCE_GROUP WEB_APP_NAME FRONT_WEB_APP_NAME FRONT_DIR
FRONT_DIR="$(cd "$DEPLOY_DIR/$FRONT_DIR" 2>/dev/null && pwd || true)"
[ -f "$FRONT_DIR/package.json" ] || { echo "ERRO: FRONT_DIR inválido (sem package.json)."; exit 1; }
command -v node >/dev/null && command -v npm >/dev/null || { echo "ERRO: instale Node 20+ (node/npm)."; exit 1; }

echo "==> 1/3 Build do front com VITE_API_URL=$API_URL"
# Compila numa cópia temporária: o node_modules do Windows no repositório do front
# não serve no Linux e não deve ser apagado.
BUILD_DIR="$(mktemp -d)"
trap 'rm -rf "$BUILD_DIR"' EXIT
tar -C "$FRONT_DIR" --exclude=node_modules --exclude=dist --exclude=.git -cf - . | tar -C "$BUILD_DIR" -xf -
(cd "$BUILD_DIR" && npm ci --no-audit --no-fund && VITE_API_URL="$API_URL" npm run build)

echo "==> 2/3 Empacotando dist/ e enviando para $FRONT_WEB_APP_NAME"
python3 - "$BUILD_DIR/dist" "$BUILD_DIR/front.zip" <<'PY'
import os, sys, zipfile
src, out = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    for root, _, files in os.walk(src):
        for f in files:
            p = os.path.join(root, f)
            z.write(p, os.path.relpath(p, src))
PY
az webapp deploy --resource-group "$RESOURCE_GROUP" --name "$FRONT_WEB_APP_NAME" \
  --src-path "$BUILD_DIR/front.zip" --type zip --track-status false --output none

FRONT_URL="https://$(front_host)"
echo "==> 3/3 Aguardando $FRONT_URL responder 200 (até 5 minutos)"
for i in $(seq 1 30); do
  CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$FRONT_URL/login" || true)
  if [ "$CODE" = "200" ]; then echo "Front publicado: $FRONT_URL"; exit 0; fi
  echo "  tentativa $i/30: HTTP $CODE"
  sleep 10
done
echo "ERRO: o front não respondeu. Logs: az webapp log tail --resource-group $RESOURCE_GROUP --name $FRONT_WEB_APP_NAME"
exit 1

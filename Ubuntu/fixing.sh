#!/bin/bash

# Normaliza scripts .sh en el directorio actual

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
echo "Procesando scripts en: $SCRIPT_DIR"

for file in "$SCRIPT_DIR"/*.sh; do
  if [ -f "$file" ]; then
    echo "Procesando: $file"
    dos2unix "$file" 2>/dev/null
    chmod +x "$file"
    echo "✓ Convertido y permisos asignados: $(basename "$file")"
  fi
done

echo "==============================================="
echo "✅ Proceso finalizado. Todos los scripts .sh en $SCRIPT_DIR"
echo "   tienen ahora permisos de ejecucion y formato Unix."
echo "==============================================="

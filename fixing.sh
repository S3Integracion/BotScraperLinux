#!/bin/bash

# Obtener el directorio donde se ejecuta este script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
echo "Procesando scripts en: $SCRIPT_DIR"

# Convertir todos los archivos .sh en el directorio actual
for file in "$SCRIPT_DIR"/*.sh; do
  if [ -f "$file" ]; then
    echo "Procesando: $file"
    
    # Convertir formato de fin de línea Windows a Unix
    dos2unix "$file" 2>/dev/null
    
    # Asignar permisos de ejecución
    chmod +x "$file"
    
    echo "✓ Convertido y permisos asignados: $(basename "$file")"
  fi
done

echo "==============================================="
echo "✅ Proceso finalizado. Todos los scripts .sh en $SCRIPT_DIR"
echo "   tienen ahora permisos de ejecución y formato Unix."
echo "==============================================="

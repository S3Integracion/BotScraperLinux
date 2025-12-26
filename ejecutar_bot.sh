#!/bin/bash
# Script wrapper para ejecutar el bot Java con variables de entorno adecuadas
# Este script será llamado desde crontab

# Configurar variables de entorno necesarias para interfaz gráfica
export DISPLAY=:0
export XAUTHORITY="/home/$(whoami)/.Xauthority"
export HOME="/home/$(whoami)"
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin"

# Cambiar al directorio donde está el bot
cd "/home/$(whoami)/scraping/"

# Usar un archivo de log fijo en lugar de uno con timestamp
LOG_FILE="/home/$(whoami)/scraping/cron_ejecution.log"

# Eliminar el archivo de log anterior si existe
if [ -f "$LOG_FILE" ]; then
    rm "$LOG_FILE"
fi

echo "=== Iniciando ejecución programada del bot Java ===" > "$LOG_FILE"
echo "Fecha y hora de ejecución: $(date)" >> "$LOG_FILE"
echo "Usuario: $(whoami)" >> "$LOG_FILE"
echo "Directorio actual: $(pwd)" >> "$LOG_FILE"
echo "DISPLAY: $DISPLAY" >> "$LOG_FILE"

# Ejecutar el bot Java y registrar salida y errores
/usr/bin/java -cp . bot >> "$LOG_FILE" 2>&1

# Registrar resultado de la ejecución
RESULTADO=$?
echo "Código de salida: $RESULTADO" >> "$LOG_FILE"
echo "=== Fin de la ejecución programada ===" >> "$LOG_FILE"

exit $RESULTADO
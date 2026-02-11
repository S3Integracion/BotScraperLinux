#!/bin/bash
# Script wrapper para ejecutar el bot Java con variables de entorno adecuadas (Ubuntu X11)
# Este script será llamado desde crontab

# Configurar variables de entorno necesarias para interfaz gráfica
export DISPLAY=:0
export XAUTHORITY="/home/$(whoami)/.Xauthority"
export HOME="/home/$(whoami)"
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin"

# Forzar backend X11 para automatizacion grafica
export GDK_BACKEND=x11
export MOZ_ENABLE_WAYLAND=0

# Limpiar variables GTK del entorno de Snap si existen
unset GTK_EXE_PREFIX
unset GTK_PATH
unset GTK_IM_MODULE_FILE
unset GIO_MODULE_DIR
unset GTK_MODULES

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
echo "XDG_SESSION_TYPE: ${XDG_SESSION_TYPE:-desconocido}" >> "$LOG_FILE"
echo "GDK_BACKEND: ${GDK_BACKEND:-desconocido}" >> "$LOG_FILE"

# Ejecutar el bot Java y registrar salida y errores
/usr/bin/java -cp . bot >> "$LOG_FILE" 2>&1

# Registrar resultado de la ejecución
RESULTADO=$?
echo "Código de salida: $RESULTADO" >> "$LOG_FILE"
echo "=== Fin de la ejecución programada ===" >> "$LOG_FILE"

exit $RESULTADO
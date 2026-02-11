#!/bin/bash
# Wrapper para ejecutar el bot en Ubuntu con X11

export DISPLAY=:0
export HOME="/home/$(whoami)"
export XAUTHORITY="$HOME/.Xauthority"
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

# Asegurar bus de DBus para entorno de escritorio
USER_ID=$(id -u)
export DBUS_SESSION_BUS_ADDRESS="unix:path=/run/user/$USER_ID/bus"

SCRIPT_DIR="$(cd "$(dirname "$0")" &>/dev/null && pwd)"
WORK_DIR="$HOME/scraping"

if [ ! -d "$WORK_DIR" ]; then
    WORK_DIR="$SCRIPT_DIR"
fi

if [ ! -f "$WORK_DIR/bot.java" ] && [ -f "$SCRIPT_DIR/bot.java" ]; then
    WORK_DIR="$SCRIPT_DIR"
fi

cd "$WORK_DIR" || exit 1

LOG_FILE="$WORK_DIR/cron_ejecution.log"

if [ -f "$LOG_FILE" ]; then
    rm "$LOG_FILE"
fi

{
    echo "=== Iniciando ejecucion programada del bot Java (Ubuntu X11) ==="
    echo "Fecha y hora de ejecucion: $(date)"
    echo "Usuario: $(whoami)"
    echo "Directorio actual: $(pwd)"
    echo "DISPLAY: $DISPLAY"
    echo "XDG_SESSION_TYPE: ${XDG_SESSION_TYPE:-desconocido}"
    echo "GDK_BACKEND: ${GDK_BACKEND:-desconocido}"
} > "$LOG_FILE"

if [ -f "$WORK_DIR/bot.java" ]; then
    /usr/bin/javac bot.java >> "$LOG_FILE" 2>&1
fi

/usr/bin/java -cp . bot >> "$LOG_FILE" 2>&1

RESULTADO=$?
{
    echo "Codigo de salida: $RESULTADO"
    echo "=== Fin de la ejecucion programada ==="
} >> "$LOG_FILE"

exit $RESULTADO

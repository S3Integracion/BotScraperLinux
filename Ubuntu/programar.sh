#!/bin/bash
# Programa el bot en cron para Ubuntu con X11

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
BOT_WRAPPER="$HOME/scraping/ejecutar.sh"

if [ ! -d "$HOME/scraping" ]; then
    echo "Creando directorio scraping..."
    mkdir -p "$HOME/scraping"
fi

echo "Copiando wrapper a $HOME/scraping..."
cp "$SCRIPT_DIR/ejecutar.sh" "$BOT_WRAPPER"
chmod +x "$BOT_WRAPPER"

if [ ! -f "$HOME/scraping/bot.java" ]; then
    if [ -f "$SCRIPT_DIR/bot.java" ]; then
        echo "Copiando bot.java a $HOME/scraping..."
        cp "$SCRIPT_DIR/bot.java" "$HOME/scraping/"
    else
        echo "ADVERTENCIA: No se encontro bot.java."
    fi
fi

echo "Eliminando tareas previas del crontab..."
crontab -r 2>/dev/null || echo "No habia tareas previas en el crontab."

echo "Programando tarea del bot para Ubuntu X11..."
(
    echo "# Tarea programada para ejecutar el bot Java de lunes a viernes a las 00:30"
    echo "SHELL=/bin/bash"
    echo "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin"
    echo "15 0 * * 1-3 \"$BOT_WRAPPER\" # Ejecutar bot de scraping Ubuntu X11"
) | crontab -

echo "Verificando tarea en crontab..."
CRONTAB_CHECK=$(crontab -l)
CRONTAB_STATUS=$?

if [ $CRONTAB_STATUS -eq 0 ]; then
    if grep -q "ejecutar.sh" <<< "$CRONTAB_CHECK"; then
        echo "✅ Tarea programada correctamente."
        echo "✅ Log: $HOME/scraping/cron_ejecution.log"
    else
        echo "❌ La tarea no contiene ejecutar.sh"
        echo "$CRONTAB_CHECK"
    fi
else
    echo "❌ No se pudo leer el crontab."
fi

echo ""
echo "🤖 Ejecutando el bot automaticamente..."
if [ -x "$BOT_WRAPPER" ]; then
    nohup "$BOT_WRAPPER" >/dev/null 2>&1 &
    echo "🚀 Bot iniciado con PID: $!"
else
    echo "❌ Error: $BOT_WRAPPER no es ejecutable."
fi

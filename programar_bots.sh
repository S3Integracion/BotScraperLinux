#!/bin/bash
# Programa las tareas de los bots normales en el crontab del usuario actual

# Definir rutas absolutas para los scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
BOT_WRAPPER="/home/$(whoami)/scraping/ejecutar_bot.sh"

# Verificar que el directorio de scraping existe, si no, crearlo
if [ ! -d "/home/$(whoami)/scraping/" ]; then
    echo "Creando directorio scraping..."
    mkdir -p "/home/$(whoami)/scraping/"
fi

# Copiar el script wrapper a la carpeta de scraping
echo "Copiando script wrapper a la carpeta de scraping..."
cp "$SCRIPT_DIR/ejecutar_bot.sh" "$BOT_WRAPPER"

# Establecer permisos de ejecución para el script wrapper
echo "Estableciendo permisos de ejecución..."
chmod +x "$BOT_WRAPPER"

# Verificar que el archivo Java esté en la carpeta de scraping
if [ ! -f "/home/$(whoami)/scraping/bot.java" ]; then
    if [ -f "$SCRIPT_DIR/bot.java" ]; then
        echo "Copiando bot.java a la carpeta de scraping..."
        cp "$SCRIPT_DIR/bot.java" "/home/$(whoami)/scraping/"
    else
        echo "ADVERTENCIA: No se encontró bot.java. Asegúrese de copiarlo manualmente a /home/$(whoami)/scraping/"
    fi
fi

# Primero, elimina todas las tareas existentes en el crontab
echo "Eliminando todas las tareas existentes en el crontab..."
crontab -r 2>/dev/null || echo "No había tareas previas en el crontab."

# Luego, programa la nueva tarea para el bot Java usando el script wrapper
echo "Programando nueva tarea para el bot Java..."
(
    echo "# Tarea programada para ejecutar el bot Java de lunes a jueves a las 00:30"
    echo "SHELL=/bin/bash"
    echo "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin"
    echo "30 0 * * 1-5 \"$BOT_WRAPPER\" # Ejecutar bot de scraping"
) | crontab -

# Verificar que la tarea se programó correctamente
echo "Verificando que la tarea se programó correctamente..."
CRONTAB_CHECK=$(crontab -l)
CRONTAB_STATUS=$?

if [ $CRONTAB_STATUS -eq 0 ]; then
    if grep -q "ejecutar_bot.sh" <<< "$CRONTAB_CHECK"; then
        echo "✅ Tarea de bot Java programada correctamente en el crontab para ejecutarse de lunes a viernes a las 00:30."
        echo "✅ Se utilizará el script wrapper para garantizar el entorno adecuado."
        echo "✅ Los logs de ejecución se guardarán en: /home/$(whoami)/scraping/cron_ejecution.log"
        echo "✅ Todas las tareas anteriores han sido eliminadas."
    else
        echo "❌ Error: La tarea parece haberse creado pero no se encontró 'ejecutar_bot.sh' en ella."
        echo "Contenido actual del crontab:"
        echo "$CRONTAB_CHECK"
    fi
else
    echo "❌ Error: No se pudo leer el crontab. Código de error: $CRONTAB_STATUS"
    echo "Intente ejecutar manualmente: crontab -l"
fi

# Ejecutar automáticamente el bot después de programar la tarea
echo ""
echo "🤖 Ejecutando el bot automáticamente..."
echo "🤖 Esto puede tardar varios minutos, por favor espere..."
echo "🤖 La salida se registrará en: /home/$(whoami)/scraping/cron_ejecution.log"
echo ""

# Verificar que el ejecutable existe y tiene permisos
if [ -x "$BOT_WRAPPER" ]; then
    # Ejecutar en segundo plano para evitar bloqueo
    nohup "$BOT_WRAPPER" > /dev/null 2>&1 &
    BOT_PID=$!
    echo "🚀 El bot se ha iniciado con PID: $BOT_PID"
    echo "🚀 Para ver el progreso, puede ejecutar: tail -f /home/$(whoami)/scraping/cron_ejecution.log"
else
    echo "❌ Error: No se puede ejecutar el bot. El archivo $BOT_WRAPPER no existe o no tiene permisos de ejecución."
fi

# Informar sobre cómo probar manualmente (mantener esto para futuras referencias)
echo ""
echo "Para probar manualmente el script en otra ocasión, ejecute:"
echo "  $BOT_WRAPPER"

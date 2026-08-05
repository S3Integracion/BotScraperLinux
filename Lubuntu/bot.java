import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bot para automatizar tareas de Web Scraping
 * Versión en Java del script bot2.sh
 * Optimizado para Ubuntu con X11
 * Compatible con Java 21.0.8
 **/
public class bot {
    // Configuración
    private static final String URL = "https://amazon.com";
    private static final int WIN_X = 0;
    private static final int WIN_Y = 0;
    private static final int WIN_WIDTH = 1920;
    private static final int WIN_HEIGHT = 1080;

    // Coordenadas Y para cada día de la semana (sitemap)
    private static final int COORD_X_SITEMAP = 95; // Coordenada X fija para todos los sitemaps
    private static final Map<Integer, Integer> DAY_COORDINATES = new HashMap<>();
    static {
        // Coordenadas Y para cada día (Calendar.MONDAY = 2, etc.)
        DAY_COORDINATES.put(Calendar.MONDAY, 195); // Lunes - Utiliza primer Sitemap
        DAY_COORDINATES.put(Calendar.TUESDAY, 215); // Martes - Utiliza segundo Sitemap
        DAY_COORDINATES.put(Calendar.WEDNESDAY, 265); // Miércoles - Utiliza 4to Sitemap
        DAY_COORDINATES.put(Calendar.THURSDAY, 265); // Jueves - Utiliza 4to Sitemap
        DAY_COORDINATES.put(Calendar.FRIDAY, 290); // Viernes (añadido) - Utiliza 5to Sitemap
        DAY_COORDINATES.put(Calendar.SATURDAY, 315); // Sábado (añadido)
        DAY_COORDINATES.put(Calendar.SUNDAY, 340); // Domingo (añadido)
    }

    private static Robot robot;
    private static PrintWriter logWriter;
    // Cambiando nombre del archivo de log para que sea consistente
    private static final String LOG_FILE = System.getProperty("user.home") + File.separator + "cron_ejecution.log";

    public static void main(String[] args) {
        try {
            // Inicializar sistema de logs
            setupLogger();

            log("========= [" + getCurrentDateTime() + "] Archivo de log limpiado =========");
            log("========= [" + getCurrentDateTime() + "] Iniciando bot.java =========");

            log("Iniciando bot optimizado para Ubuntu con X11");

            // Verificar dependencias necesarias
            checkUbuntuX11Dependencies();

            // Inicializar robot para control de ratón y teclado
            robot = new Robot();
            robot.setAutoDelay(100); // Pequeño retraso entre acciones para estabilidad

            // Ejecutar la secuencia de automatización
            runAutomationSequence();

            logWriter.close();
        } catch (Exception e) {
            System.err.println("Error en la ejecución del bot: " + e.getMessage());
            e.printStackTrace();
            if (logWriter != null) {
                logWriter.println("Error en la ejecución del bot: " + e.getMessage());
                e.printStackTrace(logWriter);
                logWriter.close();
            }
        }
    }

    /**
     * Verifica que las herramientas necesarias estén instaladas
     */
    private static void checkUbuntuX11Dependencies() {
        log("Verificando dependencias necesarias para Ubuntu X11...");

        String[] requiredTools = { "xdotool", "wmctrl" };
        boolean allToolsAvailable = true;

        for (String tool : requiredTools) {
            if (!isCommandAvailable(tool)) {
                log("ADVERTENCIA: La herramienta '" + tool
                        + "' no está instalada. Algunas funciones pueden no estar disponibles.");
                log("Instale la herramienta con: sudo apt-get install " + tool);
                allToolsAvailable = false;
            } else {
                log("Herramienta '" + tool + "' disponible.");
            }
        }

        if (allToolsAvailable) {
            log("Todas las dependencias necesarias están disponibles.");
        } else {
            log("Algunas dependencias no están instaladas. La automatización puede fallar.");
            log("Ejecute este comando para instalar todas las dependencias necesarias:");
            log("sudo apt-get install xdotool wmctrl");
        }
    }

    /**
     * Verifica si un comando está disponible en el sistema
     */
    private static boolean isCommandAvailable(String command) {
        try {
            // Usando ProcessBuilder con características mejoradas de Java 21
            ProcessBuilder processBuilder = new ProcessBuilder("which", command);
            // Redirigir la salida de error para no contaminar la consola
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // En Java 21, podemos usar waitFor con un timeout para evitar bloqueos
            // indefinidos
            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log("La verificación del comando '" + command + "' excedió el tiempo de espera");
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            log("Error al verificar el comando '" + command + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Configura el sistema de registro en archivo
     * Elimina el contenido previo del archivo de log
     */
    private static void setupLogger() throws IOException {
        File logFile = new File(LOG_FILE);
        // Crear directorios si no existen
        if (logFile.getParentFile() != null) {
            logFile.getParentFile().mkdirs();
        }

        // Si el archivo existe, eliminarlo primero para garantizar un inicio limpio
        if (logFile.exists()) {
            try {
                logFile.delete();
                log("Archivo de log anterior eliminado correctamente");
            } catch (Exception e) {
                System.err.println("No se pudo eliminar el archivo de log anterior: " + e.getMessage());
            }
        }

        // Configurar el escritor para sobrescribir el archivo (false = no append)
        logWriter = new PrintWriter(new FileWriter(logFile, false));

        // Registrar la ubicación del archivo de log
        System.out.println("Archivo de log creado en: " + logFile.getAbsolutePath());
    }

    /**
     * Registra una acción en el archivo de log
     */
    private static void log(String message) {
        System.out.println(message);
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
        }
    }

    /**
     * Ejecuta un paso de la automatización y registra la acción
     */
    private static void runStep(String description, Runnable action) {
        log("[Acción] " + description);
        action.run();

        // En Ubuntu X11, algunas operaciones gráficas pueden necesitar más tiempo
        if (description.contains("Maximizando") || description.contains("Firefox")) {
            log("Esperando tiempo adicional para operación en ventana...");
            sleep(15000); // 7 segundos para operaciones que involucran ventanas
        } else {
            sleep(5000); // 5 segundos para otras acciones
        }
    }

    /**
    * Abre Firefox con la URL y tamaño especificados, específicamente optimizado
    * para Ubuntu X11
     */
    private static void openFirefox() {
        try {
            // En Ubuntu X11, usamos el comando firefox directamente
            ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/firefox",
                    "--new-window", URL,
                    "--window-size=" + WIN_WIDTH + "," + WIN_HEIGHT,
                    "--window-position=" + WIN_X + "," + WIN_Y);

            // Configurar variables de entorno para entornos gráficos en Linux
            Map<String, String> env = processBuilder.environment();
            env.put("DISPLAY", ":0");
            // La variable XAUTHORITY generalmente se configura automáticamente

            log("Ejecutando Firefox en Ubuntu X11...");
            Process process = processBuilder.start();

            // Esperar un momento para que Firefox se inicie
            sleep(3000);

            // Intentar maximizar la ventana usando xdotool
            log("Intentando maximizar la ventana de Firefox usando xdotool...");
            maximizeWindowUsingXdotool("Firefox");
        } catch (IOException e) {
            log("Error al abrir Firefox: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
    * Maximiza una ventana usando xdotool en Ubuntu X11
     * 
     * @param windowName nombre o clase de la ventana para identificarla
     */
    private static void maximizeWindowUsingXdotool(String windowName) {
        try {
            // Obtener el ID de la ventana
            ProcessBuilder getWindowIdCmd = new ProcessBuilder("bash", "-c",
                    "xdotool search --class \"" + windowName + "\" | head -n 1");
            Process process = getWindowIdCmd.start();

            // Leer la salida del comando para obtener el ID de la ventana
            java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            String windowId = s.hasNext() ? s.next().trim() : "";
            s.close();

            if (!windowId.isEmpty()) {
                log("ID de ventana de " + windowName + " encontrado: " + windowId);

                // Activar la ventana y maximizarla
                new ProcessBuilder("xdotool", "windowactivate", windowId).start();
                sleep(500);
                new ProcessBuilder("xdotool", "windowsize", windowId, "100%", "100%").start();

                // Alternativa: usar wmctrl para maximizar
                new ProcessBuilder("wmctrl", "-i", "-r", windowId, "-b", "add,maximized_vert,maximized_horz").start();

                log("Ventana maximizada correctamente");
            } else {
                log("No se pudo encontrar la ventana: " + windowName);
            }
        } catch (IOException e) {
            log("Error al maximizar ventana: " + e.getMessage());
        }
    }

    /**
     * Mueve el ratón a una posición específica
     */
    private static void moveMouse(int x, int y) {
        robot.mouseMove(x, y);
    }

    /**
     * Hace clic con el botón izquierdo del ratón
     */
    private static void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Presiona una tecla
     */
    private static void pressKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    /**
     * Presiona una combinación de teclas
     */
    private static void pressKeys(int... keyCodes) {
        // Presionar todas las teclas en orden
        for (int keyCode : keyCodes) {
            robot.keyPress(keyCode);
        }

        // Liberar todas las teclas en orden inverso
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
        }
    }

    /**
     * Escribe un texto
     */
    private static void typeText(String text) {
        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                log("No se puede escribir el carácter: " + c);
            } else {
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }
        }
    }

    /**
     * Pausa la ejecución por un tiempo determinado
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Obtiene la fecha y hora actual en formato legible
     */
    private static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * Obtiene el día actual de la semana según Calendar
     * 
     * @return un entero que representa el día (Calendar.MONDAY, Calendar.TUESDAY,
     *         etc.)
     */
    private static int getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Obtiene el nombre del día de la semana actual en español
     */
    private static String getDayName() {
        int dayOfWeek = getCurrentDayOfWeek();
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Lunes";
            case Calendar.TUESDAY:
                return "Martes";
            case Calendar.WEDNESDAY:
                return "Miércoles";
            case Calendar.THURSDAY:
                return "Jueves";
            case Calendar.FRIDAY:
                return "Viernes";
            case Calendar.SATURDAY:
                return "Sábado";
            case Calendar.SUNDAY:
                return "Domingo";
            default:
                return "Desconocido";
        }
    }

    /**
     * Obtiene la coordenada Y correspondiente al día actual
     * 
     * @return la coordenada Y del sitemap para el día actual o una coordenada por
     *         defecto si no se encuentra
     */
    private static int getSitemapYCoordinate() {
        int dayOfWeek = getCurrentDayOfWeek();
        // Si no se encuentra el día en el mapa, usamos la coordenada del lunes como
        // valor predeterminado
        // Usando método getOrDefault que es más moderno y eficiente en Java 21
        return DAY_COORDINATES.getOrDefault(dayOfWeek, DAY_COORDINATES.get(Calendar.MONDAY));
    }

    /**
     * Cierra todas las instancias abiertas de Firefox
     */
    private static void closeAllFirefoxWindows() {
        log("Cerrando todas las ventanas existentes de Firefox...");

        try {
            // Primero intentamos cerrar Firefox de manera limpia con xdotool
            ProcessBuilder xdotoolCmd = new ProcessBuilder("bash", "-c",
                    "xdotool search --class firefox windowactivate --sync %@ key --clearmodifiers alt+F4");
            Process xdotoolProcess = xdotoolCmd.start();
            boolean completed = xdotoolProcess.waitFor(5, TimeUnit.SECONDS);

            if (!completed) {
                log("El comando xdotool excedió el tiempo de espera, usando método alternativo");
                xdotoolProcess.destroyForcibly();
            }

            // Esperamos un momento para que las ventanas se cierren
            sleep(2000);

            // Verificamos si Firefox sigue ejecutándose
            ProcessBuilder checkCmd = new ProcessBuilder("bash", "-c", "pgrep firefox");
            Process checkProcess = checkCmd.start();
            int exitCode = checkProcess.waitFor();

            // Si Firefox sigue ejecutándose (exitCode = 0), lo terminamos con pkill
            if (exitCode == 0) {
                log("Firefox sigue ejecutándose, terminando el proceso con pkill");
                ProcessBuilder killCmd = new ProcessBuilder("pkill", "-9", "firefox");
                killCmd.start().waitFor(3, TimeUnit.SECONDS);
            }

            // Esperamos un poco más para asegurarnos que el proceso terminó
            sleep(2000);

            log("Todas las ventanas de Firefox han sido cerradas");
        } catch (IOException | InterruptedException e) {
            log("Error al cerrar Firefox: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
    * Verifica si el sistema operativo es Ubuntu con X11
     */
    private static boolean isUbuntuX11() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("linux")) {
            return false;
        }

        try {
            ProcessBuilder distroCheck = new ProcessBuilder("bash", "-c",
                    "cat /etc/os-release 2>/dev/null | grep -i 'ubuntu' || cat /etc/lsb-release 2>/dev/null | grep -i 'ubuntu'");
            distroCheck.redirectErrorStream(true);
            Process distroProcess = distroCheck.start();

            java.util.Scanner s = new java.util.Scanner(distroProcess.getInputStream()).useDelimiter("\\A");
            String distro = s.hasNext() ? s.next().trim().toLowerCase() : "";
            s.close();

            String sessionType = System.getenv("XDG_SESSION_TYPE");
            boolean isUbuntu = !distro.isEmpty();
            boolean isX11Session = sessionType == null || sessionType.trim().equalsIgnoreCase("x11");

            log("Detección de entorno: Ubuntu=" + isUbuntu + ", X11=" + isX11Session);
            return isUbuntu && isX11Session;
        } catch (Exception e) {
            log("Error al verificar si es Ubuntu X11: " + e.getMessage());
            return false;
        }
    }

    /**
    * Verifica si las DevTools están abiertas en Firefox en Ubuntu X11
     * 
     * @return true si las DevTools están abiertas, false en caso contrario
     */
    private static boolean areDevToolsOpen() {
        log("Verificando si las DevTools están abiertas...");

        try {
            // Método optimizado para X11: buscar DevTools en el título de ventana
            ProcessBuilder titleCheckCmd = new ProcessBuilder("bash", "-c",
                    "xdotool search --onlyvisible --class 'Firefox' getwindowname | grep -E 'DevTools|Inspector|Depurador|Herramientas|Ferramenta|Desarrollador'");
            Process titleCheckProcess = titleCheckCmd.start();
            java.util.Scanner s = new java.util.Scanner(titleCheckProcess.getInputStream()).useDelimiter("\\A");
            String result = s.hasNext() ? s.next().trim() : "";
            s.close();

            if (!result.isEmpty()) {
                log("DevTools detectadas en título de ventana Firefox");
                return true;
            }

            // Verificar si hay una ventana secundaria de Firefox (posible DevTools
            // separada)
            ProcessBuilder windowCountCmd = new ProcessBuilder("bash", "-c",
                    "xdotool search --onlyvisible --class 'Firefox' | wc -l");
            Process windowCountProcess = windowCountCmd.start();
            s = new java.util.Scanner(windowCountProcess.getInputStream()).useDelimiter("\\A");
            String countResult = s.hasNext() ? s.next().trim() : "0";
            s.close();

            int windowCount = Integer.parseInt(countResult);
            if (windowCount > 1) {
                log("Múltiples ventanas de Firefox detectadas (" + windowCount + ") - Posibles DevTools separadas");
                return true;
            }

            log("No se detectaron DevTools abiertas");
            return false;
        } catch (IOException | NumberFormatException e) {
            log("Error al verificar DevTools: " + e.getMessage());
            return false;
        }
    }

    /**
    * Maximiza una ventana usando métodos específicos para X11
     */
    private static void maximizeWindowUsingX11(String windowClass) {
        try {
            log("Maximizando ventana '" + windowClass + "' usando métodos específicos para X11");

            // Obtener ID de la ventana
            ProcessBuilder getWindowIdCmd = new ProcessBuilder("bash", "-c",
                    "xdotool search --class \"" + windowClass + "\" | head -n 1");
            Process process = getWindowIdCmd.start();

            java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            String windowId = s.hasNext() ? s.next().trim() : "";
            s.close();

            if (!windowId.isEmpty()) {
                // Método 1: usar xfwm4 si está disponible (entornos basados en XFCE)
                if (isCommandAvailable("xfwm4-command")) {
                    new ProcessBuilder("bash", "-c",
                            "WINDOW_ID=" + windowId + " && xfwm4-command --maximize").start();
                    sleep(500);
                }

                // Método 2: usar wmctrl (más compatible)
                new ProcessBuilder("wmctrl", "-i", "-r", windowId, "-b", "add,maximized_vert,maximized_horz").start();

                // Método 3: Usar xdotool para activar y maximizar
                new ProcessBuilder("xdotool", "windowactivate", "--sync", windowId,
                        "windowsize", "100%", "100%").start();

                log("Ventana maximizada correctamente");
            } else {
                log("No se pudo encontrar la ventana '" + windowClass + "'");
            }
        } catch (IOException e) {
            log("Error al maximizar ventana: " + e.getMessage());
        }
    }

    /**
     * Intenta abrir las DevTools con métodos específicos
     */
    private static boolean openDevToolsWithRetry(int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log("Intento " + attempt + " de " + maxRetries + " para abrir DevTools");

            // Intentar con Ctrl+Shift+I
            pressKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_I);
            sleep(2000);

            // Si falló, intentar con F12 que también funciona en Firefox
            if (!areDevToolsOpen()) {
                log("Intentando con F12...");
                pressKey(KeyEvent.VK_F12);
                sleep(2000);
            }

            // Si falló, intentar el menú contextual
            if (!areDevToolsOpen() && attempt > 1) {
                log("Intentando con el menú contextual...");
                // Hacer clic derecho en la página
                robot.mouseMove(400, 400);
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                sleep(1000);

                // Mover a "Inspeccionar" o "Inspect" y hacer clic
                robot.mouseMove(450, 500);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                sleep(3000);
            }

            if (areDevToolsOpen()) {
                log("DevTools abiertas correctamente (intento " + attempt + ")");
                return true;
            }

            if (attempt < maxRetries) {
                log("Limpiando estado antes del siguiente intento...");
                pressKey(KeyEvent.VK_ESCAPE); // Cerrar diálogos
                sleep(1000);
                pressKey(KeyEvent.VK_F5); // Refrescar
                sleep(5000);
            }
        }

        log("No se pudieron abrir las DevTools después de " + maxRetries + " intentos");
        return false;
    }

    /**
     * Ejecuta la secuencia completa de automatización
     */
    private static void runAutomationSequence() {
        // Registrar información del día actual
        String dayName = getDayName();
        int dayOfWeek = getCurrentDayOfWeek();
        int sitemapYCoord = getSitemapYCoordinate();

        log("========= INFORMACIÓN DEL DÍA =========");
        log("Día detectado: " + dayName + " (valor Calendar: " + dayOfWeek + ")");
        log("Coordenadas del sitemap a utilizar: X=" + COORD_X_SITEMAP + ", Y=" + sitemapYCoord);
        log("=======================================");

        // Cerrar todas las ventanas existentes de Firefox antes de comenzar
        runStep("Cerrando ventanas existentes de Firefox", () -> {
            closeAllFirefoxWindows();
        });

        // Abrir Firefox con la URL especificada
        runStep("Abriendo Firefox", () -> {
            try {
                // Usar firefox o firefox-esr según esté disponible
                String firefoxCmd = isCommandAvailable("firefox") ? "firefox"
                        : (isCommandAvailable("firefox-esr") ? "firefox-esr" : "/usr/bin/firefox");

                ProcessBuilder processBuilder = new ProcessBuilder(firefoxCmd,
                        "--new-window", URL,
                        "--window-size=" + WIN_WIDTH + "," + WIN_HEIGHT);

                // Configurar variables de entorno para entornos gráficos
                Map<String, String> env = processBuilder.environment();
                env.put("DISPLAY", ":0");

                log("Ejecutando Firefox...");
                Process process = processBuilder.start();

                // Esperar a que Firefox se inicie
                sleep(5000);

                // Maximizar usando métodos específicos para X11
                maximizeWindowUsingX11("Firefox");
            } catch (IOException e) {
                log("Error al abrir Firefox: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Esperar a que Firefox se abra completamente
        sleep(6000); // 6 segundos como en el script original

        // Abrir DevTools con sistema de reintentos
        runStep("Abriendo DevTools", () -> {
            if (!openDevToolsWithRetry(3)) {
                log("ADVERTENCIA: No se pudieron abrir las DevTools después de múltiples intentos");
                log("El proceso continuará, pero podría no funcionar correctamente");
            }
        });

        // Solo continuar si se abrieron las DevTools
        if (areDevToolsOpen()) {
            // Maximizar las DevTools específicamente para Ubuntu X11
            runStep("Maximizando DevTools en Ubuntu X11", () -> {
                try {
                    // Usar comando para maximizar la ventana activa
                    ProcessBuilder xfceMaximizeCmd = new ProcessBuilder("bash", "-c",
                            "xdotool getactivewindow windowsize 100% 100%");
                    xfceMaximizeCmd.start();
                    sleep(500);

                    // Método adicional para maximizar
                    ProcessBuilder wmctrlCmd = new ProcessBuilder("wmctrl", "-r", ":ACTIVE:", "-b",
                            "add,maximized_vert,maximized_horz");
                    wmctrlCmd.start();
                } catch (IOException e) {
                    log("Error al maximizar DevTools en Ubuntu X11: " + e.getMessage());
                }
            });

            // Mover el ratón y hacer clic (coordenadas del script original)
            runStep("Haciendo clic en posición (1028, 68)", () -> {
                moveMouse(1028, 68);
                leftClick();
            });

            sleep(2000); // 2 segundos adicionales

            // Mover ratón y hacer clic en el primer botón de sitemaps
            runStep("Haciendo clic en botón de sitemaps (60, 90)", () -> {
                moveMouse(60, 90);
                leftClick();
            });

            // Mover ratón y hacer clic en el sitemap correspondiente al día actual
            runStep("Haciendo clic en sitemap del " + dayName + " (" + COORD_X_SITEMAP + ", " + sitemapYCoord + ")",
                    () -> {
                        moveMouse(COORD_X_SITEMAP, sitemapYCoord);
                        leftClick();
                    });

            // Mover ratón y hacer clic en el menú scrap
            runStep("Haciendo clic en menú scrap (170, 90)", () -> {
                moveMouse(170, 90);
                leftClick();
            });

            // Mover ratón y hacer clic en start scrap
            runStep("Haciendo clic en start scrap (175, 200)", () -> {
                moveMouse(175, 200);
                leftClick();
            });

            // Presionar TAB
            runStep("Presionando TAB", () -> {
                pressKey(KeyEvent.VK_TAB);
            });

            // Escribir "6000"
            runStep("Escribiendo 6000", () -> {
                typeText("6000");
            });

            // Presionar TAB
            runStep("Presionando TAB", () -> {
                pressKey(KeyEvent.VK_TAB);
            });

            // Escribir "7000"
            runStep("Escribiendo 7000", () -> {
                typeText("7000");
            });

            // Presionar TAB
            runStep("Presionando TAB", () -> {
                pressKey(KeyEvent.VK_TAB);
            });

            // Mover ratón y hacer clic en el botón final de start scrap
            runStep("Haciendo clic en botón final de start scrap (155, 215)", () -> {
                moveMouse(155, 215);
                leftClick();
            });

            log("Secuencia de automatización completada.");
        } else {
            log("ERROR CRÍTICO: No se pudieron abrir las DevTools. Abortando la secuencia de automatización.");
            // Opcionalmente, podemos intentar una recuperación o cerrar Firefox
            runStep("Cerrando Firefox debido a error en DevTools", () -> {
                closeAllFirefoxWindows();
            });
        }
    }
}
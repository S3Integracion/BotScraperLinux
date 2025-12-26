#!/bin/bash

# Script para instalar todas las dependencias necesarias para el bot Java
# Incluye JDK 21, xdotool, wmctrl, zenity y Firefox

# Colores para los mensajes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir mensajes con formato
print_message() {
    echo -e "${GREEN}[INSTALADOR]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[ADVERTENCIA]${NC} $1"
}

# Función para verificar si un comando está disponible
check_command() {
    if command -v $1 &> /dev/null; then
        return 0 # Comando encontrado
    else
        return 1 # Comando no encontrado
    fi
}

# Función para instalar un paquete si no está instalado
install_package() {
    if dpkg -l | grep -q $1; then
        print_message "El paquete $1 ya está instalado."
    else
        print_message "Instalando $1..."
        sudo apt-get install -y $1
        if [ $? -eq 0 ]; then
            print_message "Paquete $1 instalado correctamente."
        else
            print_error "Error al instalar $1. Por favor, revise los mensajes de error."
            exit 1
        fi
    fi
}

# Verificar si se está ejecutando como root
if [ "$(id -u)" -eq 0 ]; then
    print_error "Este script no debe ejecutarse como root directamente."
    print_message "En su lugar, ejecute el script como usuario normal. Las operaciones que requieran privilegios usarán sudo."
    exit 1
fi

# Comprobar si estamos en Xubuntu/Ubuntu
if [ -f /etc/os-release ]; then
    . /etc/os-release
    if [[ "$ID" != "ubuntu" && "$ID_LIKE" != *"ubuntu"* ]]; then
        print_warning "Este script está diseñado para Xubuntu/Ubuntu. Algunas operaciones pueden no funcionar correctamente en $PRETTY_NAME."
    fi
else
    print_warning "No se pudo determinar la distribución. Este script está diseñado para Xubuntu/Ubuntu."
fi

print_message "Iniciando instalación de dependencias para el bot de scraping..."

# Actualizar lista de paquetes
print_message "Actualizando lista de paquetes..."
sudo apt-get update

# Instalar herramientas básicas
print_message "Instalando herramientas básicas..."
install_package "software-properties-common"
install_package "curl"
install_package "wget"
install_package "apt-transport-https"
install_package "ca-certificates"
install_package "gnupg"

# Instalar JDK 21
print_message "Configurando repositorio para JDK 21..."

# Comprobar si ya tenemos el repositorio de Adoptium añadido
if ! grep -q "adoptium" /etc/apt/sources.list /etc/apt/sources.list.d/* 2>/dev/null; then
    # Añadir el repositorio de Adoptium para JDK 21
    print_message "Añadiendo repositorio de Adoptium..."
    
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
    echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    
    sudo apt-get update
fi

# Instalar JDK 21
print_message "Instalando JDK 21..."
install_package "temurin-21-jdk"

# Verificar la instalación de Java
if check_command java; then
    java_version=$(java -version 2>&1 | head -n 1)
    print_message "Java instalado correctamente: $java_version"
else
    print_error "Java no se instaló correctamente. Por favor, revise los mensajes de error."
    exit 1
fi

# Instalar herramientas para automatización gráfica
print_message "Instalando herramientas para automatización gráfica..."
install_package "xdotool"
install_package "wmctrl"
install_package "zenity"

# Instalar Firefox si no está instalado
if ! check_command firefox; then
    print_message "Instalando Firefox..."
    install_package "firefox"
else
    print_message "Firefox ya está instalado."
fi

# Crear directorios necesarios
print_message "Creando directorios para el bot..."
mkdir -p $HOME/scraping
print_message "Directorio creado: $HOME/scraping"

# Mover el archivo bot.java al directorio de scraping si existe en el directorio actual
if [ -f "bot.java" ]; then
    print_message "Moviendo bot.java al directorio de scraping..."
    cp bot.java $HOME/scraping/
    print_message "Archivo bot.java copiado a $HOME/scraping/"
else
    print_warning "No se encontró el archivo bot.java en el directorio actual."
    print_message "Por favor, asegúrese de copiar manualmente el archivo bot.java al directorio $HOME/scraping/"
fi

# Compilar el archivo Java para verificar que todo funciona
if [ -f "$HOME/scraping/bot.java" ]; then
    print_message "Compilando bot.java para verificar la instalación..."
    cd $HOME/scraping/
    javac bot.java
    
    if [ -f "$HOME/scraping/bot.class" ]; then
        print_message "Compilación exitosa. El bot está listo para ser ejecutado."
    else
        print_error "Error al compilar bot.java. Por favor, revise el código y las dependencias."
    fi
fi

# Configurar permisos de ejecución para los scripts
print_message "Configurando permisos de ejecución para los scripts..."
chmod +x $HOME/scraping/*.sh 2>/dev/null

# Resumen final
print_message "=============================================="
print_message "Instalación completada"
print_message "=============================================="
print_message "JDK 21 instalado: $(java -version 2>&1 | head -n 1)"
print_message "xdotool instalado: $(xdotool --version 2>&1 | head -n 1)"
print_message "Firefox instalado: $(firefox --version 2>&1)"
print_message "Directorio de trabajo: $HOME/scraping/"
print_message ""
print_message "Para ejecutar el bot manualmente:"
print_message "  cd $HOME/scraping/"
print_message "  java bot"
print_message ""
print_message "Para programar la ejecución automática, use el script programar_bots.sh"
print_message "=============================================="

exit 0
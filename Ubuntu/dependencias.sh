#!/bin/bash

# Script para instalar dependencias para Ubuntu con X11
# Incluye JDK 21, xdotool, wmctrl, zenity y Chrome/Chromium

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_message() {
    echo -e "${GREEN}[INSTALADOR-UBUNTU-X11]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[ADVERTENCIA]${NC} $1"
}

check_command() {
    if command -v "$1" >/dev/null 2>&1; then
        return 0
    fi
    return 1
}

install_package() {
    if dpkg -l | grep -q "$1"; then
        print_message "El paquete $1 ya está instalado."
    else
        print_message "Instalando $1..."
        sudo apt-get install -y "$1"
        if [ $? -ne 0 ]; then
            print_error "Error al instalar $1."
            exit 1
        fi
        print_message "Paquete $1 instalado correctamente."
    fi
}

if [ "$(id -u)" -eq 0 ]; then
    print_error "No ejecute este script como root. Use un usuario normal."
    exit 1
fi

if [ -f /etc/os-release ]; then
    . /etc/os-release
    if [[ "$ID" != "ubuntu" && "$ID_LIKE" != *"ubuntu"* ]]; then
        print_warning "Este script esta orientado a Ubuntu con X11. Sistema detectado: $PRETTY_NAME"
    fi
else
    print_warning "No se pudo determinar la distribucion."
fi

print_message "Iniciando instalacion de dependencias para Ubuntu X11..."

print_message "Actualizando lista de paquetes..."
sudo apt-get update

print_message "Instalando herramientas basicas..."
install_package "software-properties-common"
install_package "curl"
install_package "wget"
install_package "apt-transport-https"
install_package "ca-certificates"
install_package "gnupg"

print_message "Configurando repositorio para JDK 21..."
if ! grep -q "adoptium" /etc/apt/sources.list /etc/apt/sources.list.d/* 2>/dev/null; then
    print_message "Anadiendo repositorio de Adoptium..."
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
    echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null
    sudo apt-get update
fi

print_message "Instalando JDK 21..."
install_package "temurin-21-jdk"

if check_command java; then
    java_version=$(java -version 2>&1 | head -n 1)
    print_message "Java instalado correctamente: $java_version"
else
    print_error "Java no se instalo correctamente."
    exit 1
fi

print_message "Instalando herramientas de automatizacion grafica..."
install_package "xdotool"
install_package "wmctrl"
install_package "zenity"

if ! check_command google-chrome && ! check_command chromium && ! check_command chromium-browser; then
    print_message "Instalando Chromium..."
    if sudo apt-get install -y chromium-browser; then
        print_message "Chromium (chromium-browser) instalado correctamente."
    elif sudo apt-get install -y chromium; then
        print_message "Chromium instalado correctamente."
    else
        print_warning "No se pudo instalar Chromium. Instale Google Chrome o Chromium manualmente."
    fi
else
    print_message "Chrome/Chromium ya esta instalado."
fi

print_message "Creando directorio de trabajo..."
mkdir -p "$HOME/scraping"

if [ -f "bot.java" ]; then
    print_message "Copiando bot.java a $HOME/scraping/"
    cp bot.java "$HOME/scraping/"
else
    print_warning "No se encontro bot.java en el directorio actual."
fi

if [ -f "$HOME/scraping/bot.java" ]; then
    print_message "Compilando bot.java..."
    (cd "$HOME/scraping" && javac bot.java)
fi

print_message "Configurando permisos de ejecucion para scripts..."
chmod +x "$HOME/scraping"/*.sh 2>/dev/null

print_message "=============================================="
print_message "Instalacion completada"
print_message "=============================================="
print_message "Directorio de trabajo: $HOME/scraping/"
print_message "Para ejecutar el bot manualmente:"
print_message "  cd $HOME/scraping && java bot"
print_message ""
if [ "${XDG_SESSION_TYPE:-}" = "wayland" ]; then
    print_warning "Sesion Wayland detectada. Use una sesion X11 para evitar limitaciones de automatizacion grafica."
else
    print_message "Sesion X11 detectada o no especificada."
fi
print_message "=============================================="

exit 0

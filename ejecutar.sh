#!/bin/sh
cd "$(dirname "$0")" || exit 1
# Siempre se compila antes de ejecutar, para no correr una version vieja que haya quedado en la carpeta out
sh compilar.sh >/dev/null || exit 1
java -cp out "${1:-juego.Main}"

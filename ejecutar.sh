#!/bin/sh
cd "$(dirname "$0")" || exit 1
[ -f out/juego/Main.class ] || sh compilar.sh
java -cp out "${1:-juego.Main}"

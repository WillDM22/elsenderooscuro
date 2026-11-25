#!/bin/sh
cd "$(dirname "$0")" || exit 1
rm -rf out
mkdir out
javac -encoding UTF-8 -d out -sourcepath src src/juego/Main.java src/juego/pruebas/PruebaJuego.java src/juego/graficos/ExportadorSprites.java src/verificador/PruebaMisEstructuras.java

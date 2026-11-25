@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro javac. Instala un JDK 11 o superior y reabri esta ventana.
    pause
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out

javac -encoding UTF-8 -d out -sourcepath src src\juego\Main.java src\juego\pruebas\PruebaJuego.java src\juego\graficos\ExportadorSprites.java src\verificador\PruebaMisEstructuras.java
if errorlevel 1 (
    echo.
    echo La compilacion fallo. Revisa los mensajes de arriba.
    pause
    exit /b 1
)

echo Compilacion terminada: carpeta out
if not "%~1"=="silencioso" pause

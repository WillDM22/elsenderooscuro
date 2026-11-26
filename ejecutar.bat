@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo No se encontro java. Instala un JDK 11 o superior y reabri esta ventana.
    pause
    exit /b 1
)

rem Siempre se compila antes de ejecutar, para no correr una version vieja que haya quedado en la carpeta out
call compilar.bat silencioso
if not exist out\juego\Main.class (
    echo No se pudo compilar el proyecto.
    pause
    exit /b 1
)

set "CLASE=%~1"
if "%CLASE%"=="" set "CLASE=juego.Main"

java -cp out %CLASE%
if errorlevel 1 (
    echo.
    echo El programa termino con error. Revisa los mensajes de arriba.
    pause
)

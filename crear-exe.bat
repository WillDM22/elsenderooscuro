@echo off
setlocal enableextensions enabledelayedexpansion
cd /d "%~dp0"

echo ==============================================
echo   Solis Umbra - crear el ejecutable (.exe)
echo ==============================================
echo.

rem Si el JDK que esta en el PATH es viejo, se busca uno nuevo (con jpackage) en las carpetas habituales
where jpackage >nul 2>nul
if errorlevel 1 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JDKBIN=%JAVA_HOME%\bin"
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*" "%ProgramFiles%\Java\jdk-*" "%ProgramFiles%\Microsoft\jdk-*" "%ProgramFiles%\Amazon Corretto\jdk*" "%ProgramFiles%\Zulu\zulu*" "%ProgramFiles%\BellSoft\LibericaJDK-*") do (
        if exist "%%~D\bin\jpackage.exe" set "JDKBIN=%%~D\bin"
    )
    if defined JDKBIN (
        echo Usando el JDK de: !JDKBIN!
        set "PATH=!JDKBIN!;!PATH!"
    )
)

where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro javac. Instala un JDK 17 o superior y reabri esta ventana.
    pause
    exit /b 1
)
where jpackage >nul 2>nul
if errorlevel 1 (
    echo.
    echo El JDK instalado es viejo: no trae jpackage ^(aparece desde el JDK 14^).
    echo Instala un JDK 17 o superior, por ejemplo "Temurin 17" o "Temurin 21" ^(adoptium.net^),
    echo y despues volve a abrir este archivo. No hace falta desinstalar el anterior.
    echo.
    pause
    exit /b 1
)

echo [1/4] Compilando...
call compilar.bat silencioso
if not exist out\juego\Main.class (
    echo No se pudo compilar el proyecto.
    pause
    exit /b 1
)

echo [2/4] Armando el .jar (codigo + imagenes)...
if exist build rmdir /s /q build
mkdir build\input\datos
jar --create --file build\input\SolisUmbra.jar --main-class juego.Main -C out . -C src ui
if errorlevel 1 (
    echo No se pudo crear el .jar
    pause
    exit /b 1
)
copy /y datos\eventos.txt build\input\datos\eventos.txt >nul

echo [3/4] Creando el ejecutable con el icono...
if exist dist rmdir /s /q dist
jpackage --type app-image --name "Solis Umbra" --app-version 1.0 --input build\input --main-jar SolisUmbra.jar --main-class juego.Main --icon icono.ico --java-options "-Dsolis.base=$APPDIR" --dest dist
if errorlevel 1 (
    echo jpackage fallo. Revisa los mensajes de arriba.
    pause
    exit /b 1
)

rmdir /s /q build

echo [4/4] Listo.
echo.
echo El juego quedo en la carpeta:  dist\Solis Umbra
echo Para jugar abri:               dist\Solis Umbra\Solis Umbra.exe
echo.
echo Esa carpeta ya trae su propio Java: se puede comprimir en .zip y pasar a cualquier PC con Windows,
echo sin instalar nada. Hay que pasar la carpeta completa, no solo el .exe.
pause

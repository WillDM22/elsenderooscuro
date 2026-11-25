# El Sendero Oscuro

Roguelike 2D de toma de decisiones con estética de fantasía medieval pixel-art, inspirado en *Peglin*.
Proyecto final de **Programación II** (Kit Game Jam Java del profesor Goya). Desarrollo individual: William.

Elegís tu camino por un bosque ramificado (un **grafo**), enfrentás eventos con 2 o 3 decisiones y peleás
con un minijuego de precisión estilo *Undertale*. Si llegás al Rey Oscuro y lo vencés, ganás. Si caés, perdés la partida.

## Compilar y ejecutar

Requiere un JDK 11 o superior (probado con JDK 21).

| Sistema | Compilar | Ejecutar el juego |
| --- | --- | --- |
| Linux / macOS | `./compilar.sh` | `./ejecutar.sh` |
| Windows | `compilar.bat` | `ejecutar.bat` |

Siempre **desde la carpeta raíz del proyecto** (donde están `datos/` y `assets/`).
En un IDE: abrí `src` como carpeta de fuentes, codificación **UTF-8**, y que el directorio de trabajo sea la raíz.

| Para qué | Comando |
| --- | --- |
| Pruebas del juego (265 comprobaciones, sin ventana) | `java -cp out juego.pruebas.PruebaJuego` |
| Verificador de las estructuras del kit (Lista, Pila, Cola) | `java -cp out verificador.PruebaMisEstructuras` |
| Exportar los sprites a `assets/` para editarlos | `java -cp out juego.graficos.ExportadorSprites` |

## Controles

| Pantalla | Teclas |
| --- | --- |
| Menú | ↑ ↓ elegir, ENTER confirmar (también funciona el mouse) |
| Mapa | ↑ ↓ (o ← →) elegir destino, ENTER avanzar, **H** diario, **G** guardar, ESC guardar y volver al menú |
| Evento / cofre | ↑ ↓ o números 1 a 3, ENTER decidir |
| Combate | ← → ↑ ↓ o números 1 a 4 para elegir habilidad; **ESPACIO** frena la barra de precisión; ENTER continúa |

## El juego en una página

- **Héroe humano único** con Fuerza (vida y daño), Destreza (defensa y barra más fácil) e Inteligencia (maná, hechizos y curación).
- **4 habilidades** que se desbloquean en eventos y cofres: Ataque con arma, Luz sanadora, Golpe arcano y Paso sombrío (escape; no sirve contra el jefe).
- **Mapa en grafo**: 7 pisos, cada lugar es una calavera (combate), un `?` (evento, puede ser una pelea), un cofre o el jefe final.
- **Barra de precisión**: frenás el cursor; cerca del centro es crítico, en la zona media es normal, afuera es fallo.
- **Guardado automático** en `datos/guardado.txt`; los eventos se leen de `datos/eventos.txt` (podés agregar los tuyos).

## Estructura

```
src/                  código fuente (Java)
  estructuras/        librería propia: ListaEnlazada, PilaEnlazada, ColaEnlazada, Grafo (+ interfaces del kit)
  utilidades/         librería propia: Tokenizador (parte textos sin arreglos)
  motor/ contratos/ ui/ verificador/   kit del profesor (no se modifica salvo PruebaMisEstructuras)
  juego/
    entidades/        Jugador, Habilidad (y 4 hijas), Enemigo (y 4 hijos), fábricas
    reglas/           Combate, BarraPrecision, MapaBosque, Partida, eventos
    archivos/         GestorArchivos (persistencia con java.io)
    graficos/         Sprites, paleta y dibujo medieval con Graphics2D
    recursos/         Recursos (objetos compartidos entre pantallas)
    pantallas/        Escenas: menú, mapa, evento, combate, fin
    pruebas/          PruebaJuego
assets/               imágenes .png opcionales (si no están, se dibuja todo con Graphics2D)
datos/                eventos.txt (lectura) y guardado.txt (se crea al jugar)
docs/                 documentación para la entrega y la defensa oral
```

## Documentación (`docs/`)

1. `01-perfil-desarrollador.md`
2. `02-roles-y-acta-de-contrato.md`
3. `03-diseno-y-diagrama-de-clases.md`: paquetes, clases, cómo se conectan stats, grafo y archivos
4. `04-historial-de-desarrollo.md`
5. `05-guia-de-estudio-y-defensa.md`: preguntas posibles del profesor y cómo justificar cada estructura
6. `06-explicacion-clase-por-clase.md`: todo el código explicado desde cero

## Abrirlo en Visual Studio Code

1. Instalar el **Extension Pack for Java** (Microsoft) y tener un JDK 11 o superior.
2. Abrir con *File > Open Folder* la carpeta que contiene `src/`, `datos/` y `assets/` (no la carpeta `src`).
3. Esperar a que abajo diga **Java: Ready**.
4. Abrir `src/juego/Main.java`: arriba de `main` aparece **Run | Debug**. También sirve **F5** o el panel *Run and Debug* (ya hay configuraciones listas en `.vscode/launch.json`).

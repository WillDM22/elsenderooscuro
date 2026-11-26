# Solis Umbra

Roguelike 2D de toma de decisiones con estética de fantasía medieval pixel-art, inspirado en *Peglin*.
Proyecto final de **Programación II**, desarrollado en equipo.

Elegís una clase (caballero, mago, druida o pícaro), recorrés una mazmorra ramificada (un **grafo**), enfrentás eventos
con 2 o 3 decisiones y peleás por turnos con minijuegos de precisión estilo *Undertale*. Si llegás al Rey Oscuro y lo vencés, ganás. Si caés, perdés la partida.

## Compilar y ejecutar

Requiere un JDK 11 o superior (probado con JDK 21).

| Sistema | Compilar | Ejecutar el juego |
| --- | --- | --- |
| Linux / macOS | `./compilar.sh` | `./ejecutar.sh` |
| Windows | `compilar.bat` | `ejecutar.bat` |

Los scripts de ejecución **compilan siempre antes de abrir el juego**, así nunca se corre una versión vieja.

Siempre **desde la carpeta raíz del proyecto** (donde están `datos/`, `assets/` y `src/ui/`).
En un IDE: abrí `src` como carpeta de fuentes, codificación **UTF-8**, y que el directorio de trabajo sea la raíz.

| Para qué | Comando |
| --- | --- |
| Pruebas del juego (403 comprobaciones, sin ventana) | `java -cp out juego.pruebas.PruebaJuego` |
| Verificador de las estructuras del kit (Lista, Pila, Cola) | `java -cp out verificador.PruebaMisEstructuras` |
| Exportar los sprites a `assets/` para editarlos | `java -cp out juego.graficos.ExportadorSprites` |

## Crear el ejecutable (.exe)

En Windows, con un JDK 17 o superior instalado, hacé doble clic en `crear-exe.bat`. Compila, arma el `.jar` y genera
la carpeta `dist\Solis Umbra` con **`Solis Umbra.exe`** (con el ícono `icono.ico`). Esa carpeta incluye su propio Java:
se puede comprimir y pasar a otra PC con Windows sin instalar nada (hay que pasar la carpeta completa).
Las partidas y opciones se guardan dentro de esa misma carpeta (`app\datos`).

## Controles

| Pantalla | Teclas |
| --- | --- |
| Menú | ↑ ↓ ← → elegir, ENTER confirmar (también funciona el mouse). "Opciones" cambia la pantalla (normal, grande o completa), el brillo y el tamaño del texto |
| Elegir clase | ← → elegir (o números 1 a 4), ENTER empezar, ESC volver |
| Mapa | ↑ ↓ (o ← →) elegir destino (↑ sube, ↓ baja), ENTER avanzar, **H** diario, **G** guardar, **O** opciones, ESC guardar y volver al menú |
| Evento / cofre | ↑ ↓ o números 1 a 3, ENTER decidir |
| Combate | Menú estilo Pokémon: **Luchar** (arma o puños), **Magia**, **Defender** y **Huir** (el botón dice "Correr"); ← → ↑ ↓ o números para elegir, ESC vuelve al menú anterior. **ESPACIO** o **click izquierdo** juega el minijuego; ENTER continúa |

## El juego en una página

- **Héroe humano con 4 clases**: Caballero (resistente, espada), Mago (mucho maná, bastón), Druida (equilibrado, savia que cura) y Pícaro (antes "Ladrón"; ágil, dagas con crítico x3 y veneno). Cada una tiene atributos, arma y hechizo inicial propios.
- **Atributos**: Fuerza (vida y daño físico), Destreza (defensa, energía y huida) e Inteligencia (maná y poder mágico).
- **Dos recursos**: el **maná** paga los hechizos (máximo 4 por héroe, se aprenden en eventos y cofres: Luz Sanadora, Golpe Arcano, Paso sombrío, Tajo Heroico, Savia vital y Daga envenenada) y la **energía** paga el arma (cuesta más o menos según la clase) y los puños (2). La energía se llena al empezar cada combate y recupera 3 por ronda; **Defender** da +4 de energía y reduce a la mitad el daño de la ronda.
- **Acciones de combate**: Luchar, Magia, Defender y Huir (en pantalla, "Correr"; probabilidad según la Destreza; no se puede huir del jefe).
- **Tres minijuegos al azar** al atacar: *barra de precisión* (frenar el cursor), *ráfaga* (machacar ESPACIO antes de que se acabe el tiempo) y *anillo* (apretar cuando el aro toca la marca). Cuantos más enemigos derrotás, más rápidos y más exigentes se vuelven (nivel hasta 12).
- **7 tipos de enemigos** que aparecen según el piso: Esqueleto, Lobo, Goblin (roba oro), Araña (envenena), Brujo (se cura), Espectro (se cura al golpear), Golem (golpe doble cargado) y el jefe Rey Oscuro. Algunos usan **veneno**, que también puede usar el jugador.
- **Mapa en grafo**: 8 pisos, cada lugar es una calavera (combate), un `?` (evento, puede ser una pelea), un cofre o el jefe final.
- **Opciones**: pantalla (Normal 1024x640, Grande 1280x800 o Pantalla completa sin estirar, con franjas negras si el monitor no es 16:10), brillo y tamaño del texto; se guardan en `datos/opciones.txt`.
- **Guardado automático** en `datos/guardado.txt`; los eventos se leen de `datos/eventos.txt` (podés agregar los tuyos).

## Estructura

```
icono.ico             ícono del juego para el .exe
crear-exe.bat         genera el ejecutable de Windows (carpeta dist)
src/                  código fuente (Java)
  estructuras/        librería propia: ListaEnlazada, PilaEnlazada, ColaEnlazada, Grafo (+ interfaces del kit)
  utilidades/         librería propia: Tokenizador (parte textos sin arreglos)
  motor/ contratos/ ui/ verificador/   kit base de la materia (no se modifica salvo PruebaMisEstructuras)
  juego/
    entidades/        Jugador, Clase (enum), Habilidad (y sus hijas), Enemigo (y sus hijos), fábricas
    reglas/           Combate, Minijuego (barra, ráfaga, anillo), Dificultad, MapaMazmorra, Partida, eventos
    archivos/         GestorArchivos (persistencia con java.io)
    graficos/         Sprites, paleta y dibujo medieval con Graphics2D; ImagenesUi (carga de PNG), Fuentes, Personajes (poses)
    recursos/         Recursos (objetos compartidos entre pantallas)
    opciones/         Opciones (resolución, brillo, texto)
    ventana/          VentanaJuego y Escalado (modos de pantalla; el juego sigue dibujando en 1024x640 y se agranda sin deformarse)
    pantallas/        Escenas: menú, opciones, clases, mapa, evento, combate, fin
    pruebas/          PruebaJuego
src/ui/               arte de la interfaz hecho por el equipo (comparte carpeta con el paquete `ui` del kit): fondos, paneles, botones, sprites de personajes, fuentes, puntero e ícono
assets/               imágenes .png opcionales (si no están, se dibuja todo con Graphics2D)
datos/                eventos.txt (lectura); guardado.txt y opciones.txt (se crean al jugar)
docs/                 documentación del proyecto
```

## Arte y apariencia

Todo el arte de la interfaz vive en `src/ui/` (fondos, paneles, botones, hojas de sprites, fuentes `Alkhemikal` y
`gothic-pixel-font`, puntero del mouse, `ico.png` y las ilustraciones de los eventos). Las pantallas se dibujan sobre una resolución lógica de 1024x640 y
`GuiaP/` guarda las maquetas de cómo debe verse cada una. El juego busca las imágenes en el classpath (`/ui/...`) y, si
no, en la carpeta `src/ui` (por eso se ejecuta con la raíz del proyecto como directorio de trabajo). Si falta una imagen
no se cae: avisa por consola y dibuja una versión simple. `ico.png` se usa como ícono de la ventana y, si algún día se
publica una versión web, sirve también como favicon. Antes de publicar el juego conviene revisar las licencias de las fuentes (`Alkhemikal` y `gothic-pixel-font`).

## Documentación (`docs/`)

1. `01-perfil-desarrollador.md`: perfil del equipo
2. `02-roles-y-acta-de-contrato.md`
3. `03-diseno-y-diagrama-de-clases.md`: paquetes, clases, cómo se conectan stats, grafo y archivos
4. `04-historial-de-desarrollo.md`
5. `05-guia-de-estudio-y-defensa.md`: preguntas frecuentes y cómo justificar cada estructura
6. `06-explicacion-clase-por-clase.md`: todo el código explicado desde cero

## Abrirlo en Visual Studio Code

1. Instalar el **Extension Pack for Java** (Microsoft) y tener un JDK 11 o superior.
2. Abrir con *File > Open Folder* la carpeta que contiene `src/`, `datos/` y `assets/` (no la carpeta `src`).
3. Esperar a que abajo diga **Java: Ready**.
4. Abrir `src/juego/Main.java`: arriba de `main` aparece **Run | Debug**. También sirve **F5** o el panel *Run and Debug* (ya hay configuraciones listas en `.vscode/launch.json`).

# El Sendero Oscuro

Roguelike 2D de toma de decisiones con estética de fantasía medieval pixel-art, inspirado en *Peglin*.
Proyecto final de **Programación II** (Kit Game Jam Java del profesor Goya). Desarrollo individual: William.

Elegís una clase (caballero, mago, druida o ladrón), recorrés un bosque ramificado (un **grafo**), enfrentás eventos
con 2 o 3 decisiones y peleás por turnos con minijuegos de precisión estilo *Undertale*. Si llegás al Rey Oscuro y lo vencés, ganás. Si caés, perdés la partida.

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
| Pruebas del juego (395 comprobaciones, sin ventana) | `java -cp out juego.pruebas.PruebaJuego` |
| Verificador de las estructuras del kit (Lista, Pila, Cola) | `java -cp out verificador.PruebaMisEstructuras` |
| Exportar los sprites a `assets/` para editarlos | `java -cp out juego.graficos.ExportadorSprites` |

## Controles

| Pantalla | Teclas |
| --- | --- |
| Menú | ↑ ↓ elegir, ENTER confirmar (también funciona el mouse). "Opciones" cambia la pantalla (normal, grande o completa), el brillo y el tamaño del texto |
| Elegir clase | ← → elegir (o números 1 a 4), ENTER empezar, ESC volver |
| Mapa | ↑ ↓ (o ← →) elegir destino (↑ sube, ↓ baja), ENTER avanzar, **H** diario, **G** guardar, **O** opciones, ESC guardar y volver al menú |
| Evento / cofre | ↑ ↓ o números 1 a 3, ENTER decidir |
| Combate | Menú estilo Pokémon: **Luchar** (arma o puños), **Magia**, **Defender** y **Huir**; ← → ↑ ↓ o números para elegir, ESC vuelve al menú anterior. **ESPACIO** juega el minijuego; ENTER continúa |

## El juego en una página

- **Héroe humano con 4 clases**: Caballero (resistente, espada), Mago (mucho maná, bastón), Druida (equilibrado, savia que cura) y Ladrón (ágil, dagas con crítico x3 y veneno). Cada una tiene atributos, arma y hechizo inicial propios.
- **Atributos**: Fuerza (vida y daño físico), Destreza (defensa, energía y huida) e Inteligencia (maná y poder mágico).
- **Dos recursos**: el **maná** paga los hechizos (máximo 4 por héroe, se aprenden en eventos y cofres: Luz sanadora, Golpe arcano, Paso sombrío, Tajo heroico, Savia vital y Daga envenenada) y la **energía** paga el arma (cuesta más o menos según la clase) y los puños (2). La energía se llena al empezar cada combate y recupera 3 por ronda; **Defender** da +4 de energía y reduce a la mitad el daño de la ronda.
- **Acciones de combate**: Luchar, Magia, Defender y Huir (probabilidad según la Destreza; no se puede huir del jefe).
- **Tres minijuegos al azar** al atacar: *barra de precisión* (frenar el cursor), *ráfaga* (machacar ESPACIO antes de que se acabe el tiempo) y *anillo* (apretar cuando el aro toca la marca). Cuantos más enemigos derrotás, más rápidos y más exigentes se vuelven (nivel hasta 12).
- **7 tipos de enemigos** que aparecen según el piso: Esqueleto, Lobo, Goblin (roba oro), Araña (envenena), Brujo (se cura), Espectro (se cura al golpear), Golem (golpe doble cargado) y el jefe Rey Oscuro. Algunos usan **veneno**, que también puede usar el jugador.
- **Mapa en grafo**: 7 pisos, cada lugar es una calavera (combate), un `?` (evento, puede ser una pelea), un cofre o el jefe final.
- **Opciones**: pantalla (Normal 1024x640, Grande 1280x800 o Pantalla completa sin estirar, con franjas negras si el monitor no es 16:10), brillo y tamaño del texto; se guardan en `datos/opciones.txt`.
- **Guardado automático** en `datos/guardado.txt`; los eventos se leen de `datos/eventos.txt` (podés agregar los tuyos).

## Estructura

```
src/                  código fuente (Java)
  estructuras/        librería propia: ListaEnlazada, PilaEnlazada, ColaEnlazada, Grafo (+ interfaces del kit)
  utilidades/         librería propia: Tokenizador (parte textos sin arreglos)
  motor/ contratos/ ui/ verificador/   kit del profesor (no se modifica salvo PruebaMisEstructuras)
  juego/
    entidades/        Jugador, Clase (enum), Habilidad (y sus hijas), Enemigo (y sus hijos), fábricas
    reglas/           Combate, Minijuego (barra, ráfaga, anillo), Dificultad, MapaBosque, Partida, eventos
    archivos/         GestorArchivos (persistencia con java.io)
    graficos/         Sprites, paleta y dibujo medieval con Graphics2D
    recursos/         Recursos (objetos compartidos entre pantallas)
    opciones/         Opciones (resolución, brillo, texto)
    ventana/          VentanaJuego y Escalado (modos de pantalla; el juego sigue dibujando en 1024x640 y se agranda sin deformarse)
    pantallas/        Escenas: menú, opciones, clases, mapa, evento, combate, fin
    pruebas/          PruebaJuego
assets/               imágenes .png opcionales (si no están, se dibuja todo con Graphics2D)
datos/                eventos.txt (lectura); guardado.txt y opciones.txt (se crean al jugar)
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

# Historial de desarrollo

Un día de trabajo por entrada. Los mensajes de commit sugeridos están entre comillas.

- **Día 1:** Se lee el enunciado nuevo (listas, pilas, colas, **grafos** y **persistencia**), se elige el género (roguelike de decisiones, inspirado en Peglin) y se parte de la base del juego anterior: kit, `PilaEnlazada` y `ColaEnlazada`. Se retiran las clases del juego viejo. *"Estructura inicial de El Sendero Oscuro"*
- **Día 2:** `ListaEnlazada` simple con inserción, borrado e `indiceDe`. Se actualiza `PruebaMisEstructuras` y las tres estructuras pasan el verificador. *"Lista enlazada propia; el verificador pasa"*
- **Día 3:** `Grafo` dirigido con listas de adyacencia, `agregarArista` sin lazos ni repetidos y `hayCamino`. *"Grafo dirigido con listas de adyacencia"*
- **Día 4:** Recorridos del grafo: anchura con una `ColaEnlazada` y profundidad con una `PilaEnlazada`. Pruebas con un grafo de 5 vértices. *"BFS y DFS sobre el grafo"*
- **Día 5:** `Tokenizador` (librería propia para partir textos sin arreglos) y su prueba. *"Tokenizador propio"*
- **Día 6:** `Jugador` con Fuerza, Destreza e Inteligencia y sus fórmulas, validaciones con `IllegalArgumentException`. *"Jugador y atributos"*
- **Día 7:** `Habilidad` abstracta y `HabilidadAtaque`, `HabilidadCuracion`, `HabilidadGolpePotente`, `HabilidadEscape`; `CatalogoHabilidades`. El repertorio es una `ListaEnlazada` de máximo 4. *"Habilidades con polimorfismo"*
- **Día 8:** `Enemigo` abstracta, `Esqueleto`, `Lobo`, `Brujo` y `ReyOscuro`, `FabricaEnemigos`. *"Enemigos con comportamiento propio"*
- **Día 9:** `BarraPrecision`: cursor que rebota, zonas crítica y normal, efecto de la Destreza. *"Barra de precisión"*
- **Día 10:** `Combate` con la `ColaEnlazada` de turnos y su máquina de estados (elección, apuntando, resultado, victoria, derrota, huida). *"Combate por turnos con cola"*
- **Día 11:** `MapaBosque`: generación del grafo por pisos a partir de una semilla; se prueba con 40 semillas que todo nodo es alcanzable. *"Mapa generado como grafo"*
- **Día 12:** `Partida` con las dos pilas (ruta y diario) y el movimiento por las aristas. *"Partida, ruta y diario con pilas"*
- **Día 13:** `EfectoEvento`, `OpcionEvento` y `EventoNarrativo` (2 o 3 opciones en una lista). *"Eventos de decisión"*
- **Día 14:** `GestorArchivos`: lectura de `eventos.txt` y escritura/lectura de `guardado.txt` con `BufferedReader` y `PrintWriter`; el archivo dañado devuelve `null` sin romper el juego. *"Persistencia en archivos"*
- **Día 15:** Primer intento de guardar el grafo completo en el archivo; se descarta: alcanza con la **semilla**, porque el mapa se regenera igual. *"Guardado con semilla"*
- **Día 16:** `Paleta`, `Sprite` y `CatalogoSprites`: sprites 16x16 dibujados con texto; `GestorSprites` con `.png` opcional en `assets/`. *"Sprites con respaldo procedural"*
- **Día 17:** `DibujoMedieval` y `TextoPixel`: fondos de bosque, senderos punteados, marcos de pergamino, cuadros de diálogo y flecha. *"Dibujo medieval con Graphics2D"*
- **Día 18:** `EscenaMenu`, `EscenaMapa` (dibuja el grafo, elige destino con teclado y mouse) y `Hud`. *"Menú y mapa"*
- **Día 19:** `EscenaEvento` (máquina de escribir, opciones, emboscadas que llevan al combate) y los 11 eventos de `eventos.txt`. *"Pantalla de eventos"*
- **Día 20:** `EscenaCombate` con barra, turnos visibles, sacudidas y números flotantes; `EscenaFin`. *"Pantalla de combate y final"*
- **Día 21:** Autoguardado al volver al mapa, permadeath (se borra el guardado al morir o ganar) y progreso del héroe cada 2 victorias. *"Autoguardado y crecimiento del héroe"*
- **Día 22:** `PruebaJuego` (265 comprobaciones) con un "jugador robot" que juega aventuras completas para revisar el equilibrio; se corrige que `Recursos` quedara en un ciclo de dependencias entre paquetes. *"Pruebas, balance y paquetes sin ciclos"*
- **Día 23:** `ExportadorSprites`, revisión de las reglas del enunciado y documentación. *"Documentación y entrega"*

- **Día 24:** Primera ronda de pruebas jugando: el selector del mapa se movía al revés con las flechas. Causa: el grafo guarda los vecinos en orden de creación, no de pantalla. Se agrega `Partida.getAdyacentesOrdenados()` (ordenamiento por inserción sobre la lista enlazada). *"Flechas del mapa corregidas"*
- **Día 25:** Menú de **opciones** (resolución, brillo y tamaño del texto): `Opciones`, `EscenaOpciones`, `VentanaJuego` propia (la del kit tiene tamaño fijo) y `Escalado` para el mouse; se guardan en `datos/opciones.txt` con lectura tolerante. *"Opciones de pantalla"*
- **Día 26:** Combate más profundo: **energía** además del maná (`HabilidadArma`, `HabilidadPunos`), menú estilo Pokémon (Luchar, Magia, Defender, Huir), `EstadoVeneno` por composición y tres **minijuegos** al azar (`Minijuego`, `BarraPrecision`, `MinijuegoRafaga`, `MinijuegoAnillo`) que se endurecen con `Dificultad`. *"Energía, nuevas acciones y minijuegos"*
- **Día 27:** Cuatro **clases** (`Clase` enum: caballero, mago, druida, ladrón) con `EscenaClases`, cuatro sprites nuevos y guardado versión 2; tres hechizos nuevos (Tajo, Savia, Daga) y cuatro enemigos nuevos (Goblin, Araña, Espectro, Golem) con tres eventos más. *"Clases y más enemigos"*
- **Día 28:** `PruebaJuego` pasa a 382 comprobaciones; el robot juega las 4 clases y con él se rebalancean armas y costos (el Caballero ganaba casi siempre y el Ladrón nunca). Se regenera la documentación. *"Pruebas y balance por clase"*
- **Día 29:** Se corrige la **resolución**: las ventanas fijas de 1280x800 y 1536x960 no entraban en muchas pantallas y el mouse se desfasaba. Ahora hay tres modos (**Normal**, **Grande** y **Pantalla completa**); la ventana nunca es más grande que la pantalla, el juego se agranda con el mismo factor en ancho y alto (nunca se estira; sobran franjas negras) y `Escalado` usa el tamaño real del panel. Se prueba con una ventana real en una pantalla virtual. `PruebaJuego` pasa a 395 comprobaciones. *"Pantalla completa sin estirar"*
- **Día 30:** Dos fallas del mouse en Opciones. (1) Las flechas ◀ ▶ iban alineadas a la derecha y el click se decidía por la mitad de la pantalla: con valores cortos (Brillo, Texto) la ◀ caía del lado "subir". Ahora las flechas están en columnas fijas y el click se decide respecto del centro del valor. (2) Al cambiar el modo la ventana se cierra y se reabre y se perdía el aviso de "botón soltado": la `Entrada` del kit quedaba creyendo que el botón seguía apretado y no registraba más clicks. Se limpia al reabrir y con el primer `mouseMoved`. La pantalla completa pasa a ser una ventana sin bordes (se deja el modo exclusivo de Java). `PruebaJuego`: 403 comprobaciones. *"Mouse en opciones corregido"*

> **Importante:** hacé los commits reales siguiendo este orden. El profesor puede ver `git log` con las fechas. Si algo te pasó distinto (un error que te costó un día, un cambio de diseño), corregí esta lista para que cuente lo que realmente hiciste.

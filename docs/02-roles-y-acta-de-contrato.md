# Roles y acta de contrato (Solo Dev)

El enunciado reparte el trabajo en cuatro módulos para equipos de 3 o 4 personas. Al trabajar solo, William es dueño de los cuatro.

| Rol | Paquete | Qué programa | Responsable |
|---|---|---|---|
| A. Entidades | `juego.entidades` | `Jugador`; `Habilidad` (abstracta) y `HabilidadAtaque`, `HabilidadCuracion`, `HabilidadGolpePotente`, `HabilidadEscape`; `CatalogoHabilidades`; `Enemigo` (abstracta) y `Esqueleto`, `Lobo`, `Brujo`, `ReyOscuro`; `FabricaEnemigos`; `Calidad` | William |
| B. Estructuras | `estructuras`, `utilidades` | `ListaEnlazada`, `PilaEnlazada`, `ColaEnlazada`, `Grafo` (hasta pasar el verificador) y el `Tokenizador` | William |
| C. Reglas | `juego.reglas`, `juego.archivos` | `Combate`, `BarraPrecision`, `MapaBosque`, `NodoMapa`, `TipoNodo`, `Partida`, `Registro`, `EventoNarrativo`, `OpcionEvento`, `EfectoEvento`, `GestorArchivos` | William |
| D. Pantalla | `juego.pantallas`, `juego.graficos`, `juego.recursos` | `EscenaMenu`, `EscenaMapa`, `EscenaEvento`, `EscenaCombate`, `EscenaFin`, `Hud`, `Adornos`; `Paleta`, `Sprite`, `CatalogoSprites`, `GestorSprites`, `TextoPixel`, `DibujoMedieval`; `Recursos` | William |

Además, en cada rol se cubren las tareas de testing (`PruebaJuego`) y documentación.

## Acta de contrato

Una fila por cada clase que un módulo le pide a otro, con su constructor y los métodos que usan los demás.

| Clase | Módulo | Constructor | Métodos que usan los demás |
|---|---|---|---|
| `ListaEnlazada<T>` | B | `ListaEnlazada()` | `agregar`, `insertar`, `obtener`, `indiceDe`, `eliminar`, `tamanio`, `estaVacia` |
| `PilaEnlazada<T>` | B | `PilaEnlazada()` | `apilar`, `desapilar`, `tope`, `estaVacia`, `tamanio` |
| `ColaEnlazada<T>` | B | `ColaEnlazada()` | `encolar`, `desencolar`, `frente`, `estaVacia`, `tamanio` |
| `Grafo<T>` | B | `Grafo()` | `agregarVertice`, `agregarArista`, `obtener`, `existeArista`, `idsAdyacentes`, `adyacentes`, `gradoSalida`, `recorridoEnAnchura`, `recorridoEnProfundidad`, `hayCamino` |
| `Tokenizador` | B | `Tokenizador(texto, separador)` | `hayMas`, `siguiente`, `contar` (estático) |
| `Jugador` | A | `Jugador()` y `Jugador(fuerza, destreza, inteligencia, vida, mana, oro)` | `getVida`, `getMana`, `getDanioFisico`, `getPotenciaMagica`, `recibirDanio`, `curar`, `mejorarAtributo`, `aprenderHabilidad`, `getHabilidad`, `cantidadHabilidades` |
| `Habilidad` y sus 4 hijas | A | `HabilidadAtaque()`, etc. | `aplicar(jugador, enemigo, calidad)`, `usaBarra`, `getDificultadBarra`, `esEscape`, `getCostoMana`, `getNombre` |
| `Enemigo` y sus 4 hijos | A | `Esqueleto()`, `Lobo()`, `Brujo()`, `ReyOscuro()` | `atacar(jugador)`, `recibirDanio`, `reforzar(piso)`, `getTurnosPorRonda`, `estaMuerto`, `getSprite` |
| `FabricaEnemigos` | A | (utilidad, sin instancias) | `crearAleatorio(piso, azar)`, `crearPorNombre`, `crearJefe` |
| `BarraPrecision` | C | `BarraPrecision(jugador, dificultad)` | `actualizar(dt)`, `detener()`, `calidadEn`, `getPosicion`, `getAnchoCritico`, `getAnchoNormal` |
| `Combate` | C | `Combate(jugador, enemigo, esJefe)` | `elegirHabilidad`, `actualizar`, `detenerBarra`, `continuar`, `getEstado`, `getMensaje`, `getOrdenDeTurnos` |
| `MapaBosque` | C | `MapaBosque(semilla)` | `getGrafo`, `getNodo`, `getIdInicio`, `getIdJefe` |
| `Partida` | C | `Partida(semilla, jugador, ruta, diario, derrotados, eventos)` y `Partida.nueva(semilla, eventos)` | `getAdyacentes`, `puedeMoverA`, `moverA`, `registrar`, `getUltimosRegistros`, `getRuta`, `crearEnemigoDeNodo`, `crearEventoDeNodo`, `crearEventoCofre`, `sumarDerrotado` |
| `EventoNarrativo`, `OpcionEvento`, `EfectoEvento` | C | `(titulo, relato, opciones)`, `(texto, resultado, efectos)`, `(tipo, valor, texto)` | `getOpcion`, `puedeElegir`, `aplicar`, `getEmboscada` |
| `GestorArchivos` | C | `GestorArchivos(carpeta)` | `guardar`, `cargar`, `existeGuardado`, `borrarGuardado`, `cargarEventos`, `interpretarEventos` |
| `Recursos` | D | `Recursos(carpetaDatos, carpetaAssets)` | `getArchivos`, `getSprites`, `getEventos`, `getFondoBosque`, `getFondoMapa` |
| `GestorSprites` | D | `GestorSprites(carpetaAssets)` | `obtener(nombre, escala)`, `dibujar`, `tieneArchivo` |
| Escenas | D | `EscenaMenu(recursos)`, `EscenaMapa(recursos, partida, autoguardar)`, `EscenaEvento(recursos, partida, evento, esCofre)`, `EscenaCombate(recursos, partida, enemigo, esJefe)`, `EscenaFin(recursos, partida, victoria)` | `actualizar(dt)`, `dibujar(g)` |

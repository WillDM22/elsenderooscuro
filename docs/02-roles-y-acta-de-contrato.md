# Roles y acta de contrato

El trabajo se reparte en cuatro módulos. El equipo los comparte y todos responden por el proyecto completo.

| Rol | Paquete | Qué programa | Responsable |
|---|---|---|---|
| A. Entidades | `juego.entidades` | `Jugador`, `Clase` (enum), `EstadoVeneno`; `Habilidad` (abstracta) y `HabilidadArma`, `HabilidadPunos`, `HabilidadCuracion`, `HabilidadGolpePotente`, `HabilidadEscape`, `HabilidadFuria`, `HabilidadSavia`, `HabilidadVeneno`; `CatalogoHabilidades`; `Enemigo` (abstracta) y `Esqueleto`, `Lobo`, `Goblin`, `Arana`, `Brujo`, `Espectro`, `Golem`, `ReyOscuro`; `FabricaEnemigos`; `Calidad` | Equipo |
| B. Estructuras | `estructuras`, `utilidades` | `ListaEnlazada`, `PilaEnlazada`, `ColaEnlazada`, `Grafo` (hasta pasar el verificador) y el `Tokenizador` | Equipo |
| C. Reglas | `juego.reglas`, `juego.archivos` | `Combate`, `Minijuego`, `BarraPrecision`, `MinijuegoRafaga`, `MinijuegoAnillo`, `Dificultad`, `MapaBosque`, `NodoMapa`, `TipoNodo`, `Partida`, `Registro`, `EventoNarrativo`, `OpcionEvento`, `EfectoEvento`, `GestorArchivos` | Equipo |
| D. Pantalla | `juego.pantallas`, `juego.graficos`, `juego.recursos`, `juego.opciones`, `juego.ventana` | `EscenaMenu`, `EscenaClases`, `EscenaOpciones`, `EscenaMapa`, `EscenaEvento`, `EscenaCombate`, `EscenaFin`, `Hud`, `Adornos`; `Paleta`, `Sprite`, `CatalogoSprites`, `GestorSprites`, `TextoPixel`, `DibujoMedieval`, `ImagenesUi`, `Fuentes`, `Personajes`; `Recursos`; `Opciones`; `VentanaJuego`, `Escalado` | Equipo |

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
| `Clase` | A | (enum: `CABALLERO`, `MAGO`, `DRUIDA`, `LADRON`) | `porOrden`, `porCodigo`, `crearArma`, `getNombre`, `getFuerza`, `getDestreza`, `getInteligencia` |
| `Jugador` | A | `Jugador(clase)`, `Jugador()` y `Jugador(clase, fuerza, destreza, inteligencia, vida, mana, oro)` | `getClase`, `getVida`, `getMana`, `getEnergia`, `getArma`, `getPunos`, `getDanioFisico`, `getPotenciaMagica`, `recibirDanio`, `curar`, `gastarEnergia`, `recuperarEnergia`, `setGuardia`, `envenenar`, `prepararCombate`, `mejorarAtributo`, `aprenderHabilidad`, `getHabilidad`, `cantidadHabilidades` |
| `Habilidad` y sus hijas | A | `HabilidadCuracion()`, `HabilidadVeneno()`, etc. (el arma sale de `Clase.crearArma()`) | `aplicar(jugador, enemigo, calidad)`, `usaBarra`, `getDificultadBarra`, `esEscape`, `getCostoMana`, `getCostoEnergia`, `getNombre` |
| `Enemigo` y sus 8 hijos | A | `Esqueleto()`, `Lobo()`, `Goblin()`, `Arana()`, `Brujo()`, `Espectro()`, `Golem()`, `ReyOscuro()` | `atacar(jugador)`, `recibirDanio`, `envenenar`, `aplicarVeneno`, `reforzar(piso)`, `getTurnosPorRonda`, `estaMuerto`, `getSprite` |
| `FabricaEnemigos` | A | (utilidad, sin instancias) | `tiposDisponibles(piso)`, `crearAleatorio(piso, azar)`, `crearPorNombre`, `crearJefe` |
| `Minijuego` y sus 3 hijos (`BarraPrecision`, `MinijuegoRafaga`, `MinijuegoAnillo`) | C | `BarraPrecision(jugador, dificultad, nivel)`, `MinijuegoRafaga(jugador, dificultad, nivel)`, `MinijuegoAnillo(jugador, dificultad, nivel)` | `actualizar(dt)`, `pulsar()`, `getTipo`, `estaTerminado`, `getResultado` (y los getters propios de cada dibujo) |
| `Dificultad` | C | (utilidad, sin instancias) | `limitarNivel`, `factorVelocidad(nivel)`, `factorZona(nivel)` |
| `Combate` | C | `Combate(jugador, enemigo, esJefe)` y `Combate(jugador, enemigo, esJefe, nivel, azar)` | `elegirHabilidad`, `elegirArma`, `elegirPunos`, `defender`, `huir`, `actualizar`, `pulsar`, `continuar`, `getEstado`, `getMinijuego`, `getMensaje`, `getOrdenDeTurnos` |
| `MapaBosque` | C | `MapaBosque(semilla)` | `getGrafo`, `getNodo`, `getIdInicio`, `getIdJefe` |
| `Partida` | C | `Partida(semilla, jugador, ruta, diario, derrotados, eventos)` y `Partida.nueva(semilla, eventos, clase)` | `getAdyacentes`, `getAdyacentesOrdenados`, `puedeMoverA`, `moverA`, `registrar`, `getUltimosRegistros`, `getRuta`, `crearEnemigoDeNodo`, `crearEventoDeNodo`, `crearEventoCofre`, `sumarDerrotado` |
| `EventoNarrativo`, `OpcionEvento`, `EfectoEvento` | C | `(titulo, relato, opciones)`, `(texto, resultado, efectos)`, `(tipo, valor, texto)` | `getOpcion`, `puedeElegir`, `aplicar`, `getEmboscada` |
| `GestorArchivos` | C | `GestorArchivos(carpeta)` | `guardar`, `cargar`, `existeGuardado`, `borrarGuardado`, `guardarOpciones`, `cargarOpciones`, `cargarEventos`, `interpretarEventos` |
| `Opciones` | D | `Opciones()` | `getResolucion`, `esPantallaCompleta`, `getFactorVentana`, `getNombreResolucion`, `getBrillo`, `getTexto`, `cambiarResolucion`, `cambiarBrillo`, `cambiarTexto`, `getEscalaTexto` |
| `Recursos` | D | `Recursos(carpetaDatos, carpetaAssets)` | `getArchivos`, `getSprites`, `getEventos`, `getOpciones`, `aplicarOpciones`, `getFondoBosque`, `getFondoMapa`, `getUi` |
| `ImagenesUi` | D | `ImagenesUi()` | `existe`, `obtener`, `dibujar`, `dibujarCentrado`, `dibujarEstirada`, `dibujarVisibleEstirada`, `dibujarSinFilas`, `dibujarPersonaje`, `recortada`, `anchoVisible`, `altoVisible` |
| `Fuentes` | D | (estática) | `cuerpo(tamanio)`, `titulo(tamanio)`, `respaldo(tamanio)` |
| `Personajes` | D | (estática) | `ruta`, `alturaDePies`, `dibujar(ui, g, sprite, pose, cx, ypies)` |
| `GestorSprites` | D | `GestorSprites(carpetaAssets)` | `obtener(nombre, escala)`, `dibujar`, `tieneArchivo` |
| Escenas | D | `EscenaMenu(recursos)`, `EscenaClases(recursos)`, `EscenaOpciones(recursos, anterior)`, `EscenaMapa(recursos, partida, autoguardar)`, `EscenaEvento(recursos, partida, evento, esCofre)`, `EscenaCombate(recursos, partida, enemigo, esJefe)`, `EscenaFin(recursos, partida, victoria)` | `actualizar(dt)`, `dibujar(g)` |

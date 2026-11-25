# Diseño y diagrama de clases

## 1. Idea general

El juego tiene **tres capas** que no se mezclan:

1. **Estructuras** (`estructuras`, `utilidades`): la librería propia. No sabe nada del juego.
2. **Lógica** (`juego.entidades`, `juego.reglas`, `juego.archivos`): las reglas. No dibuja nada ni usa Swing, por eso se puede probar sin ventana (`PruebaJuego`).
3. **Presentación** (`juego.graficos`, `juego.pantallas`, `juego.recursos`, `juego.ventana`): lee el teclado, le pide cosas a la lógica y dibuja lo que ella dice.

```mermaid
flowchart TD
    subgraph kit["Kit del profesor"]
        motor["motor / contratos / ui"]
    end
    subgraph lib["Librería propia"]
        estructuras["estructuras<br/>ListaEnlazada, PilaEnlazada,<br/>ColaEnlazada, Grafo"]
        utilidades["utilidades<br/>Tokenizador"]
    end
    entidades["juego.entidades<br/>Jugador, Clase, Habilidad, Enemigo"]
    reglas["juego.reglas<br/>Combate, Minijuego (3),<br/>MapaBosque, Partida, eventos"]
    opciones["juego.opciones<br/>Opciones"]
    ventana["juego.ventana<br/>VentanaJuego, Escalado"]
    archivos["juego.archivos<br/>GestorArchivos"]
    graficos["juego.graficos<br/>Sprites, DibujoMedieval"]
    recursos["juego.recursos<br/>Recursos"]
    pantallas["juego.pantallas<br/>Escenas"]
    main["juego.Main"]

    entidades --> estructuras
    reglas --> entidades
    reglas --> estructuras
    archivos --> reglas
    archivos --> entidades
    archivos --> utilidades
    archivos --> opciones
    ventana --> opciones
    ventana --> motor
    recursos --> opciones
    pantallas --> opciones
    graficos --> reglas
    graficos --> utilidades
    recursos --> archivos
    recursos --> graficos
    pantallas --> recursos
    pantallas --> reglas
    pantallas --> graficos
    pantallas --> motor
    main --> pantallas
    main --> ventana
```

Las flechas van siempre "hacia abajo": **no hay dependencias circulares** entre paquetes (se revisó con `grep` de los `import`).
Por eso `Habilidad` no usa los minijuegos: el resultado de un minijuego viaja como un número (`Calidad.FALLO/NORMAL/CRITICO`) definido en `entidades`.

## 2. Qué estructura se usa para qué (requisito de la cátedra)

| Estructura | Clase | Dónde se usa | Por qué esa y no otra |
|---|---|---|---|
| **Grafo dirigido** (listas de adyacencia) | `Grafo<T>` | `MapaBosque`: cada lugar es un vértice y cada camino una arista | Los caminos tienen forma de red y solo se avanza en un sentido |
| **Lista enlazada** | `ListaEnlazada<T>` | Repertorio de hechizos del `Jugador` (máx. 4), opciones de cada `EventoNarrativo`, efectos de cada opción, lista de adyacencia de cada vértice, eventos cargados | Pocos elementos, se recorren siempre de principio a fin, se agregan al final |
| **Cola enlazada** (FIFO) | `ColaEnlazada<T>` | `Combate`: los turnos (Jugador / Enemigo); `Grafo`: recorrido en anchura | El primero en llegar es el primero en actuar |
| **Pila enlazada** (LIFO) | `PilaEnlazada<T>` | `Partida`: la ruta de nodos visitados y el diario de decisiones; `Grafo`: recorrido en profundidad | Lo más reciente es lo que importa (último lugar, últimas decisiones) |

## 3. Diagrama de clases (lógica)

```mermaid
classDiagram
    class Lista~T~ { <<interface>> }
    class Pila~T~ { <<interface>> }
    class Cola~T~ { <<interface>> }
    Lista <|.. ListaEnlazada
    Pila <|.. PilaEnlazada
    Cola <|.. ColaEnlazada

    class Grafo~T~ {
        -ListaEnlazada vertices
        +agregarVertice(dato) int
        +agregarArista(origen, destino) boolean
        +idsAdyacentes(id) ListaEnlazada
        +recorridoEnAnchura(inicio)
        +recorridoEnProfundidad(inicio)
        +hayCamino(origen, destino) boolean
    }
    Grafo ..> ListaEnlazada
    Grafo ..> ColaEnlazada
    Grafo ..> PilaEnlazada

    class Clase { <<enum>>
        CABALLERO
        MAGO
        DRUIDA
        LADRON
        +crearArma() HabilidadArma
    }
    class Jugador {
        -fuerza, destreza, inteligencia
        -vida, mana, energia, oro
        -boolean guardia
        -EstadoVeneno veneno
        -ListaEnlazada~Habilidad~ habilidades
        +recibirDanio(n) int
        +mejorarAtributo(cual, n)
        +aprenderHabilidad(h) boolean
        +prepararCombate()
    }
    Jugador --> Clase
    class Habilidad { <<abstract>>
        -costoMana, costoEnergia
        +aplicar(jugador, enemigo, calidad)* String
        +usaBarra() boolean
        +getDificultadBarra() double
        +esEscape() boolean
    }
    Habilidad <|-- HabilidadArma
    Habilidad <|-- HabilidadPunos
    Habilidad <|-- HabilidadCuracion
    Habilidad <|-- HabilidadGolpePotente
    Habilidad <|-- HabilidadEscape
    Habilidad <|-- HabilidadFuria
    Habilidad <|-- HabilidadSavia
    Habilidad <|-- HabilidadVeneno
    Jugador o-- Habilidad
    Jugador *-- EstadoVeneno

    class Enemigo { <<abstract>>
        #accionar(jugador, n)* String
        +atacar(jugador) String
        +reforzar(piso)
        +getTurnosPorRonda() int
    }
    Enemigo <|-- Esqueleto
    Enemigo <|-- Lobo
    Enemigo <|-- Goblin
    Enemigo <|-- Arana
    Enemigo <|-- Brujo
    Enemigo <|-- Espectro
    Enemigo <|-- Golem
    Enemigo <|-- ReyOscuro
    Enemigo *-- EstadoVeneno

    class Minijuego { <<abstract>>
        +actualizar(dt)*
        +pulsar()*
        +getTipo()* int
        +estaTerminado() boolean
        +getResultado() int
    }
    Minijuego <|-- BarraPrecision
    Minijuego <|-- MinijuegoRafaga
    Minijuego <|-- MinijuegoAnillo

    class Combate {
        -ColaEnlazada turnos
        -int estado
        -Minijuego minijuego
        +elegirHabilidad(i) boolean
        +elegirArma() boolean
        +elegirPunos() boolean
        +defender() boolean
        +huir() boolean
        +pulsar()
        +continuar()
        +getOrdenDeTurnos() ColaEnlazada
    }
    Combate --> Jugador
    Combate --> Enemigo
    Combate --> Minijuego
    Combate ..> ColaEnlazada

    class MapaBosque {
        -Grafo~NodoMapa~ grafo
        +MapaBosque(semilla)
    }
    MapaBosque --> Grafo
    MapaBosque --> NodoMapa
    NodoMapa --> TipoNodo

    class Partida {
        -PilaEnlazada~Integer~ ruta
        -PilaEnlazada~Registro~ diario
        +moverA(id)
        +registrar(id, texto)
        +getAdyacentesOrdenados() ListaEnlazada
        +getUltimosRegistros(n)
    }
    Partida --> MapaBosque
    Partida --> Jugador
    Partida o-- Registro

    class EventoNarrativo
    class OpcionEvento
    class EfectoEvento
    EventoNarrativo o-- "2..3" OpcionEvento
    OpcionEvento o-- EfectoEvento

    class GestorArchivos {
        +guardar(partida) boolean
        +cargar(eventos) Partida
        +cargarEventos() ListaEnlazada
        +guardarOpciones(opciones) boolean
        +cargarOpciones() Opciones
    }
    GestorArchivos ..> Partida
    GestorArchivos ..> EventoNarrativo
```

## 4. Cómo se conectan las piezas (paso a paso)

### 4.1 Las estadísticas y el combate

Todo parte de tres números del `Jugador`, que dependen de su **clase** (Caballero 5/3/1, Mago 1/3/5, Druida 3/3/3, Ladrón 2/5/2) y crecen al derrotar enemigos: **Fuerza, Destreza e Inteligencia**. Las demás cifras son fórmulas simples de esos tres (ejemplos con 3/3/3):

| Fórmula (en `Jugador`) | Con 3/3/3 | Quién la usa |
|---|---|---|
| Vida máxima = 20 + 5 × Fuerza | 35 | Hud, `curar`, `mejorarAtributo` |
| Daño físico = 2 + 2 × Fuerza | 8 | `HabilidadArma`, `HabilidadFuria`, `HabilidadVeneno` |
| Energía máxima = 6 + 2 × Destreza | 12 | Cuántos golpes de arma o puños puede pagar el héroe |
| Defensa = Destreza ÷ 2 (entero) | 1 | `recibirDanio` (el daño baja en 1, mínimo 1) |
| Zona crítica extra = 4 × Destreza (píxeles) | 12 | `BarraPrecision` y `MinijuegoAnillo` (zona crítica más ancha) |
| Factor de velocidad = 1 − 0,03 × Destreza (mín. 0,5) | 0,91 | `BarraPrecision` y `MinijuegoAnillo` (más lentos) |
| Maná máximo = 6 + 3 × Inteligencia | 15 | Hud, `cambiarMana` |
| Potencia mágica = 3 + 3 × Inteligencia | 12 | `HabilidadGolpePotente`, `HabilidadSavia` |
| Curación = 6 + 3 × Inteligencia | 15 | `HabilidadCuracion` |

Dos recursos distintos se gastan en combate: el **maná** (hechizos) y la **energía** (arma y puños). La energía se llena al empezar cada combate, se recupera +3 por ronda y +4 al **Defender**.

El recorrido de **un turno del jugador** es:

1. `EscenaCombate` muestra un menú estilo Pokémon (**Luchar**, **Magia**, **Defender**, **Huir**). Según lo elegido llama a `combate.elegirArma()`, `elegirPunos()`, `elegirHabilidad(i)`, `defender()` o `huir()`.
2. `Combate.iniciarAccion` mira que haya maná y energía suficientes y que no sea un escape contra el jefe. Gasta los costos.
3. Si la habilidad `usaBarra()`, `crearMinijuego` elige **al azar** una barra (50 %), una ráfaga (25 %) o un anillo (25 %), pasándole la dificultad de la habilidad y el **nivel** (enemigos derrotados, tope 12: más nivel = más rápido y zonas más chicas). Ahí entra también la **Destreza**.
4. La escena llama `combate.actualizar(dt)` en cada cuadro (el minijuego avanza) y, cuando se aprieta ESPACIO, `combate.pulsar()`.
5. El minijuego devuelve una `Calidad` (fallo, normal o crítico) y `Combate` llama `habilidad.aplicar(jugador, enemigo, calidad)`. Ahí entran **Fuerza** e **Inteligencia**.
6. `Combate` saca de la **cola de turnos** al jugador (`desencolar`), le cobra el **veneno** si lo tiene y mira el frente: si es el enemigo, queda en estado `TURNO_ENEMIGO`.
7. El enemigo ataca (`enemigo.atacar(jugador)`), el jugador recibe el daño reducido por la **Defensa** (y a la mitad si se defendió) y se vuelve a mirar la cola. Si la cola quedó vacía, se arma una ronda nueva.

### 4.2 El mapa y el grafo

1. `MapaBosque(semilla)` crea un `Grafo<NodoMapa>`. Cada `NodoMapa` es un vértice con tipo (calavera, `?`, cofre, jefe), piso y posición en pantalla.
2. Los pisos 1 a 5 tienen 2 o 3 lugares. Para cada par de pisos consecutivos se agregan aristas **dirigidas** de izquierda a derecha, y se asegura que ningún lugar quede sin salida ni sin entrada.
3. `Partida` guarda en una **pila** (`ruta`) los ids visitados; el tope es el lugar actual.
4. `EscenaMapa` pide `partida.getAdyacentesOrdenados()` (que llama a `grafo.idsAdyacentes(actual)` y ordena los vecinos de arriba hacia abajo) y esos son los únicos destinos elegibles. Dibuja los demás lugares, pero sin anillo blanco.
5. Al elegir, `partida.moverA(id)` verifica con `grafo.existeArista(actual, id)`, apila el id y anota en la pila `diario`.
6. Según el `TipoNodo` del destino, la escena abre combate, evento o cofre.

### 4.3 La persistencia en archivos

- **Guardar** (`GestorArchivos.guardar`): con un `PrintWriter` escribe una línea `CLAVE=valor` por dato: semilla, clase, atributos, vida, maná, oro, nodo actual, hechizos, ruta y una línea `REGISTRO=` por cada entrada del diario.
- **No se guarda el grafo**: el mapa depende solo de la semilla (`new Random(semilla)`), así que al cargar se vuelve a generar idéntico.
- **Cargar** (`GestorArchivos.cargar`): con un `BufferedReader` lee todas las líneas a una `ListaEnlazada<String>`, busca cada clave, reconstruye el `Jugador`, las dos pilas y la `Partida`. Se valida que la ruta sea un camino real del grafo; si **algo** falla (falta una clave, un número mal escrito, una clase o un hechizo inventado, más de 4 hechizos) devuelve `null` y el menú avisa "archivo dañado" sin romper el juego.
- **Opciones** (`guardarOpciones` / `cargarOpciones`): `datos/opciones.txt` guarda resolución, brillo y tamaño de texto. Al cargar, cada línea mala vuelve a su valor por defecto sin afectar a las demás.
- **Eventos** (`cargarEventos`): lee `datos/eventos.txt`. Cada evento descartado por estar mal escrito no afecta a los demás; si no queda ninguno (o falta el archivo) se usan dos eventos de respaldo.

Formato de `datos/guardado.txt`:

```
VERSION=2
SEMILLA=2024
CLASE=CABALLERO
FUERZA=5
DESTREZA=3
INTELIGENCIA=3
VIDA=26
MANA=11
ORO=37
DERROTADOS=2
NODO_ACTUAL=6
HABILIDADES=FURIA,CURACION
RUTA=0,2,6
REGISTRO=0|Entras al Sendero Oscuro.
REGISTRO=2|Piso 1: llegas a Combate.
REGISTRO=2|Derrotaste a Lobo.
REGISTRO=6|Piso 2: llegas a Evento.
```

Formato de `datos/opciones.txt`:

```
RESOLUCION=0
BRILLO=2
TEXTO=1
```

Formato de `datos/eventos.txt`:

```
EVENTO|título|relato
OPCION|texto del botón|EFECTO:valor;EFECTO:valor|texto del resultado
```

## 5. Las reglas del enunciado, una por una

| Requisito | Dónde se cumple |
|---|---|
| Grafo dirigido con listas de adyacencia propias | `estructuras/Grafo.java` (cada `Vertice` tiene una `ListaEnlazada` de adyacentes) |
| Lista enlazada propia (hechizos y opciones de evento) | `Jugador.habilidades`, `EventoNarrativo.opciones`; implementación en `ListaEnlazada.java` |
| Cola enlazada propia para los turnos | `Combate.turnos` |
| Pila enlazada propia para el historial | `Partida.ruta` y `Partida.diario` |
| Persistencia con `BufferedReader` y `PrintWriter` | `GestorArchivos` (`guardado.txt`, `opciones.txt` y `eventos.txt`) |
| Sin `ArrayList`, `Stack`, `Queue` ni arreglos propios | Auditado con `grep`: de `java.util` solo se usa `Random` |
| Sin lambdas ni streams | Auditado con `grep` (las únicas lambdas están en el kit: `Ventana`, `VerificadorEstructuras`) |
| Al menos una librería propia | Paquete `estructuras` (4 estructuras genéricas, con interfaces) y `utilidades.Tokenizador` |
| Pixel-art medieval sin archivos externos | `juego.graficos`: sprites hechos con texto y Graphics2D |
| Cargador de sprites con `assets/` y respaldo | `GestorSprites` |
| Personaje humano con Fuerza, Destreza, Inteligencia y hasta 4 hechizos (con 4 clases a elegir) | `Jugador`, `Clase`, `Habilidad*` |
| Mapa ramificado con Combate, Cofre, `?` y Jefe | `MapaBosque`, `TipoNodo`, `EscenaMapa` |
| Minijuegos de precisión estilo Undertale (barra, ráfaga, anillo) con efecto de la Destreza y dificultad creciente | `Minijuego`, `BarraPrecision`, `MinijuegoRafaga`, `MinijuegoAnillo`, `Dificultad`, `EscenaCombate.dibujarMinijuego` |
| Acciones de combate estilo Pokémon (Luchar, Magia, Defender, Huir) y energía además del maná | `Combate`, `Jugador`, `HabilidadArma`, `HabilidadPunos` |
| Variedad de enemigos | `Esqueleto`, `Lobo`, `Goblin`, `Arana`, `Brujo`, `Espectro`, `Golem`, `ReyOscuro`, `FabricaEnemigos` |
| Opciones: resolución, brillo y tamaño del texto | `Opciones`, `EscenaOpciones`, `VentanaJuego`, `Escalado` |
| Eventos con 2 o 3 opciones que modifican stats o dan habilidades | `datos/eventos.txt`, `EventoNarrativo`, `EfectoEvento` |

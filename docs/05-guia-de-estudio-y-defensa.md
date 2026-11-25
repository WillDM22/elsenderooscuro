# Guía de estudio y defensa oral

Esta guía te prepara para exponer **El Sendero Oscuro** y responder preguntas del profesor. Está organizada así:

1. El guion de la exposición (7 a 8 minutos)
2. Qué demostrar en vivo
3. Preguntas sobre cada estructura de datos
4. Preguntas sobre grafos, archivos y POO
5. Preguntas trampa
6. Lo que NO tenés que decir
7. Tarjetas de repaso

---

## 1. Guion de la exposición

**Minuto 0–1: qué es.**
"Es un roguelike de decisiones inspirado en Peglin. Avanzás por un bosque ramificado, con eventos de 2 o 3 opciones, y peleás con una barra de precisión como la de Undertale. Es un solo personaje humano con tres atributos y hasta cuatro habilidades."

**Minuto 1–3: las cuatro estructuras y para qué sirve cada una.**
- *Grafo dirigido:* el mapa. Cada lugar es un vértice y cada camino una arista. Solo se puede ir hacia adelante.
- *Lista enlazada:* las habilidades del héroe (máximo 4) y las opciones de cada evento.
- *Cola:* los turnos del combate. El primero que entra es el primero que actúa.
- *Pila:* el historial. La ruta de lugares visitados y el diario de decisiones: lo último que pasó es lo primero que se ve.

"Las cuatro son propias, hechas con nodos y referencias. No uso `ArrayList`, `Stack` ni `Queue`. De `java.util` solo uso `Random`."

**Minuto 3–5: cómo se conectan las piezas.**
1. Las stats (Fuerza, Destreza, Inteligencia) alimentan fórmulas. La Destreza agranda la zona crítica de la barra y baja la velocidad del cursor.
2. El mapa se genera con una **semilla**: la misma semilla produce el mismo grafo. Por eso al guardar no guardo el grafo, solo la semilla.
3. El guardado es un archivo de texto con una línea `CLAVE=valor` por dato. Los eventos narrativos se leen de otro archivo, así que se pueden agregar sin tocar el código.

**Minuto 5–7: decisiones de diseño que puedo defender.**
- Separé la lógica (sin Swing) de las pantallas: por eso pude hacer 265 pruebas automáticas sin abrir ventana.
- Herencia donde hay comportamiento distinto (`Habilidad` y `Enemigo` son abstractas), pero solo donde hay un motivo.
- Si el archivo de guardado está dañado, el juego no se rompe: `cargar` devuelve `null`.
- Si faltan los `.png`, el juego dibuja los sprites con Graphics2D: nunca falla por una imagen.

**Minuto 7–8: demo en vivo y cierre** (ver sección 2).

---

## 2. Qué demostrar en vivo

1. **Nueva partida** → se ve el mapa. Señalá: "estos son los vértices, estas las aristas; solo puedo ir a los que tienen anillo blanco".
2. Apretá **H**: "este es el diario, es una pila; muestra primero lo último".
3. Entrá a un **combate**: elegí Ataque y mostrá la barra. "Si frenó en el centro es crítico". Mostrá arriba la **cola de turnos** (T = turno tuyo, E = enemigo).
4. Entrá a un **evento**: mostrá las 2 o 3 opciones y una opción gris (`te falta oro`).
5. Volvé al mapa, apretá **ESC**, elegí **Continuar**: "se guardó y recupera exactamente donde estaba".
6. Abrí `datos/guardado.txt` en el bloc de notas: "es texto plano, una clave por línea".
7. Si te preguntan por los eventos, abrí `datos/eventos.txt` y agregá uno en vivo (ver trampa 9).

Tené compilado el proyecto **antes** de la exposición y probá el recorrido completo una vez.

---

## 3. Preguntas sobre las estructuras de datos

### Lista enlazada

**¿Por qué una lista enlazada y no un arreglo para las habilidades?**
Porque la cantidad cambia durante el juego (empieza con 1 y llega a 4) y la cátedra pide estructuras dinámicas con nodos. Con la lista no hay que saber el tamaño de antemano ni copiar elementos al crecer. En este caso el máximo es 4, así que técnicamente un arreglo funcionaría, pero la consigna es justamente practicar la estructura dinámica.

**¿Cuánto cuesta cada operación?**
| Operación | Costo | Por qué |
|---|---|---|
| `agregar` al final | O(1) | Hay una referencia `ultimo` |
| `obtener(i)` | O(i) | Hay que caminar nodo por nodo desde `primero` |
| `insertar(i, x)` | O(i) | Hay que llegar hasta el nodo anterior |
| `eliminar(i)` | O(i) | Ídem |
| `indiceDe(x)` | O(n) | Se recorre todo en el peor caso |

**¿Qué pasa si elimino el último nodo y después agrego uno?**
Hay que actualizar `ultimo` al eliminar (lo apunto al anterior) y, si la lista queda vacía, ponerlo en `null`. Si no, `agregar` enlazaría el nodo nuevo detrás de un nodo ya borrado. Está probado en `PruebaJuego` ("agregar tras eliminar el último enlaza bien").

**¿Por qué `obtener` lanza `IndexOutOfBoundsException`?**
Porque el verificador del kit lo exige y porque es la excepción estándar de Java para "índice fuera de rango". Es una excepción no verificada: no obliga a poner `try/catch` en todos lados.

### Pila

**¿Qué es una pila y dónde la usás?**
Estructura LIFO (el último en entrar es el primero en salir). La uso en `Partida` para la **ruta** (el tope es el lugar actual) y el **diario** (el tope es lo más reciente). También en el recorrido en profundidad del grafo.

**¿Por qué una pila y no una lista para el diario?**
Porque lo que se consulta casi siempre es "lo último que pasó": el tope se lee en O(1) y agregar también es O(1), sin recorrer nada.

**Pero una pila no se puede recorrer. ¿Cómo mostrás las últimas 8 entradas?**
Saco elementos con `desapilar` y los paso a una **pila auxiliar** mientras los voy copiando a una lista; después los devuelvo a la pila original. Así miro sin destruir. Está en `Partida.getUltimosRegistros`. El costo es O(k) para k entradas.

**¿Cómo guardás una pila en un archivo?**
La paso a una pila auxiliar (que invierte el orden) y la vuelvo a pasar a la original mientras escribo: así se escribe del más antiguo al más nuevo. Al cargar, voy apilando en ese orden y el último queda en el tope. Está en `Partida.getDiarioCompleto` y `getRuta`.

### Cola

**¿Qué es una cola y dónde la usás?**
FIFO (el primero en entrar es el primero en salir). La uso en `Combate` para los **turnos**: se encolan Jugador, Enemigo (y el Lobo, dos veces) y se va sacando de a uno.

**¿Por qué una cola para los turnos y no un `if`?**
Con una cola el orden es *datos*, no código: el Lobo "actúa dos veces" con solo encolar dos veces al enemigo, sin cambiar la lógica del combate. Si mañana agrego un enemigo muy lento o un aliado, solo cambia lo que se encola.

**¿Cómo mostrás la cola en pantalla si no se puede mirar adentro?**
`getOrdenDeTurnos` desencola todos los elementos, y a medida que los saca los encola en una cola copia y en la original. Cuando termina, la original quedó igual y la copia es la que se recorre. Costo O(n).

**¿Qué pasa cuando la cola se vacía?**
`prepararSiguienteTurno` arma una ronda nueva (`rondas++` y vuelve a encolar). Siempre hay alguien al frente.

### Grafo

**¿Qué es un grafo y por qué el mapa lo es?**
Un conjunto de vértices unidos por aristas. En el mapa cada lugar es un vértice y cada camino una arista. Un árbol no alcanza porque dos lugares de un piso pueden llevar al mismo lugar del piso siguiente (hay caminos que se reúnen).

**¿Por qué dirigido?**
Porque el jugador solo avanza: de un lugar del piso *n* se va a uno del piso *n+1*, nunca para atrás. La arista A→B no implica B→A. Eso también hace que el grafo no tenga ciclos.

**¿Por qué listas de adyacencia y no matriz?**
Cada lugar tiene solo 1 a 3 caminos. Una matriz de 15×15 guardaría 225 celdas casi todas vacías y no se puede hacer sin arreglos (que no están permitidos). La lista guarda solo las aristas que existen: ocupa O(V + A).

| Operación | Costo con listas de adyacencia |
|---|---|
| `agregarVertice` | O(1) |
| `existeArista(a, b)` | O(grado de a) |
| `idsAdyacentes(a)` | O(grado de a) |
| Recorrido (anchura o profundidad) | O(V + A) |

**¿Cuál es la diferencia entre recorrido en anchura y en profundidad?**
En **anchura** se visita por niveles y se usa una **cola**. En **profundidad** se avanza lo más posible por un camino antes de volver, y se usa una **pila**. Son el mismo algoritmo cambiando la estructura. En el juego uso la anchura en `hayCamino` (¿se puede llegar al jefe?) y las pruebas verifican con ella que todos los nodos son alcanzables.

**¿Qué garantiza que el jugador nunca queda atrapado?**
`MapaBosque.conectarPisos`: tras unir cada lugar con el "proporcional" del piso siguiente, revisa los lugares sin entrada y les agrega una. Así ningún lugar queda sin salida (salvo el jefe) ni sin entrada. Lo verifiqué en `PruebaJuego` con 40 semillas: desde el inicio se llega a todos los nodos y al jefe.

**¿Por qué `agregarArista` devuelve `boolean`?**
Para avisar si realmente agregó algo: devuelve `false` si es un lazo (un vértice conectado consigo mismo) o una arista repetida. El generador puede intentar agregar la misma arista dos veces sin ensuciar el grafo.

---

## 4. Preguntas sobre archivos, POO y diseño

**¿Cómo guardás la partida?**
Con un `PrintWriter` sobre un `OutputStreamWriter` en UTF-8, escribo una línea `CLAVE=valor` por dato. Uso `try-with-resources` para que el archivo se cierre aunque haya una excepción.

**¿Y cómo la cargás?**
Con un `BufferedReader`: leo todas las líneas a una lista, busco cada clave con `valorDe` y reconstruyo los objetos. El mapa se regenera con la semilla.

**¿Por qué no guardás el grafo?**
Porque `MapaBosque` usa `new Random(semilla)`: la misma semilla produce el mismo mapa. Guardo un número en vez de decenas de nodos y aristas. Está probado: "la misma semilla genera el mismo mapa".

**¿Qué pasa si el archivo de guardado está dañado o lo editó el usuario?**
`cargar` atrapa cualquier `RuntimeException` (número mal escrito, falta una clave, habilidad inventada) y devuelve `null`; antes valida que la ruta sea un camino real del grafo y que empiece en el inicio. El menú muestra "El archivo de guardado está dañado" y no se rompe. Hay 13 pruebas de archivos dañados.

**¿Por qué `null` y no lanzar una excepción?**
Porque que falte o esté dañado el guardado no es un error del programa: es una situación esperable. El que llama (el menú) decide qué mostrar. Una excepción obligaría a envolver todo en `try/catch` en cada pantalla.

**¿Qué pasa si no se puede escribir (disco lleno)?**
`guardar` atrapa la `IOException` y devuelve `false`; el mapa muestra "No se pudo guardar la partida".

**¿Qué es el polimorfismo en tu juego? Dame un ejemplo.**
`Combate` llama `habilidad.aplicar(...)` sin saber si es un ataque, una curación o un hechizo: cada subclase tiene su versión. Lo mismo con `enemigo.atacar(...)`: el Brujo se cura cada tercera acción y el Rey Oscuro pega doble cada cuarta; `Combate` no tiene ningún `if` por tipo de enemigo.

**¿Por qué `Habilidad` y `Enemigo` son abstractas?**
Porque no existe "una habilidad cualquiera" ni "un enemigo cualquiera": tienen que ser de un tipo concreto. La abstracta obliga a cada hija a implementar `aplicar` o `accionar`.

**¿Qué es el método plantilla en `Enemigo`?**
`atacar` es `final`: cuenta la acción y llama al método abstracto `accionar`. La estructura es siempre la misma (contar y actuar) y las hijas solo cambian el "cómo".

**¿Por qué `Jugador` es `final`?**
Porque no pensé en heredarlo (hay un único personaje) y evita que una subclase rompa las validaciones. Además su constructor llama a métodos propios y el compilador advertía de ese riesgo (`this-escape`).

**¿Para qué sirve `Calidad`?**
Para que `entidades` no dependa de `reglas`. La barra (en `reglas`) devuelve un número y las habilidades (en `entidades`) lo reciben. Si `Habilidad` importara `BarraPrecision` habría un ciclo: `reglas → entidades → reglas`.

**¿Por qué usás `enum` en `TipoNodo` y `EfectoEvento.Tipo`?**
Son listas cerradas de valores (calavera, `?`, cofre...). Un `enum` evita escribir un tipo inexistente (el compilador lo rechaza) y `Tipo.valueOf("FUEGO")` lanza `IllegalArgumentException`, que uso para descartar eventos mal escritos del archivo.

**¿Dónde validás datos y con qué excepciones?**
En los constructores y setters: `IllegalArgumentException` para valores inválidos (atributo 0, daño negativo, dificultad 0), `IllegalStateException` si se usa un objeto en un estado incorrecto (desapilar una pila vacía), `IndexOutOfBoundsException` en las listas.

**¿Cómo evitás que el combate sea "demasiado fácil o difícil"?**
Hice un "jugador robot" que juega 60 aventuras completas con una puntería regular (en `PruebaJuego`) y verifico que a veces gane y a veces pierda. Los números de enemigos y las fórmulas se ajustaron con eso.

---

## 5. Preguntas trampa

**1. "¿Por qué no usaste `ArrayList`? Es más rápido."**
La consigna pide estructuras propias con nodos y referencias para aprender cómo funcionan por dentro. Es más rápida para acceso por índice (O(1) contra O(n)), pero con listas de 1 a 4 elementos la diferencia no se nota.

**2. "¿Cuál es la complejidad de `obtener` en tu lista? ¿Tiene algún problema recorrerla con `for` y `obtener(i)`?"**
`obtener(i)` es O(i); un `for` con `obtener(i)` es O(n²) en total. Para listas chicas (habilidades, opciones, vecinos) no importa. Para listas grandes convendría un iterador o guardar el nodo actual. Lo sé y lo asumo: tiene un costo, y lo elegí a cambio de simplicidad.

**3. "¿Qué pasa si agrego una arista a un vértice que no existe?"**
`Grafo.buscar` lanza `IllegalArgumentException("no existe el vértice N")` antes de modificar nada (está probado).

**4. "Si el grafo no se guarda, ¿qué pasa si cambio el algoritmo de generación y cargo una partida vieja?"**
El mapa saldría distinto y la ruta guardada podría no ser un camino válido. Por eso hay un campo `VERSION` y la carga valida la ruta contra el grafo regenerado: si no encaja, devuelve `null`. Es una limitación conocida del diseño.

**5. "¿El mismo nodo siempre tiene el mismo enemigo? ¿Y si recargo la partida para 'tirar' de nuevo?"**
El enemigo y el evento de cada nodo salen de una semilla propia (`semilla * 1000 + idNodo`), así que son siempre los mismos: cargar la partida no sirve para repetir la tirada.

**6. "¿Por qué la pila de ruta guarda `Integer` y no el `NodoMapa`?"**
Porque guardar el id alcanza (el nodo se pide al grafo) y es lo que se escribe en el archivo. Si guardara objetos tendría que reconstruirlos al cargar.

**7. "¿Qué pasa si el jugador tiene 0 de maná y ya no tiene Ataque?"**
No puede pasar: el ataque cuesta 0 maná y siempre está en el repertorio (se carga como habilidad inicial y `cargar` rechaza un guardado que no la tenga).

**8. "¿Qué es el `this-escape`?"**
Una advertencia del compilador: llamar a un método sobrescribible desde el constructor puede ejecutar código de una subclase antes de que esté terminada. `final class Jugador` lo evita.

**9. "Agregame un evento nuevo ahora."**
Abrís `datos/eventos.txt`, agregás al final:
```
EVENTO|Un viejo pozo|Un pozo de piedra susurra tu nombre.
OPCION|Arrojar una moneda (5 oro)|ORO:-5;MANA:6|El agua brilla un instante.
OPCION|Mirar adentro|VIDA:-2;INTELIGENCIA:1|Ves algo que no debías.
```
Reiniciás el juego. No hay que recompilar porque los eventos son datos, no código.

**10. "¿Qué pasa si las opciones de un evento son 4?"**
El constructor de `EventoNarrativo` lanza `IllegalArgumentException` y el cargador descarta ese evento (los otros se cargan igual).

**11. "¿Dónde está el `Grafo` dirigido si dibujás líneas entre los dos lados?"**
Las líneas se dibujan por cada arista recorriendo cada vértice y su lista de adyacentes (`EscenaMapa.dibujarSenderos`); es dirigido aunque visualmente no haya flechas porque todas van de izquierda a derecha.

**12. "¿Qué hace el `Tokenizador` y por qué no usás `split`?"**
Parte un texto por un separador devolviendo un trozo por vez. `String.split` devuelve un **arreglo** y la consigna evita arreglos en mi código. Además es mi librería propia y se usa en el archivo de eventos, el de guardado, los sprites y el corte de líneas de texto.

---

## 6. Lo que NO tenés que decir

- "Esto me lo hizo la IA." Decí: "Armé el diseño y revisé cada clase para poder explicarla". Y que sea verdad: leé `06-explicacion-clase-por-clase.md`.
- "No sé por qué funciona." Si no sabés, decí cómo lo comprobarías: "Lo probaría con una prueba chica, como las de `PruebaJuego`".
- Inventar complejidades. Usá las tablas de esta guía.

---

## 7. Tarjetas de repaso

| Pregunta | Respuesta corta |
|---|---|
| Grafo del mapa | `Grafo<NodoMapa>`, dirigido, listas de adyacencia, 12 a 17 vértices |
| Lista | Habilidades (máx. 4), opciones (2 a 3), efectos, adyacentes |
| Cola | Turnos del combate; BFS |
| Pila | Ruta y diario; DFS |
| Semilla | Regenera el mapa; por eso se guarda solo un número |
| Archivos | `guardado.txt` (`PrintWriter`) y `eventos.txt` (`BufferedReader`) |
| Archivo dañado | `cargar` devuelve `null`, el menú avisa |
| Barra | Crítico en el centro; Destreza agranda la zona y frena el cursor |
| Estados del combate | ELECCION, APUNTANDO, RESULTADO, TURNO_ENEMIGO, VICTORIA, DERROTA, HUIDA |
| Pruebas | 265 comprobaciones + verificador del kit (36/23/22) |

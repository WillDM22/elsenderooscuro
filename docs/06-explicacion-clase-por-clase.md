# Explicación clase por clase (para principiantes)

Este documento explica **todo** el código de *El Sendero Oscuro*, en el orden en que conviene leerlo: de las piezas más simples (las estructuras de datos) a las más complejas (las pantallas). Está pensado para alguien que conoce la teoría básica de POO y excepciones pero es nuevo en Java: no se da nada por sabido.

Cómo está armado cada apartado:

1. **Para qué sirve la clase** (en una frase).
2. **El código**, en bloques chicos, cada uno seguido de su explicación.
3. **Por qué se hizo así** (lo que te pueden preguntar en la defensa).

Los bloques de código son copias exactas de los archivos del proyecto.

---

## Índice

1. Glosario mínimo de Java
2. Mapa del proyecto y orden de lectura
3. Paquete `estructuras` (la librería propia): `ListaEnlazada`, `PilaEnlazada`, `ColaEnlazada`, `Grafo`
4. Paquete `utilidades`: `Tokenizador`
5. Paquete `juego.entidades`: `Calidad`, `Habilidad` y sus hijas, `Jugador`, `Enemigo` y sus hijos, fábricas
6. Paquete `juego.reglas`: `BarraPrecision`, `Combate`, `MapaBosque`, `Partida`, eventos
7. Paquete `juego.archivos`: `GestorArchivos`
8. Paquete `juego.graficos`: sprites y dibujo medieval
9. Paquetes `juego.recursos` y `juego.pantallas`: lo que se ve y se toca
10. El motor del kit (lo mínimo que hay que saber)
11. `PruebaJuego`: cómo se prueba todo sin abrir la ventana
12. Una partida de punta a punta (recorrido completo)

---

## 1. Glosario mínimo de Java

| Palabra | Qué significa | Ejemplo del proyecto |
|---|---|---|
| `class` | Molde para crear objetos: define sus datos (atributos) y sus acciones (métodos). | `class Jugador` |
| objeto / instancia | Una cosa concreta creada con `new`. | `new Jugador()` |
| atributo | Variable que vive dentro del objeto. | `private int vida;` |
| método | Acción que sabe hacer el objeto. | `recibirDanio(5)` |
| constructor | Método especial con el nombre de la clase; se ejecuta al hacer `new` y deja el objeto listo para usar. | `public Jugador() { ... }` |
| `private` | Solo se ve desde dentro de la propia clase (encapsulamiento). | `private int vida;` |
| `public` | Se puede usar desde cualquier clase. | `public int getVida()` |
| `protected` | Lo ven la clase y sus hijas. | `protected abstract String accionar(...)` |
| `static` | Pertenece a la clase, no a un objeto. | `Calidad.CRITICO` |
| `final` (atributo) | Una vez asignado no puede cambiar. | `private final String nombre;` |
| `final` (clase) | Nadie puede heredar de ella. | `public final class Jugador` |
| `final` (método) | Las hijas no pueden sobrescribirlo. | `public final String atacar(...)` |
| `extends` | "Es un": herencia. | `class Lobo extends Enemigo` |
| `implements` | La clase cumple lo que promete una interfaz. | `class ListaEnlazada<T> implements Lista<T>` |
| `interface` | Lista de métodos sin código: un contrato. | `interface Lista<T>` |
| `abstract` | Clase que no se puede crear con `new`, o método sin código que las hijas deben escribir. | `abstract class Habilidad` |
| `@Override` | Avisa que estás reemplazando un método del padre (el compilador verifica que exista). | `@Override public String aplicar(...)` |
| genéricos `<T>` | La clase funciona con cualquier tipo, que se elige al usarla. | `ListaEnlazada<Habilidad>` es una lista de habilidades |
| `null` | "No hay ningún objeto". Usarlo con `.` produce `NullPointerException`. | `private Nodo<T> siguiente;` empieza en `null` |
| `enum` | Lista cerrada de valores con nombre. | `TipoNodo.COMBATE` |
| `package` / `import` | Carpeta lógica de clases / traer una clase de otro paquete. | `import estructuras.ListaEnlazada;` |
| excepción | Aviso de error que interrumpe el flujo. Se lanza con `throw` y se atrapa con `try/catch`. | `throw new IllegalArgumentException("...")` |
| `try-with-resources` | `try (archivo) { ... }`: cierra el archivo solo, pase lo que pase. | `GestorArchivos.guardar` |
| `Math.max(a, b)` / `Math.min(a, b)` | El mayor / el menor de dos números. | `Math.max(0, vida - real)` |
| división entera | `7 / 2` da `3` (se pierde la parte decimal) si ambos son `int`. | `destreza / 2` |
| `%` (resto) | `7 % 3` da `1`. Sirve para "cada N veces". | `numeroDeAccion % 3 == 0` |

**Dos excepciones que se usan siempre:**

- `IllegalArgumentException`: "el dato que me pasaste no tiene sentido" (un daño negativo, un atributo 0).
- `IllegalStateException`: "ahora no se puede hacer eso" (desapilar una pila vacía).

---

## 2. Mapa del proyecto y orden de lectura

```
src/
  estructuras/    ListaEnlazada, PilaEnlazada, ColaEnlazada, Grafo      <- librería propia
  utilidades/     Tokenizador                                           <- librería propia
  juego/
    entidades/    Jugador, Habilidad..., Enemigo...                     <- los "personajes"
    reglas/       Combate, BarraPrecision, MapaBosque, Partida, eventos <- las reglas del juego
    archivos/     GestorArchivos                                        <- guardar y cargar
    graficos/     Paleta, Sprite, GestorSprites, DibujoMedieval...      <- cómo se dibuja
    recursos/     Recursos                                              <- objetos compartidos
    pantallas/    EscenaMenu, EscenaMapa, EscenaEvento, EscenaCombate...<- lo que ve el usuario
  motor/ contratos/ ui/ verificador/                                    <- kit del profesor
```

**Regla de oro del diseño:** las flechas de dependencia van siempre de las pantallas hacia la lógica y de la lógica hacia las estructuras, nunca al revés. `Jugador` no sabe que existe una ventana. Por eso se puede probar sin pantalla.

Orden de lectura recomendado: sección 3 → 4 → 5 → 6 → 7 → 8 → 9. Si tenés poco tiempo antes de la defensa, leé al menos 3 (estructuras), 6 (`Combate`, `MapaBosque`, `Partida`) y 7 (archivos).

---

## 3. Paquete `estructuras` (la librería propia)

La cátedra pide que las estructuras de datos sean **propias**: hechas con nodos y referencias, sin `ArrayList`, `Stack` ni `Queue`. El kit trae tres **interfaces** (`Lista`, `Pila`, `Cola`) que definen qué operaciones debe tener cada estructura. Nosotros escribimos las **implementaciones**.

### 3.1 Las interfaces del kit

Una interfaz es un contrato: dice *qué* se puede hacer, no *cómo*. Así `Combate` puede declarar `ColaEnlazada<Integer>` hoy y mañana usar otra cola sin cambiar nada más.

```java
public interface Lista<T> {

    // Altas
    void agregar(T elemento);

    void insertar(int indice, T elemento);

    // Consultas
    T obtener(int indice);

    int indiceDe(T elemento);

    int tamanio();

    boolean estaVacia();

    // Bajas
    T eliminar(int indice);

    void vaciar();
}
```

Cada línea es una operación sin cuerpo (termina en `;`). `T` es el tipo de los elementos: `Lista<String>` es una lista de textos, `Lista<Integer>` una de números enteros. (Las colecciones genéricas no pueden guardar `int` directamente: usan su versión objeto, `Integer`. Java convierte solo entre uno y otro: se llama *autoboxing*.)

```java
public interface Pila<T> {

    // Operaciones
    void apilar(T elemento);

    T desapilar();

    T tope();

    // Consultas
    boolean estaVacia();

    int tamanio();

    void vaciar();
}
```

```java
public interface Cola<T> {

    // Operaciones
    void encolar(T elemento);

    T desencolar();

    T frente();

    // Consultas
    boolean estaVacia();

    int tamanio();

    void vaciar();
}
```

Diferencias entre las tres:

| | Se agrega por... | Se saca por... | Regla |
|---|---|---|---|
| Lista | cualquier posición | cualquier posición | Se accede por índice |
| Pila | el tope | el tope | LIFO: el último en entrar es el primero en salir |
| Cola | el final | el frente | FIFO: el primero en entrar es el primero en salir |

### 3.2 `ListaEnlazada<T>`

**Para qué sirve:** guardar una secuencia de elementos que crece y decrece. Se usa en todo el proyecto: habilidades del héroe, opciones de cada evento, efectos de cada opción, vecinos de cada vértice del grafo, eventos cargados, líneas de un archivo.

**La idea:** cada elemento vive dentro de un **nodo**. El nodo tiene el dato y una referencia al nodo siguiente. La lista solo recuerda dónde está el primero y el último.

```
primero                                         ultimo
   |                                               |
   v                                               v
[dato A | sig] --> [dato B | sig] --> [dato C | sig] --> null
```

```java
public class ListaEnlazada<T> implements Lista<T> {

    // Nodo de la lista
    private static class Nodo<T> {
        private T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    // Atributos
    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamanio;
```

Línea por línea:

- `public class ListaEnlazada<T> implements Lista<T>`: la clase es genérica (`<T>`) y cumple el contrato `Lista`. El compilador obliga a que escribamos todos los métodos de la interfaz.
- `private static class Nodo<T>`: una clase **dentro** de otra (clase anidada). Es `private` porque solo la lista necesita saber que existen los nodos; el resto del programa nunca los ve. Es `static` porque un nodo no necesita acceder a la lista que lo contiene.
- Dentro de `Nodo`: `dato` es lo que guarda, y `siguiente` es la **flecha** hacia el próximo nodo. El último nodo tiene `siguiente == null`.
- `primero`, `ultimo`: las dos puntas de la lista. Tener `ultimo` permite agregar al final en una sola operación, sin recorrer.
- `tamanio`: se lleva la cuenta para no tener que contar cada vez.

### `agregar`: poner un elemento al final

```java
public void agregar(T elemento) {
    Nodo<T> nuevo = new Nodo<>(elemento);
    if (tamanio == 0) {
        primero = nuevo;
    } else {
        ultimo.siguiente = nuevo;
    }
    ultimo = nuevo;
    tamanio++;
}
```

1. Se crea un nodo nuevo con el dato.
2. Si la lista estaba vacía (`tamanio == 0`), ese nodo es a la vez el primero y el último.
3. Si no, el que era último ahora apunta al nuevo (`ultimo.siguiente = nuevo`).
4. En ambos casos el nuevo pasa a ser el último y se suma uno al tamaño.

Es O(1): no importa cuántos elementos haya, siempre son los mismos pasos.

### `insertar`: poner un elemento en una posición

```java
public void insertar(int indice, T elemento) {
    if (indice < 0 || indice > tamanio) {
        throw new IndexOutOfBoundsException("índice fuera de rango: " + indice);
    }
    if (indice == tamanio) {
        agregar(elemento);
        return;
    }
    Nodo<T> nuevo = new Nodo<>(elemento);
    if (indice == 0) {
        nuevo.siguiente = primero;
        primero = nuevo;
    } else {
        Nodo<T> anterior = nodoEn(indice - 1);
        nuevo.siguiente = anterior.siguiente;
        anterior.siguiente = nuevo;
    }
    tamanio++;
}
```

- Primero se valida el índice. Se permite `indice == tamanio` (insertar al final). Si es inválido se lanza `IndexOutOfBoundsException`, que es la excepción estándar para eso y la que espera el verificador del kit.
- `return;` dentro del `if` termina el método: insertar al final es lo mismo que `agregar`, entonces se reutiliza.
- Si el índice es 0, el nodo nuevo apunta al que era primero y pasa a ser el primero.
- En cualquier otro caso se busca el nodo **anterior** a la posición (`nodoEn(indice - 1)`), el nuevo apunta a lo que seguía y el anterior apunta al nuevo. Es importante hacerlo en ese orden: si primero cambiaras `anterior.siguiente`, perderías la referencia al resto de la lista.

### `obtener` e `indiceDe`

```java
public T obtener(int indice) {
    verificarIndice(indice);
    return nodoEn(indice).dato;
}

@Override
public int indiceDe(T elemento) {
    Nodo<T> actual = primero;
    int posicion = 0;
    while (actual != null) {
        if (iguales(actual.dato, elemento)) {
            return posicion;
        }
        actual = actual.siguiente;
        posicion++;
    }
    return -1;
}

@Override
public int tamanio() {
```

- `obtener` valida el índice y pide el nodo con `nodoEn`.
- `indiceDe` recorre con un nodo `actual` que empieza en `primero` y avanza con `actual = actual.siguiente` hasta llegar a `null` (el final). Devuelve la posición de la primera coincidencia o `-1` si no está. Usa un método privado `iguales` para comparar con `equals` aceptando `null` (llamar `equals` sobre `null` rompería con `NullPointerException`).

### `eliminar`

```java
public T eliminar(int indice) {
    verificarIndice(indice);
    T dato;
    if (indice == 0) {
        dato = primero.dato;
        primero = primero.siguiente;
        if (tamanio == 1) {
            ultimo = null;
        }
    } else {
        Nodo<T> anterior = nodoEn(indice - 1);
        Nodo<T> borrado = anterior.siguiente;
        dato = borrado.dato;
        anterior.siguiente = borrado.siguiente;
        if (borrado == ultimo) {
            ultimo = anterior;
        }
    }
    tamanio--;
    return dato;
}

@Override
public void vaciar() {
```

Hay tres casos:

1. **Borrar el primero**: `primero` pasa a apuntar al segundo. Si era el único elemento, `ultimo` también queda en `null` (si no, `ultimo` seguiría apuntando a un nodo que ya no pertenece a la lista y el próximo `agregar` enlazaría detrás de un nodo fantasma).
2. **Borrar uno del medio**: el anterior "salta" al borrado (`anterior.siguiente = borrado.siguiente`).
3. **Borrar el último**: igual que el del medio, pero además hay que mover `ultimo` hacia atrás (`ultimo = anterior`).

Java libera solo la memoria del nodo que ya nadie referencia (recolector de basura).

### Métodos privados auxiliares

```java
private void verificarIndice(int indice) {
    if (indice < 0 || indice >= tamanio) {
        throw new IndexOutOfBoundsException("índice fuera de rango: " + indice);
    }
}

// Recorre desde el primero hasta llegar a la posición pedida
private Nodo<T> nodoEn(int indice) {
    Nodo<T> actual = primero;
    for (int i = 0; i < indice; i++) {
        actual = actual.siguiente;
    }
    return actual;
}

// Compara dos datos aceptando null
```

`nodoEn` es el corazón de la lista: parte del primero y avanza `indice` veces. Por eso `obtener`, `insertar` y `eliminar` cuestan O(n): hay que caminar.

**Por qué una lista enlazada y no un arreglo:** la cantidad cambia mientras se juega y la consigna pide estructuras dinámicas con nodos. Con la lista no hay que preguntar "¿cuánto espacio reservo?" ni copiar elementos al agrandar.

### 3.3 `PilaEnlazada<T>`

**Para qué sirve:** guardar cosas de modo que lo **último que entró** sea **lo primero que sale** (LIFO). En el juego: la ruta recorrida, el diario de decisiones y el recorrido en profundidad del grafo.

**La idea:** como la lista, pero cada nodo apunta al que está **debajo**. La pila solo recuerda la cima.

```
cima
  |
  v
[C | debajo] --> [B | debajo] --> [A | debajo] --> null
```

```java
public class PilaEnlazada<T> implements Pila<T> {

    // Nodo de la pila
    private static class Nodo<T> {
        private final T dato;
        private final Nodo<T> debajo;

        private Nodo(T dato, Nodo<T> debajo) {
            this.dato = dato;
            this.debajo = debajo;
        }
    }

    // Atributos
    private Nodo<T> cima;
    private int tamanio;
```

- En el nodo, `debajo` es `final`: una vez creado el nodo, su "de abajo" nunca cambia (la pila nunca reordena).
- El constructor del nodo recibe el dato **y** el nodo de abajo.

```java
public void apilar(T elemento) {
    cima = new Nodo<>(elemento, cima);
    tamanio++;
}

@Override
public T desapilar() {
    if (tamanio == 0) {
        throw new IllegalStateException("la pila está vacía");
    }
    T dato = cima.dato;
    cima = cima.debajo;
    tamanio--;
    return dato;
}

@Override
public T tope() {
    if (tamanio == 0) {
        throw new IllegalStateException("la pila está vacía");
    }
    return cima.dato;
}

// Consultas
@Override
public boolean estaVacia() {
```

- `apilar`: `cima = new Nodo<>(elemento, cima)` hace dos cosas en una línea: crea un nodo cuyo "debajo" es la cima **actual**, y ese nodo nuevo pasa a ser la cima. Es O(1).
- `desapilar`: guarda el dato de la cima, mueve la cima al nodo de abajo y devuelve el dato. Si la pila está vacía lanza `IllegalStateException` ("ahora no se puede").
- `tope`: lo mismo que `desapilar` pero sin sacar: solo mira.
- Para **mirar toda la pila sin destruirla** hay que pasar los elementos a otra pila y devolverlos (lo hace `Partida`; ver sección 6.6).

### 3.4 `ColaEnlazada<T>`

**Para qué sirve:** que el **primero que llega sea el primero en salir** (FIFO). En el juego: los turnos del combate y el recorrido en anchura del grafo.

**La idea:** cada nodo apunta al **siguiente**; se agrega por el final y se saca por el principio. Por eso la cola recuerda las dos puntas.

```
primero                                   ultimo
   |                                         |
   v                                         v
[A | sig] --> [B | sig] --> [C | sig] --> null      (se saca A, se agrega después de C)
```

```java
public void encolar(T elemento) {
    Nodo<T> nuevo = new Nodo<>(elemento);
    if (tamanio == 0) {
        primero = nuevo;
    } else {
        ultimo.siguiente = nuevo;
    }
    ultimo = nuevo;
    tamanio++;
}

@Override
public T desencolar() {
    if (tamanio == 0) {
        throw new IllegalStateException("la cola está vacía");
    }
    T dato = primero.dato;
    primero = primero.siguiente;
    tamanio--;
    if (tamanio == 0) {
        ultimo = null;
    }
    return dato;
}

@Override
public T frente() {
    if (tamanio == 0) {
        throw new IllegalStateException("la cola está vacía");
    }
    return primero.dato;
}

// Consultas
@Override
public boolean estaVacia() {
```

- `encolar` es igual a `agregar` de la lista: si estaba vacía el nuevo es el primero; si no, el último apunta al nuevo.
- `desencolar` saca por el principio: guarda el dato, mueve `primero` al siguiente y, **si la cola quedó vacía, también pone `ultimo` en `null`** (el mismo problema del nodo fantasma que en la lista).
- `frente` mira el primero sin sacarlo.

---

### 3.5 `Grafo<T>`

**Para qué sirve:** representar el mapa del bosque. Un **grafo** es un conjunto de **vértices** (los lugares) unidos por **aristas** (los caminos). Es **dirigido** porque cada arista tiene un sentido: de A a B no significa de B a A. Es **de listas de adyacencia** porque cada vértice guarda la lista de los vértices a los que puede ir.

```
(0 inicio) --> (1 combate) --> (3 cofre) --> (5 jefe)
      \                          ^
       \-----> (2 evento) -------/
```

Lista de adyacencia de este grafo:

| Vértice | Adyacentes |
|---|---|
| 0 | 1, 2 |
| 1 | 3 |
| 2 | 3 |
| 3 | 5 |
| 5 | (ninguno) |

```java
public class Grafo<T> {

    // Vértice del grafo: dato + lista de adyacencia
    private static class Vertice<T> {
        private final int id;
        private final T dato;
        private final ListaEnlazada<Vertice<T>> adyacentes = new ListaEnlazada<>();
        private boolean visitado;

        private Vertice(int id, T dato) {
            this.id = id;
            this.dato = dato;
        }
    }

    // Atributos
    private final ListaEnlazada<Vertice<T>> vertices = new ListaEnlazada<>();
    private int aristas;
```

- `Grafo<T>`: `T` es el tipo del dato de cada vértice. En el juego es `NodoMapa`.
- `Vertice` es una clase anidada privada con: un `id` (su número), el `dato`, y `adyacentes`: **una `ListaEnlazada` de vértices**. Eso es la lista de adyacencia: la estructura que se construyó antes se reutiliza adentro de la nueva.
- `visitado` es una marca que usan los recorridos para no pasar dos veces por el mismo vértice.
- `vertices` es una lista con todos los vértices; la posición en la lista **es** el id (el vértice 0 está en la posición 0).
- `aristas` cuenta cuántas flechas hay.

### Altas

```java
public int agregarVertice(T dato) {
    int id = vertices.tamanio();
    vertices.agregar(new Vertice<>(id, dato));
    return id;
}

// Arista dirigida origen -> destino. Devuelve false si ya existía o si es un lazo.
public boolean agregarArista(int origen, int destino) {
    Vertice<T> desde = buscar(origen);
    Vertice<T> hasta = buscar(destino);
    if (origen == destino || existeArista(origen, destino)) {
        return false;
    }
    desde.adyacentes.agregar(hasta);
    aristas++;
    return true;
}
```

- `agregarVertice` calcula el id como el tamaño actual de la lista (0, 1, 2...), agrega el vértice y **devuelve el id**, así quien lo crea puede usarlo para conectarlo.
- `agregarArista(origen, destino)`:
  1. `buscar` convierte cada id en su `Vertice` y **lanza `IllegalArgumentException` si el id no existe** (y lo hace antes de modificar nada, así un error no deja el grafo a medias).
  2. Se rechazan los **lazos** (`origen == destino`: un lugar conectado consigo mismo no tiene sentido) y las aristas **repetidas**.
  3. Se agrega el destino a la lista de adyacentes del **origen** (solo del origen: por eso es dirigido; en uno no dirigido también habría que agregar el origen a la lista del destino).
  4. Devuelve `true` si agregó algo y `false` si no. Eso permite al generador del mapa intentar agregar una arista sin preocuparse de repetidos.

### Consultas

```java
public boolean existeArista(int origen, int destino) {
    Vertice<T> desde = buscar(origen);
    buscar(destino);
    for (int i = 0; i < desde.adyacentes.tamanio(); i++) {
        if (desde.adyacentes.obtener(i).id == destino) {
            return true;
        }
    }
    return false;
}

// Identificadores de los vecinos de un vértice (a dónde se puede ir desde él)
public ListaEnlazada<Integer> idsAdyacentes(int id) {
    Vertice<T> v = buscar(id);
    ListaEnlazada<Integer> ids = new ListaEnlazada<>();
    for (int i = 0; i < v.adyacentes.tamanio(); i++) {
        ids.agregar(v.adyacentes.obtener(i).id);
    }
    return ids;
}

// Datos de los vecinos de un vértice
public ListaEnlazada<T> adyacentes(int id) {
    Vertice<T> v = buscar(id);
    ListaEnlazada<T> datos = new ListaEnlazada<>();
    for (int i = 0; i < v.adyacentes.tamanio(); i++) {
        datos.agregar(v.adyacentes.obtener(i).dato);
    }
    return datos;
}

public int gradoSalida(int id) {
```

- `existeArista` recorre la lista de adyacentes del origen buscando el destino. Cuesta O(grado del origen) y como en este mapa cada lugar tiene entre 1 y 3 caminos, es casi instantáneo.
- `idsAdyacentes` y `adyacentes` devuelven **listas nuevas** (copias): quien las pida no puede romper la estructura interna del grafo (encapsulamiento). `idsAdyacentes` da los números; `adyacentes` da los datos (`NodoMapa`).

### Recorrido en anchura (BFS) con una cola

```java
public ListaEnlazada<Integer> recorridoEnAnchura(int inicio) {
    buscar(inicio);
    limpiarMarcas();
    ListaEnlazada<Integer> orden = new ListaEnlazada<>();
    ColaEnlazada<Vertice<T>> pendientes = new ColaEnlazada<>();
    Vertice<T> primero = buscar(inicio);
    primero.visitado = true;
    pendientes.encolar(primero);
    while (!pendientes.estaVacia()) {
        Vertice<T> actual = pendientes.desencolar();
        orden.agregar(actual.id);
        for (int i = 0; i < actual.adyacentes.tamanio(); i++) {
            Vertice<T> vecino = actual.adyacentes.obtener(i);
            if (!vecino.visitado) {
                vecino.visitado = true;
                pendientes.encolar(vecino);
            }
        }
    }
    return orden;
}

// Recorrido en profundidad (DFS): usa una PILA.
public ListaEnlazada<Integer> recorridoEnProfundidad(int inicio) {
```

Se explora el grafo **por niveles**: primero el inicio, después todos sus vecinos, después los vecinos de los vecinos.

1. `limpiarMarcas()` pone `visitado = false` en todos (para poder repetir el recorrido).
2. Se marca el inicio como visitado y se **encola**.
3. Mientras la cola no esté vacía: se **desencola** un vértice, se lo anota en el resultado y se encolan sus vecinos que todavía no estén marcados (marcándolos **al encolar**, no al sacar, para que nunca se encole dos veces el mismo).

Como la cola es FIFO, los vértices más cercanos al inicio salen primero.

### Recorrido en profundidad (DFS) con una pila

```java
public ListaEnlazada<Integer> recorridoEnProfundidad(int inicio) {
    buscar(inicio);
    limpiarMarcas();
    ListaEnlazada<Integer> orden = new ListaEnlazada<>();
    PilaEnlazada<Vertice<T>> pendientes = new PilaEnlazada<>();
    pendientes.apilar(buscar(inicio));
    while (!pendientes.estaVacia()) {
        Vertice<T> actual = pendientes.desapilar();
        if (!actual.visitado) {
            actual.visitado = true;
            orden.agregar(actual.id);
            // Se apilan al revés para que el primer vecino salga primero
            for (int i = actual.adyacentes.tamanio() - 1; i >= 0; i--) {
                Vertice<T> vecino = actual.adyacentes.obtener(i);
                if (!vecino.visitado) {
                    pendientes.apilar(vecino);
                }
            }
        }
    }
    return orden;
}

// ¿Se puede llegar de origen a destino siguiendo las flechas?
public boolean hayCamino(int origen, int destino) {
```

Se explora **lo más lejos posible** por un camino antes de volver. Es el mismo esquema que la anchura pero con una **pila**: el último vértice apilado es el primero en explorarse. Los vecinos se apilan al revés (`for` desde el final hacia el principio) para que el primer vecino quede arriba y se explore primero.

Aquí se marca el vértice **al sacarlo** (puede estar apilado más de una vez: por eso el `if (!actual.visitado)`).

### `hayCamino`

```java
public boolean hayCamino(int origen, int destino) {
    buscar(destino);
    ListaEnlazada<Integer> alcanzables = recorridoEnAnchura(origen);
    return alcanzables.indiceDe(destino) != -1;
}
```

Pregunta "¿puedo llegar de `origen` a `destino` siguiendo las flechas?" haciendo un recorrido en anchura y mirando si el destino aparece. Lo usan las pruebas para comprobar que el jefe es alcanzable.

### Costos (para la defensa)

| Operación | Costo |
|---|---|
| `agregarVertice` | O(1) (agrega al final de la lista de vértices) |
| `agregarArista` | O(grado del origen) por la búsqueda de repetidos, más recorrer la lista de vértices para ubicar los ids |
| `existeArista`, `idsAdyacentes` | O(grado del origen) |
| `recorridoEnAnchura`, `recorridoEnProfundidad` | O(V + A) en el recorrido, más el costo de `obtener(i)` de la lista |

**Por qué listas de adyacencia y no matriz de adyacencia:** una matriz necesita un arreglo de V×V celdas (prohibido en este trabajo) y casi todas quedarían vacías, porque cada lugar tiene solo 1 a 3 caminos.

---

## 4. Paquete `utilidades`: `Tokenizador`

**Para qué sirve:** partir un texto en trozos separados por un carácter. Por ejemplo `"uno|dos|tres"` con separador `'|'` da `uno`, `dos`, `tres`. Se usa para leer los archivos (`eventos.txt` y `guardado.txt`), para armar los sprites desde sus filas de texto y para cortar párrafos en renglones.

**Por qué existe:** el método estándar `String.split` devuelve un **arreglo** (`String[]`), y la cátedra pide evitar arreglos en nuestro código. El `Tokenizador` devuelve **un trozo por vez**, como un lector que avanza por el texto.

```java
public class Tokenizador {

    // Atributos
    private final String texto;
    private final char separador;
    private int posicion;
    private boolean quedaTrozo;

    // Constructor
    public Tokenizador(String texto, char separador) {
        if (texto == null) {
            throw new IllegalArgumentException("el texto no puede ser nulo");
        }
        this.texto = texto;
        this.separador = separador;
        this.posicion = 0;
        // Un texto vacío tiene cero trozos; "a|" tiene dos ("a" y "")
        this.quedaTrozo = !texto.isEmpty();
    }
```

- `texto` y `separador` son `final`: el tokenizador no cambia lo que está leyendo.
- `posicion` es el índice del próximo carácter a leer. Empieza en 0.
- `quedaTrozo` indica si falta algún trozo. Es necesario aparte de `posicion` por un caso borde: `"a|"` tiene **dos** trozos (`"a"` y uno vacío). Si preguntáramos solo "¿llegué al final del texto?", después de leer `"a"` creeríamos que no queda nada.
- El constructor rechaza un texto `null` (`IllegalArgumentException`) y deja `quedaTrozo` en `false` si el texto está vacío (cero trozos).

```java
public boolean hayMas() {
    return quedaTrozo;
}

// Devuelve el trozo siguiente (sin el separador)
public String siguiente() {
    if (!quedaTrozo) {
        throw new IllegalStateException("no quedan más trozos");
    }
    int fin = posicion;
    while (fin < texto.length() && texto.charAt(fin) != separador) {
        fin++;
    }
    String trozo = texto.substring(posicion, fin);
    if (fin >= texto.length()) {
        quedaTrozo = false;
    } else {
        posicion = fin + 1;
    }
    return trozo;
}

// Cantidad de trozos que tiene el texto completo
public static int contar(String texto, char separador) {
```

`siguiente` hace esto:

1. Parte de `posicion` y avanza con `fin++` mientras no llegue al final del texto ni encuentre el separador.
2. Corta el trozo con `substring(posicion, fin)` (desde `posicion` incluida hasta `fin` excluida).
3. Si `fin` llegó al final del texto, ese era el último trozo (`quedaTrozo = false`). Si no, `fin` apunta al separador y `posicion = fin + 1` salta el separador para el próximo trozo.
4. Si ya no quedan trozos y alguien pide otro, lanza `IllegalStateException`.

Ejemplo con `"uno|dos"`:

| Llamada | `posicion` antes | `fin` | Devuelve | `posicion` después | `quedaTrozo` |
|---|---|---|---|---|---|
| 1 | 0 | 3 (el `|`) | `uno` | 4 | `true` |
| 2 | 4 | 7 (fin del texto) | `dos` | 4 | `false` |

`contar(texto, separador)` es un método estático que recorre un tokenizador completo y cuenta los trozos.

---

## 5. Paquete `juego.entidades`

Acá viven los "personajes" del juego: el héroe, sus habilidades y los enemigos. **Ninguna clase de este paquete dibuja nada.**

### 5.1 `Calidad`

**Para qué sirve:** nombrar el resultado de la barra de precisión: fallo, normal o crítico.

```java
public final class Calidad {

    // Constantes
    public static final int FALLO = 0;
    public static final int NORMAL = 1;
    public static final int CRITICO = 2;

    // Sin instancias
    private Calidad() {
    }

    public static String nombre(int calidad) {
        if (calidad == CRITICO) {
            return "CRÍTICO";
        }
        if (calidad == NORMAL) {
            return "BIEN";
        }
        return "FALLO";
    }
}
```

- Son tres constantes enteras (`public static final`): `Calidad.FALLO` vale 0, `Calidad.NORMAL` 1 y `Calidad.CRITICO` 2. Se escriben en mayúsculas por convención.
- El constructor es `private`: nadie puede hacer `new Calidad()` (no tiene sentido; es una clase de utilidad).
- **Por qué está en `entidades` y no en `reglas`:** la barra de precisión (en `reglas`) devuelve una calidad y las habilidades (en `entidades`) la reciben. Si `Habilidad` importara `BarraPrecision` y `BarraPrecision` importara `Jugador` habría un **ciclo de dependencias** entre paquetes. Poniendo estas tres constantes en `entidades`, el ciclo desaparece.

### 5.2 `Habilidad` (abstracta)

**Para qué sirve:** ser el molde común de las cuatro habilidades del héroe. Define lo que **todas** tienen (código, nombre, costo de maná) y obliga a cada una a decir **qué hace** (`aplicar`).

```java
public abstract class Habilidad {

    // Atributos
    private final String codigo;
    private final String nombre;
    private final int costoMana;

    // Constructor
    protected Habilidad(String codigo, String nombre, int costoMana) {
        if (codigo == null || codigo.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un código");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un nombre");
        }
        if (costoMana < 0) {
            throw new IllegalArgumentException("el costo de maná no puede ser negativo: " + costoMana);
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.costoMana = costoMana;
    }
```

- `abstract class`: no se puede hacer `new Habilidad(...)`. Solo existen las hijas.
- El constructor es `protected`: solo lo llaman las clases hijas con `super(...)`.
- Las tres validaciones lanzan `IllegalArgumentException` con un mensaje claro. **Validar en el constructor garantiza que nunca exista una habilidad inválida** (un nombre vacío, un costo negativo).
- `codigo` es el identificador interno (`"ATAQUE"`, `"CURACION"`...). Es lo que se guarda en el archivo.

```java
public abstract String aplicar(Jugador jugador, Enemigo enemigo, int calidad);

public abstract String getDescripcion();

// Valores por defecto que algunas subclases cambian
public boolean usaBarra() {
    return true;
}

public double getDificultadBarra() {
    return 1.0;
}

public boolean esEscape() {
    return false;
}

// Getters
public String getCodigo() {
    return codigo;
}

public String getNombre() {
```

- `aplicar` y `getDescripcion` son **abstractos**: no tienen cuerpo (terminan en `;`). Cada hija **debe** escribirlos o no compila.
- `usaBarra`, `getDificultadBarra` y `esEscape` tienen un **valor por defecto** (la mayoría usa la barra, con dificultad normal, y no es un escape). Las hijas que sean distintas lo **sobrescriben** con `@Override`.
- `textoCalidad` es `protected`: lo usan las hijas para escribir "¡CRÍTICO!" o "Fallaste." antes del mensaje.

**Polimorfismo:** `Combate` guarda una `Habilidad` y llama `habilidad.aplicar(...)`. No sabe (ni le importa) cuál de las cuatro es: Java ejecuta la versión de la hija correcta.

### 5.3 Las cuatro habilidades

```java
public class HabilidadAtaque extends Habilidad {

    // Constantes
    public static final String CODIGO = "ATAQUE";
    private static final int MANA_RECUPERADO = 2;

    // Constructor
    public HabilidadAtaque() {
        super(CODIGO, "Ataque con arma", 0);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Tu espada pasa de largo.";
        }
        int danio = jugador.getDanioFisico();
        if (calidad == Calidad.CRITICO) {
            danio = danio * 2;
        }
        int real = enemigo.recibirDanio(danio);
        jugador.cambiarMana(MANA_RECUPERADO);
        return textoCalidad(calidad) + "Golpeas a " + enemigo.getNombre() + " por " + real
                + " de daño. (+" + MANA_RECUPERADO + " maná)";
    }

    @Override
    public String getDescripcion() {
        return "Golpe con tu arma. Daño según Fuerza. Recupera 2 de maná.";
    }
}
```

- `extends Habilidad`: "un ataque **es una** habilidad".
- `CODIGO` es una constante pública: así otras clases escriben `HabilidadAtaque.CODIGO` en lugar de repetir `"ATAQUE"` (un error de tipeo no se detectaría; con la constante sí).
- `super(CODIGO, "Ataque con arma", 0)`: llama al constructor del padre con el código, el nombre y **0 de maná**.
- `aplicar`:
  - **Fallo**: no hace daño, solo avisa.
  - Si no: el daño es `jugador.getDanioFisico()` (que escala con **Fuerza**). Si fue **crítico**, se duplica.
  - `enemigo.recibirDanio(danio)` devuelve el daño **real** aplicado (que puede ser menor si al enemigo le quedaba poca vida) y es el que se muestra.
  - Al acertar, devuelve 2 de maná: así el héroe nunca se queda sin poder hacer nada.

```java
public class HabilidadCuracion extends Habilidad {

    // Constantes
    public static final String CODIGO = "CURACION";

    // Constructor
    public HabilidadCuracion() {
        super(CODIGO, "Luz sanadora", 4);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        int curado = jugador.curar(jugador.getPotenciaCuracion());
        return "Una luz tibia te envuelve: recuperas " + curado + " de vida.";
    }

    @Override
    public boolean usaBarra() {
        return false;
    }

    @Override
    public String getDescripcion() {
        return "Recupera vida según tu Inteligencia. No usa la barra.";
    }
}
```

- Cuesta 4 de maná y **no usa la barra**: sobrescribe `usaBarra()` para devolver `false`.
- Cura `getPotenciaCuracion()` (6 + 3 × Inteligencia). `jugador.curar(...)` devuelve lo que **realmente** curó (no puede pasar de la vida máxima) y eso se muestra.
- Ignora los parámetros `enemigo` y `calidad`: tiene que recibirlos porque así lo define el método abstracto del padre.

```java
public class HabilidadGolpePotente extends Habilidad {

    // Constantes
    public static final String CODIGO = "GOLPE";

    // Constructor
    public HabilidadGolpePotente() {
        super(CODIGO, "Golpe arcano", 5);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "El hechizo se disipa en el aire.";
        }
        int danio = jugador.getPotenciaMagica();
        if (calidad == Calidad.CRITICO) {
            danio = danio * 5 / 2;
        }
        int real = enemigo.recibirDanio(danio);
        return textoCalidad(calidad) + "Un rayo arcano hiere a " + enemigo.getNombre() + " por " + real + " de daño.";
    }

    @Override
    public double getDificultadBarra() {
        return 0.6;
    }

    @Override
    public String getDescripcion() {
        return "Gran daño mágico (Inteligencia). La barra es más difícil.";
    }
}
```

- Cuesta 5 de maná y su daño escala con **Inteligencia** (`getPotenciaMagica`).
- El crítico hace `danio * 5 / 2` (2,5 veces; con enteros primero se multiplica y después se divide para no perder precisión).
- `getDificultadBarra()` devuelve 0,6: la barra se vuelve **más exigente** (zona crítica más chica y cursor más rápido). Lo pedía el enunciado: gran daño, barra más difícil.

```java
public class HabilidadEscape extends Habilidad {

    // Constantes
    public static final String CODIGO = "ESCAPE";

    // Constructor
    public HabilidadEscape() {
        super(CODIGO, "Paso sombrío", 3);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        return "Te fundes con las sombras y escapas del combate.";
    }

    @Override
    public boolean usaBarra() {
        return false;
    }

    @Override
    public boolean esEscape() {
        return true;
    }

    @Override
    public String getDescripcion() {
        return "Huyes de un combate normal. No funciona contra jefes.";
    }
}
```

- Cuesta 3 de maná, no usa la barra y devuelve `true` en `esEscape()`.
- **El Escape no huye por sí mismo**: solo se identifica como escape. Es `Combate` quien, al ver `esEscape()`, termina la pelea con el estado `HUIDA` (y quien impide usarlo contra el jefe). Así cada clase hace una sola cosa.

### 5.4 `CatalogoHabilidades`

**Para qué sirve:** crear una habilidad a partir de su código (`"GOLPE"` → un objeto `HabilidadGolpePotente`). Se necesita al **cargar** el archivo de guardado y al leer eventos, donde las habilidades aparecen como texto.

```java
public static String codigoPorOrden(int orden) {
    if (orden == 0) {
        return HabilidadAtaque.CODIGO;
    }
    if (orden == 1) {
        return HabilidadCuracion.CODIGO;
    }
    if (orden == 2) {
        return HabilidadGolpePotente.CODIGO;
    }
    if (orden == 3) {
        return HabilidadEscape.CODIGO;
    }
    throw new IllegalArgumentException("orden inválido: " + orden);
}
```

```java
public static boolean esValido(String codigo) {
    for (int i = 0; i < CANTIDAD; i++) {
        if (codigoPorOrden(i).equals(codigo)) {
            return true;
        }
    }
    return false;
}
```

```java
public static Habilidad crear(String codigo) {
    if (HabilidadAtaque.CODIGO.equals(codigo)) {
        return new HabilidadAtaque();
    }
    if (HabilidadCuracion.CODIGO.equals(codigo)) {
        return new HabilidadCuracion();
    }
    if (HabilidadGolpePotente.CODIGO.equals(codigo)) {
        return new HabilidadGolpePotente();
    }
    if (HabilidadEscape.CODIGO.equals(codigo)) {
        return new HabilidadEscape();
    }
    throw new IllegalArgumentException("habilidad desconocida: " + codigo);
}
```

- Es una **clase de utilidad**: constructor privado, métodos `static`.
- `codigoPorOrden(0..3)` define el **orden en que se desbloquean**: Ataque, Curación, Golpe, Escape. Lo usa `Jugador.proximaHabilidadPorAprender` y el cofre.
- `esValido(codigo)` recorre el catálogo preguntando si el texto coincide con alguno.
- `crear(codigo)` es una **fábrica** (patrón *Factory*): devuelve el objeto correcto según el texto. Con un código desconocido lanza `IllegalArgumentException`. Se compara con `HabilidadAtaque.CODIGO.equals(codigo)` y no al revés para que, si `codigo` fuera `null`, no se produzca un `NullPointerException`.

### 5.5 `Jugador`

**Para qué sirve:** guardar todo lo del héroe: atributos, vida, maná, oro y el repertorio de habilidades. Aplica las **fórmulas** que conectan los tres atributos con todo lo demás.

```java
public final class Jugador {

    // Constantes
    public static final int FUERZA = 0;
    public static final int DESTREZA = 1;
    public static final int INTELIGENCIA = 2;
    public static final int MAX_HABILIDADES = 4;
    private static final int ATRIBUTO_INICIAL = 3;
    private static final int ATRIBUTO_MAXIMO = 20;

    // Atributos
    private int fuerza;
    private int destreza;
    private int inteligencia;
    private int vida;
    private int mana;
    private int oro;
    private final ListaEnlazada<Habilidad> habilidades = new ListaEnlazada<>();
```

- `final class`: no se puede heredar (hay un único personaje).
- Las constantes `FUERZA`, `DESTREZA`, `INTELIGENCIA` (0, 1, 2) identifican un atributo al llamar `mejorarAtributo(Jugador.FUERZA, 2)`.
- `MAX_HABILIDADES = 4` es el límite que pide el enunciado.
- `habilidades` es una `ListaEnlazada<Habilidad>`: **el repertorio es una lista enlazada propia**, como exige la consigna. Es `final` porque la lista se crea una vez; lo que cambia es su contenido.

```java
public Jugador() {
    this(ATRIBUTO_INICIAL, ATRIBUTO_INICIAL, ATRIBUTO_INICIAL, 0, 0, 0);
    this.vida = getVidaMaxima();
    this.mana = getManaMaximo();
    habilidades.agregar(new HabilidadAtaque());
}

// Constructor para cargar una partida: sin habilidades (se agregan después)
public Jugador(int fuerza, int destreza, int inteligencia, int vida, int mana, int oro) {
    validarAtributo(fuerza, "fuerza");
    validarAtributo(destreza, "destreza");
    validarAtributo(inteligencia, "inteligencia");
    this.fuerza = fuerza;
    this.destreza = destreza;
    this.inteligencia = inteligencia;
    this.vida = limitar(vida, 0, getVidaMaxima());
    this.mana = limitar(mana, 0, getManaMaximo());
    this.oro = Math.max(0, oro);
}
```

Hay **dos constructores**:

1. `Jugador()`: una partida nueva. Llama al otro constructor con `this(3, 3, 3, 0, 0, 0)` (3 en cada atributo y vida/maná/oro provisorios en 0), y después, como ya existen las fórmulas, pone la vida y el maná en su máximo y agrega la habilidad Ataque.
2. `Jugador(fuerza, destreza, inteligencia, vida, mana, oro)`: para **cargar** una partida. Valida cada atributo (entre 1 y 20), y deja la vida y el maná dentro de sus límites con `limitar`. No agrega habilidades: las agrega quien carga.

`this(...)` en la primera línea de un constructor significa "usá el otro constructor para lo básico".

**Por qué la vida inicial se pone después:** `getVidaMaxima()` usa `fuerza`, que recién tiene valor cuando el otro constructor terminó.

```java
public int getVidaMaxima() {
    return 20 + 5 * fuerza;
}

public int getManaMaximo() {
    return 6 + 3 * inteligencia;
}

public int getDefensa() {
    return destreza / 2;
}

public int getDanioFisico() {
    return 2 + 2 * fuerza;
}

public int getPotenciaMagica() {
    return 3 + 3 * inteligencia;
}

public int getPotenciaCuracion() {
    return 6 + 3 * inteligencia;
}

// Bonus de la barra: más Destreza = zona crítica más ancha y cursor más lento
public int getBonusZonaCritica() {
    return 4 * destreza;
}

public double getFactorVelocidadBarra() {
    return Math.max(0.5, 1.0 - 0.03 * destreza);
}
```

Estas son las **fórmulas**. Se calculan cada vez que se piden (no se guardan), así que si sube un atributo todo se actualiza solo:

| Método | Fórmula | Efecto |
|---|---|---|
| `getVidaMaxima` | 20 + 5 × Fuerza | Más Fuerza, más vida |
| `getManaMaximo` | 6 + 3 × Inteligencia | |
| `getDefensa` | Destreza ÷ 2 (entera) | Resta daño recibido |
| `getDanioFisico` | 2 + 2 × Fuerza | Daño del ataque con arma |
| `getPotenciaMagica` | 3 + 3 × Inteligencia | Daño del golpe arcano |
| `getPotenciaCuracion` | 6 + 3 × Inteligencia | Cuánto cura |
| `getBonusZonaCritica` | 4 × Destreza | Píxeles extra de zona crítica en la barra |
| `getFactorVelocidadBarra` | 1 − 0,03 × Destreza, pero nunca menos que 0,5 | Cursor más lento |

`Math.max(0.5, ...)` pone un **piso** al factor: aunque la Destreza fuera enorme, el cursor nunca se detiene del todo.

### Recibir daño, curarse, gastar maná

```java
public int recibirDanio(int danioBruto) {
    if (danioBruto < 0) {
        throw new IllegalArgumentException("el daño no puede ser negativo: " + danioBruto);
    }
    int real = Math.max(1, danioBruto - getDefensa());
    vida = Math.max(0, vida - real);
    return real;
}

// Devuelve cuánta vida se recuperó realmente
public int curar(int cantidad) {
    if (cantidad < 0) {
        throw new IllegalArgumentException("la curación no puede ser negativa: " + cantidad);
    }
    int antes = vida;
    vida = Math.min(getVidaMaxima(), vida + cantidad);
    return vida - antes;
}

public boolean estaMuerto() {
    return vida <= 0;
}
```

- `recibirDanio(danioBruto)`: rechaza daños negativos. El daño real es `danioBruto - defensa`, pero **como mínimo 1** (`Math.max(1, ...)`): la defensa nunca vuelve invulnerable al héroe. La vida baja pero **nunca por debajo de 0** (`Math.max(0, ...)`). Devuelve el daño real para poder mostrarlo.
- `curar(cantidad)`: la vida sube pero no pasa de la máxima (`Math.min`). Devuelve lo realmente curado (`vida - antes`).
- `estaMuerto()`: vida en 0.

```java
public void cambiarVida(int delta) {
    vida = limitar(vida + delta, 1, getVidaMaxima());
}

public void cambiarMana(int delta) {
    mana = limitar(mana + delta, 0, getManaMaximo());
}

public void cambiarOro(int delta) {
    oro = Math.max(0, oro + delta);
}

public boolean puedePagar(int costoMana) {
    return mana >= costoMana;
}

public void gastarMana(int cantidad) {
    if (cantidad < 0 || cantidad > mana) {
        throw new IllegalArgumentException("maná insuficiente: se pide " + cantidad + " y hay " + mana);
    }
    mana = mana - cantidad;
}

// Atributos: subir uno también sube el máximo correspondiente
public void mejorarAtributo(int cual, int cantidad) {
    if (cantidad <= 0) {
        throw new IllegalArgumentException("la mejora debe ser positiva: " + cantidad);
    }
    if (cual == FUERZA) {
        fuerza = Math.min(ATRIBUTO_MAXIMO, fuerza + cantidad);
        vida = Math.min(getVidaMaxima(), vida + 5 * cantidad);
    } else if (cual == DESTREZA) {
        destreza = Math.min(ATRIBUTO_MAXIMO, destreza + cantidad);
    } else if (cual == INTELIGENCIA) {
        inteligencia = Math.min(ATRIBUTO_MAXIMO, inteligencia + cantidad);
        mana = Math.min(getManaMaximo(), mana + 3 * cantidad);
    } else {
        throw new IllegalArgumentException("atributo desconocido: " + cual);
    }
}
```

- `cambiarVida(delta)`, `cambiarMana(delta)` y `cambiarOro(delta)` los usan los **eventos** (el delta puede ser positivo o negativo). `limitar(valor, minimo, maximo)` es un método privado que mantiene el valor dentro del rango.
- **Un evento nunca mata:** `cambiarVida` limita la vida a un mínimo de **1**. En cambio, el combate sí puede dejarla en 0 (`recibirDanio`).
- `gastarMana`: lanza `IllegalArgumentException` si se pide más maná del que hay. Quien la llama debe preguntar antes con `puedePagar` (lo hace `Combate.elegirHabilidad`).
- `mejorarAtributo(cual, cantidad)`: sube el atributo (con tope 20) y, además, **sube también la vida o el maná actual** en lo que aumentó el máximo (Fuerza: +5 por punto; Inteligencia: +3 por punto). Si no, un héroe que sube Fuerza tendría el máximo más alto pero la barra de vida igual de vacía. Un atributo desconocido lanza `IllegalArgumentException`.

### El repertorio de habilidades (la lista enlazada en acción)

```java
public boolean aprenderHabilidad(Habilidad nueva) {
    if (nueva == null) {
        throw new IllegalArgumentException("la habilidad no puede ser nula");
    }
    if (conoceHabilidad(nueva.getCodigo()) || habilidades.tamanio() >= MAX_HABILIDADES) {
        return false;
    }
    habilidades.agregar(nueva);
    return true;
}

public boolean conoceHabilidad(String codigo) {
    for (int i = 0; i < habilidades.tamanio(); i++) {
        if (habilidades.obtener(i).getCodigo().equals(codigo)) {
            return true;
        }
    }
    return false;
}

public Habilidad getHabilidad(int indice) {
    return habilidades.obtener(indice);
}

public int cantidadHabilidades() {
    return habilidades.tamanio();
}

// Código de la primera habilidad del catálogo que todavía no se conoce (null si ya las tiene todas)
public String proximaHabilidadPorAprender() {
    for (int i = 0; i < CatalogoHabilidades.CANTIDAD; i++) {
        String codigo = CatalogoHabilidades.codigoPorOrden(i);
        if (!conoceHabilidad(codigo)) {
            return codigo;
        }
    }
    return null;
}
```

- `aprenderHabilidad`: rechaza `null`; devuelve `false` si el héroe **ya la conoce** o si el repertorio **ya está lleno** (4); si no, la agrega a la lista y devuelve `true`.
- `conoceHabilidad(codigo)`: recorre la lista con un `for` y `obtener(i)` comparando códigos.
- `proximaHabilidadPorAprender()`: recorre el **catálogo** en orden y devuelve el código de la primera que el héroe todavía no conoce, o `null` si ya las sabe todas. La usa el cofre.

---

### 5.6 `Enemigo` (abstracta)

**Para qué sirve:** ser el molde de los enemigos: vida, daño, recompensa y los métodos comunes. Cada enemigo concreto decide **cómo ataca**.

```java
public abstract class Enemigo {

    // Atributos
    private final String nombre;
    private final String sprite;
    private int vidaMaxima;
    private int vida;
    private int danio;
    private final int recompensaOro;
    private int acciones;

    // Constructor
    protected Enemigo(String nombre, String sprite, int vidaMaxima, int danio, int recompensaOro) {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("el enemigo necesita un nombre");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("la vida debe ser positiva: " + vidaMaxima);
        }
        if (danio < 0 || recompensaOro < 0) {
            throw new IllegalArgumentException("daño y recompensa no pueden ser negativos");
        }
        this.nombre = nombre;
        this.sprite = sprite;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.danio = danio;
        this.recompensaOro = recompensaOro;
    }
```

El constructor valida todo (nombre, vida positiva, daño y recompensa no negativos). `vida` empieza igual a `vidaMaxima`. `acciones` cuenta cuántas veces actuó el enemigo (lo usan el Brujo y el Rey).

```java
protected abstract String accionar(Jugador jugador, int numeroDeAccion);

// Turno del enemigo: cuenta la acción y delega en la subclase
public final String atacar(Jugador jugador) {
    acciones++;
    return accionar(jugador, acciones);
}

// Los enemigos de pisos más altos son más duros
public void reforzar(int piso) {
    if (piso < 0) {
        throw new IllegalArgumentException("el piso no puede ser negativo: " + piso);
    }
    vidaMaxima = vidaMaxima + 3 * piso;
    vida = vidaMaxima;
    danio = danio + piso / 2;
}

// Devuelve el daño real aplicado
public int recibirDanio(int cantidad) {
    if (cantidad < 0) {
        throw new IllegalArgumentException("el daño no puede ser negativo: " + cantidad);
    }
    int real = Math.min(vida, cantidad);
    vida = vida - real;
    return real;
}

protected void curar(int cantidad) {
    vida = Math.min(vidaMaxima, vida + cantidad);
}
```

**Método plantilla (*Template Method*):**

- `accionar` es **abstracto y protegido**: lo escribe cada hija.
- `atacar` es **`final`**: cuenta la acción y llama a `accionar`. Las hijas no pueden cambiar esta estructura; solo el "cómo". `Combate` llama a `atacar` y listo.

Otros métodos:

- `reforzar(piso)`: los enemigos de los pisos altos son más duros (+3 de vida por piso, +1 de daño cada 2 pisos).
- `recibirDanio`: `Math.min(vida, cantidad)` evita que la vida quede negativa y **devuelve el daño real** (si le quedaban 5 de vida y le pegás 16, el daño real es 5).
- `curar` es `protected`: solo las hijas (el Brujo) pueden curarse.

```java
public class Esqueleto extends Enemigo {

    // Constructor
    public Esqueleto() {
        super("Esqueleto", "esqueleto", 18, 5, 6);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        return "El Esqueleto te golpea con su espada oxidada: -" + real + " de vida.";
    }
}
```

```java
public class Lobo extends Enemigo {

    // Constructor
    public Lobo() {
        super("Lobo", "lobo", 14, 3, 5);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        return "El Lobo te muerde: -" + real + " de vida.";
    }

    @Override
    public int getTurnosPorRonda() {
        return 2;
    }
}
```

```java
public class Brujo extends Enemigo {

    // Constantes
    private static final int CURACION = 5;

    // Constructor
    public Brujo() {
        super("Brujo", "brujo", 16, 6, 9);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        if (numeroDeAccion % 3 == 0) {
            curar(CURACION);
            return "El Brujo murmura un conjuro y se cura " + CURACION + " de vida.";
        }
        int real = jugador.recibirDanio(getDanio());
        return "El Brujo lanza una esfera oscura: -" + real + " de vida.";
    }
}
```

```java
public class ReyOscuro extends Enemigo {

    // Constructor
    public ReyOscuro() {
        super("Rey Oscuro", "rey", 60, 8, 50);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        if (numeroDeAccion % 4 == 0) {
            int real = jugador.recibirDanio(getDanio() * 2);
            return "¡El Rey Oscuro descarga un golpe devastador! -" + real + " de vida.";
        }
        int real = jugador.recibirDanio(getDanio());
        return "El Rey Oscuro te hiere con su hoja negra: -" + real + " de vida.";
    }
}
```

Los cuatro enemigos comparten la forma: un constructor que llama a `super(nombre, sprite, vida, daño, oro)` y un método `accionar`.

| Enemigo | Vida | Daño | Particularidad | Cómo se logra |
|---|---|---|---|---|
| `Esqueleto` | 18 | 5 | Resistente, golpe parejo | Solo `accionar` |
| `Lobo` | 14 | 3 | **Actúa dos veces por ronda** | Sobrescribe `getTurnosPorRonda()` y devuelve 2: `Combate` encola dos turnos suyos |
| `Brujo` | 16 | 6 | **Cada 3.ª acción se cura 5** en lugar de atacar | `numeroDeAccion % 3 == 0` |
| `ReyOscuro` | 60 | 8 | **Cada 4.ª acción pega doble** | `numeroDeAccion % 4 == 0` |

`numeroDeAccion % 3 == 0` significa "el resto de dividir por 3 es 0", o sea, la acción número 3, 6, 9...

El Lobo es un buen ejemplo de **diseño dirigido por datos**: para que actúe dos veces no hay ninguna condición especial en `Combate`; solo cambia cuántas veces se encola.

### 5.7 `FabricaEnemigos`

```java
public static Enemigo crearAleatorio(int piso, Random azar) {
    int tipos = 2;
    if (piso >= 3) {
        tipos = 3;
    }
    int elegido = azar.nextInt(tipos);
    Enemigo enemigo;
    if (elegido == 0) {
        enemigo = new Esqueleto();
    } else if (elegido == 1) {
        enemigo = new Lobo();
    } else {
        enemigo = new Brujo();
    }
    enemigo.reforzar(piso);
    return enemigo;
}

// Enemigo por nombre (lo usan las emboscadas escritas en eventos.txt)
public static Enemigo crearPorNombre(String nombre, int piso) {
    Enemigo enemigo;
    if ("ESQUELETO".equals(nombre)) {
        enemigo = new Esqueleto();
    } else if ("LOBO".equals(nombre)) {
        enemigo = new Lobo();
    } else if ("BRUJO".equals(nombre)) {
        enemigo = new Brujo();
    } else {
        throw new IllegalArgumentException("enemigo desconocido: " + nombre);
    }
    enemigo.reforzar(piso);
    return enemigo;
}

public static Enemigo crearJefe() {
    return new ReyOscuro();
}
```

- `crearAleatorio(piso, azar)`: elige un tipo al azar con `azar.nextInt(tipos)`, que da un número entre 0 y `tipos - 1`. Hasta el piso 2 hay 2 tipos posibles (Esqueleto, Lobo) y **desde el piso 3 se suma el Brujo**. Después lo `reforzar(piso)`.
- Recibe el `Random` por parámetro (en vez de crear uno): así quien llama controla la semilla y el resultado es **repetible** (lo aprovecha `Partida`).
- `crearPorNombre` se usa para las emboscadas escritas en `eventos.txt` (`COMBATE:LOBO`).
- `crearJefe` devuelve el Rey Oscuro sin refuerzo.

Es una **clase de utilidad** (constructor privado, métodos estáticos).

---

## 6. Paquete `juego.reglas`

Las reglas del juego: cómo funciona un combate, cómo se genera el mapa, qué es una partida, qué es un evento. **Tampoco dibuja nada.**

### 6.1 `BarraPrecision`

**Para qué sirve:** el minijuego estilo Undertale. Un cursor va y viene sobre una barra; el jugador lo frena y, según qué tan cerca del centro quedó, el golpe es **fallo**, **normal** o **crítico**. La **Destreza** lo vuelve más fácil.

```
|------ fallo ------|-------- normal --------|[crítico]|-------- normal --------|------ fallo ------|
0                                        centro (300)                                         600
```

```java
public static final double ANCHO = 600;
private static final double ANCHO_NORMAL_BASE = 420;
private static final double ANCHO_CRITICO_BASE = 40;
private static final double VELOCIDAD_BASE = 560;

// Atributos
private final double anchoCritico;
private final double anchoNormal;
private final double velocidad;
private double posicion;
private int direccion = 1;
private boolean detenida;
private int resultado = Calidad.FALLO;
```

- `ANCHO = 600`: la barra mide 600 unidades (píxeles en pantalla). Posiciones: de 0 a 600; el centro es 300.
- Las constantes `..._BASE` son los valores de referencia con dificultad 1.0 y sin Destreza.
- `anchoCritico` y `anchoNormal` son los anchos **totales** de cada zona (centradas en 300). `velocidad` es en unidades por segundo.
- `posicion` es dónde está el cursor; `direccion` vale 1 (va hacia la derecha) o -1 (hacia la izquierda); `detenida` pasa a `true` cuando el jugador la frena.

```java
public BarraPrecision(Jugador jugador, double dificultad) {
    this(jugador.getBonusZonaCritica(), jugador.getFactorVelocidadBarra(), dificultad);
}
```

```java
public BarraPrecision(int bonusCritico, double factorVelocidad, double dificultad) {
    if (!(dificultad > 0 && dificultad <= 1.5)) {
        throw new IllegalArgumentException("dificultad fuera de rango: " + dificultad);
    }
    if (!(factorVelocidad > 0)) {
        throw new IllegalArgumentException("el factor de velocidad debe ser positivo: " + factorVelocidad);
    }
    this.anchoCritico = (ANCHO_CRITICO_BASE + bonusCritico) * dificultad;
    this.anchoNormal = ANCHO_NORMAL_BASE * (0.5 + 0.5 * dificultad);
    this.velocidad = VELOCIDAD_BASE * factorVelocidad * (2.0 - dificultad);
}
```

Dos constructores:

1. El principal recibe el `Jugador` y la dificultad de la habilidad, y llama al segundo con `this(...)` pasándole **los datos del jugador**: aquí entra la **Destreza** (`getBonusZonaCritica`, `getFactorVelocidadBarra`).
2. El segundo recibe números sueltos (sirve para hacer pruebas sin crear un jugador). Valida que la dificultad esté entre 0 y 1,5 y que el factor de velocidad sea positivo. Escribe la condición como `!(x > 0 && x <= 1.5)` para que un valor `NaN` (no es un número) también sea rechazado.

Las fórmulas:

- `anchoCritico = (40 + bonus) × dificultad`. Con Destreza 3 y dificultad 1: (40 + 12) × 1 = **52**. Con el golpe arcano (0,6): 52 × 0,6 = 31,2: mucho más difícil de acertar.
- `anchoNormal = 420 × (0,5 + 0,5 × dificultad)`. Con dificultad 1 son 420; con 0,6 son 336.
- `velocidad = 560 × factor × (2 − dificultad)`. Con dificultad menor, el término `(2 − dificultad)` es mayor: el cursor va **más rápido**. Con más Destreza el factor es menor: va **más lento**.

```java
public void actualizar(double dt) {
    if (detenida) {
        return;
    }
    posicion = posicion + direccion * velocidad * dt;
    if (posicion >= ANCHO) {
        posicion = ANCHO;
        direccion = -1;
    } else if (posicion <= 0) {
        posicion = 0;
        direccion = 1;
    }
}

// El jugador apretó ESPACIO: se frena el cursor y se mide el resultado
public int detener() {
    detenida = true;
    resultado = calidadEn(posicion);
    return resultado;
}

// Calidad que daría un golpe frenado en esa posición
public int calidadEn(double x) {
    double distancia = Math.abs(x - ANCHO / 2);
    if (distancia <= anchoCritico / 2) {
        return Calidad.CRITICO;
    }
    if (distancia <= anchoNormal / 2) {
        return Calidad.NORMAL;
    }
    return Calidad.FALLO;
}
```

- `actualizar(dt)` se llama en cada cuadro del juego. `dt` es el tiempo en segundos desde el cuadro anterior (en este motor siempre 1/120 s). El cursor avanza `direccion * velocidad * dt` unidades. Al llegar a un extremo se lo deja justo en el borde y se invierte la dirección (**rebota**). Si la barra está detenida no hace nada.
- `detener()` es lo que pasa al apretar ESPACIO: marca la barra como detenida y calcula el resultado con `calidadEn(posicion)`.
- `calidadEn(x)` mide la **distancia al centro** (`Math.abs(x - 300)`):
  - Si la distancia es ≤ la mitad del ancho crítico → `CRITICO`.
  - Si es ≤ la mitad del ancho normal → `NORMAL`.
  - Si no → `FALLO`.

  Se compara con la **mitad** del ancho porque cada zona se extiende hacia los dos lados del centro.
- `setPosicion` existe para las pruebas (poner el cursor en un lugar exacto) y valida que no se salga de la barra.

### 6.2 `TipoNodo`, `NodoMapa`

**`TipoNodo`** es un `enum`: una lista cerrada de los tipos de lugar.

```java
public enum TipoNodo {
    INICIO("Inicio"),
    COMBATE("Combate"),
    COFRE("Cofre"),
    EVENTO("Evento"),
    JEFE("Jefe final");

    // Atributo
    private final String nombre;

    // Constructor del enum
    TipoNodo(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }
}
```

Cada valor del `enum` es un objeto que se construye con un texto (`"Inicio"`, `"Combate"`...). `TipoNodo.COMBATE.nombre()` devuelve `"Combate"`. Se usa un `enum` en lugar de números sueltos para que el compilador impida escribir un tipo inexistente.

**`NodoMapa`** es cada lugar del mapa: su `id` (el número de vértice en el grafo), su `tipo`, su `piso` (la columna: 0 es el inicio, 6 el jefe), sus coordenadas `x`, `y` en pantalla y si ya fue `visitado`.

```java
public NodoMapa(int id, TipoNodo tipo, int piso, int x, int y) {
    if (tipo == null) {
        throw new IllegalArgumentException("el nodo necesita un tipo");
    }
    if (piso < 0) {
        throw new IllegalArgumentException("el piso no puede ser negativo: " + piso);
    }
    this.id = id;
    this.tipo = tipo;
    this.piso = piso;
    this.x = x;
    this.y = y;
}
```

El constructor rechaza un tipo `null` y un piso negativo. Los demás métodos son getters. `setVisitado` es el único "setter": es lo único que cambia durante el juego.

### 6.3 `MapaBosque`

**Para qué sirve:** **generar** el grafo del mapa a partir de una semilla.

**Qué es una semilla:** un número que inicializa un `Random`. Con la misma semilla, `Random` produce **siempre la misma secuencia** de números "aleatorios". Por eso `new MapaBosque(1234)` genera siempre el mismo mapa, y por eso **al guardar la partida alcanza con guardar la semilla**.

La forma del mapa:

```
piso 0     piso 1     piso 2     piso 3     piso 4     piso 5     piso 6
                       (cofre)               (cofre)
 INICIO --> 2-3 lugares --> 2-3 lugares --> 2-3 lugares --> 2-3 lugares --> 2-3 lugares --> JEFE
```

```java
public static final int ANCHO = 1024;
public static final int ALTO = 640;
public static final int PISOS = 7;
private static final int MARGEN_X = 90;
private static final int SEPARACION_X = 140;
private static final int ARRIBA = 150;
private static final int ABAJO = 530;

// Atributos
private final long semilla;
private final Grafo<NodoMapa> grafo = new Grafo<>();
private int idInicio;
private int idJefe;
```

- Las constantes definen el tamaño del mapa en pantalla (1024×640), la cantidad de pisos (7) y los márgenes.
- `grafo` es un `Grafo<NodoMapa>`: **el mapa es un grafo dirigido con listas de adyacencia**.
- `semilla` es `final`: una vez creado, el mapa no cambia de semilla.

```java
private void generar() {
    Random azar = new Random(semilla);
    idInicio = grafo.agregarVertice(new NodoMapa(0, TipoNodo.INICIO, 0, MARGEN_X, (ARRIBA + ABAJO) / 2));
    ListaEnlazada<Integer> pisoAnterior = new ListaEnlazada<>();
    pisoAnterior.agregar(idInicio);

    for (int piso = 1; piso < PISOS - 1; piso++) {
        int cantidad = 2 + azar.nextInt(2);
        ListaEnlazada<Integer> pisoActual = new ListaEnlazada<>();
        for (int k = 0; k < cantidad; k++) {
            TipoNodo tipo = elegirTipo(azar, piso, k);
            int x = MARGEN_X + piso * SEPARACION_X;
            int franja = (ABAJO - ARRIBA) / cantidad;
            int y = ARRIBA + franja * k + franja / 2 + azar.nextInt(31) - 15;
            int id = grafo.cantidadVertices();
            pisoActual.agregar(grafo.agregarVertice(new NodoMapa(id, tipo, piso, x, y)));
        }
        conectarPisos(pisoAnterior, pisoActual, azar);
        pisoAnterior = pisoActual;
    }

    int idUltimo = grafo.cantidadVertices();
    NodoMapa jefe = new NodoMapa(idUltimo, TipoNodo.JEFE, PISOS - 1,
            MARGEN_X + (PISOS - 1) * SEPARACION_X, (ARRIBA + ABAJO) / 2);
    idJefe = grafo.agregarVertice(jefe);
    ListaEnlazada<Integer> pisoJefe = new ListaEnlazada<>();
    pisoJefe.agregar(idJefe);
    conectarPisos(pisoAnterior, pisoJefe, azar);
}
```

El método `generar` construye el grafo piso por piso:

1. `Random azar = new Random(semilla)`: el generador de números, con la semilla.
2. Crea el vértice de **inicio** (piso 0, a la izquierda y al medio).
3. Guarda en `pisoAnterior` (una `ListaEnlazada<Integer>`) los ids del piso anterior. Al principio contiene solo el inicio.
4. Para cada piso de 1 a 5:
   - `cantidad = 2 + azar.nextInt(2)`: 2 o 3 lugares (`nextInt(2)` da 0 o 1).
   - Para cada lugar `k`: elige su tipo (`elegirTipo`), calcula su posición (`x` según el piso; `y` repartida en franjas con un pequeño corrimiento al azar entre -15 y +15 para que no quede todo alineado) y lo agrega al grafo con `agregarVertice`. Guarda el id devuelto en `pisoActual`.
   - Llama a `conectarPisos(pisoAnterior, pisoActual, azar)`.
   - Ahora el piso actual pasa a ser el anterior.
5. Finalmente agrega el vértice del **jefe** (piso 6) y lo conecta con el último piso.

```java
private TipoNodo elegirTipo(Random azar, int piso, int k) {
    if (k == 0 && (piso == 2 || piso == 4)) {
        return TipoNodo.COFRE;
    }
    int tirada = azar.nextInt(100);
    if (tirada < 45) {
        return TipoNodo.COMBATE;
    }
    if (tirada < 75) {
        return TipoNodo.EVENTO;
    }
    return TipoNodo.COFRE;
}
```

`azar.nextInt(100)` da un número de 0 a 99: menos de 45 → combate (45%), menos de 75 → evento (30%), el resto → cofre (25%). Además, **en los pisos 2 y 4 el primer lugar es siempre un cofre**: garantiza que el jugador tenga al menos la *posibilidad* de conseguir las habilidades que pide el juego.

```java
private void conectarPisos(ListaEnlazada<Integer> anterior, ListaEnlazada<Integer> siguiente, Random azar) {
    int n = anterior.tamanio();
    int m = siguiente.tamanio();
    // 1) cada lugar del piso anterior se conecta con el lugar "proporcional" del siguiente
    for (int i = 0; i < n; i++) {
        int j = indiceProporcional(i, n, m);
        grafo.agregarArista(anterior.obtener(i), siguiente.obtener(j));
        // 2) a veces, un segundo camino a un vecino
        if (m > 1 && azar.nextInt(100) < 40) {
            int vecino = j + 1;
            if (vecino >= m) {
                vecino = j - 1;
            }
            grafo.agregarArista(anterior.obtener(i), siguiente.obtener(vecino));
        }
    }
    // 3) si algún lugar del siguiente piso quedó sin entrada, se lo conecta con el más cercano
    for (int j = 0; j < m; j++) {
        if (!tieneEntrada(anterior, siguiente.obtener(j))) {
            int i = indiceProporcional(j, m, n);
            grafo.agregarArista(anterior.obtener(i), siguiente.obtener(j));
        }
    }
}
```

Es la parte más delicada: crea las aristas entre dos pisos consecutivos de modo que **ningún lugar quede aislado**.

- `n` lugares en el piso anterior, `m` en el siguiente.
- **Paso 1:** cada lugar del piso anterior se conecta con el lugar "proporcional" del siguiente (`indiceProporcional`). Así los caminos no se cruzan de cualquier manera.
- **Paso 2:** con 40% de probabilidad se agrega un **segundo camino** hacia el vecino más cercano (el de abajo, o el de arriba si ya estaba en el último). `agregarArista` ignora las repetidas, así que no hace falta comprobar nada.
- **Paso 3:** se revisa que **cada lugar del piso siguiente tenga alguna entrada** (`tieneEntrada`). Si alguno quedó sin ninguna, se lo conecta con el lugar proporcional del piso anterior.

Garantía final: **todo lugar (salvo el jefe) tiene al menos una salida** (paso 1) y **todo lugar (salvo el inicio) tiene al menos una entrada** (paso 3). Por eso el jugador nunca queda atrapado y todo se puede alcanzar: `PruebaJuego` lo comprueba con 40 semillas.

```java
private int indiceProporcional(int i, int n, int m) {
    if (n == 1 || m == 1) {
        return m == 1 ? 0 : (m - 1) / 2;
    }
    double ubicacion = (double) i / (n - 1);
    return (int) Math.round(ubicacion * (m - 1));
}
```

Convierte "el lugar `i` de `n`" en "el lugar equivalente de `m`": calcula su ubicación relativa entre 0 y 1 (`i / (n - 1)`) y la multiplica por `m - 1`, redondeando. Si alguno de los dos pisos tiene un solo lugar, se resuelve directamente (el único, o el del medio). El `(double)` antes de `i` fuerza la división **decimal** (con enteros, `1 / 2` daría 0).

### 6.4 `Combate`

**Para qué sirve:** toda la lógica de una pelea por turnos. Es una **máquina de estados**: en cada momento el combate está en un estado y solo ciertas acciones son válidas.

```
          elegirHabilidad (usa barra)           detenerBarra
ELECCION ------------------------> APUNTANDO ------------------> RESULTADO
    ^   \ (sin barra: cura)                                          |   |
    |    \----------------------------------------------------------/   | continuar
    |                                                                   v
    |        continuar            ejecutarTurnoEnemigo         ¿enemigo muerto? --> VICTORIA
    +---- (nueva ronda) <---- TURNO_ENEMIGO <------ continuar    ¿jugador muerto? --> DERROTA
                                                                 escape --> HUIDA
```

```java
public static final int ELECCION = 0;
public static final int APUNTANDO = 1;
public static final int RESULTADO = 2;
public static final int TURNO_ENEMIGO = 3;
public static final int VICTORIA = 4;
public static final int DERROTA = 5;
public static final int HUIDA = 6;

// Quién actúa (los valores que viajan por la cola)
public static final int JUGADOR = 1;
public static final int ENEMIGO = 2;

// Atributos
private final Jugador jugador;
private final Enemigo enemigo;
private final boolean esJefe;
private final ColaEnlazada<Integer> turnos = new ColaEnlazada<>();
private int estado;
private BarraPrecision barra;
private Habilidad habilidadActual;
private String mensaje = "";
private int rondas;
```

- Los estados son constantes enteras: `ELECCION` (el jugador elige habilidad), `APUNTANDO` (se mueve la barra), `RESULTADO` (se muestra lo que pasó), `TURNO_ENEMIGO`, y los tres finales `VICTORIA`, `DERROTA`, `HUIDA`.
- `JUGADOR` y `ENEMIGO` son los valores que viajan por la **cola de turnos**.
- **`turnos` es una `ColaEnlazada<Integer>`**: la cola FIFO que exige la consigna.
- `barra` y `habilidadActual` son los datos de la acción en curso.

```java
public Combate(Jugador jugador, Enemigo enemigo, boolean esJefe) {
    if (jugador == null || enemigo == null) {
        throw new IllegalArgumentException("el combate necesita jugador y enemigo");
    }
    this.jugador = jugador;
    this.enemigo = enemigo;
    this.esJefe = esJefe;
    this.mensaje = "¡" + enemigo.getNombre() + " aparece!";
    prepararSiguienteTurno();
}
```

El constructor valida que haya jugador y enemigo, guarda todo, escribe el mensaje inicial y llama a `prepararSiguienteTurno()` para armar la primera ronda.

```java
private void prepararSiguienteTurno() {
    if (turnos.estaVacia()) {
        rondas++;
        turnos.encolar(JUGADOR);
        for (int i = 0; i < enemigo.getTurnosPorRonda(); i++) {
            turnos.encolar(ENEMIGO);
        }
    }
    if (turnos.frente() == JUGADOR) {
        estado = ELECCION;
    } else {
        estado = TURNO_ENEMIGO;
    }
}
```

**Esto es el uso de la cola:**

1. Si la cola está vacía, se arma una ronda nueva: se encola `JUGADOR` y después `ENEMIGO` tantas veces como actúe el enemigo por ronda (1 normalmente; **2 para el Lobo**).
2. Se mira el **frente** de la cola (`turnos.frente()`) **sin sacarlo**. Si es el jugador, el estado pasa a `ELECCION`; si es el enemigo, a `TURNO_ENEMIGO`.

El turno se **saca** de la cola recién cuando el que actúa **termina** de actuar (`turnos.desencolar()` en `resolver` y en `ejecutarTurnoEnemigo`). Por eso el frente siempre es "quién tiene que actuar ahora".

Ejemplo con el Lobo: la cola es `[J, E, E]`. Actúa el jugador → `[E, E]`. Actúa el lobo → `[E]`. Actúa otra vez → `[]` (vacía) → se arma una ronda nueva `[J, E, E]`.

```java
public boolean elegirHabilidad(int indice) {
    if (estado != ELECCION) {
        return false;
    }
    if (indice < 0 || indice >= jugador.cantidadHabilidades()) {
        return false;
    }
    Habilidad habilidad = jugador.getHabilidad(indice);
    if (!jugador.puedePagar(habilidad.getCostoMana())) {
        mensaje = "No tienes maná suficiente para " + habilidad.getNombre() + ".";
        return false;
    }
    if (habilidad.esEscape() && esJefe) {
        mensaje = "¡No puedes huir de un jefe!";
        return false;
    }
    jugador.gastarMana(habilidad.getCostoMana());
    habilidadActual = habilidad;
    if (habilidad.usaBarra()) {
        barra = new BarraPrecision(jugador, habilidad.getDificultadBarra());
        estado = APUNTANDO;
        mensaje = "¡Frena la barra con ESPACIO!";
    } else {
        resolver(Calidad.NORMAL);
    }
    return true;
}
```

Es lo que pasa cuando el jugador elige una habilidad. Devuelve `true` si la acción se inició.

1. Solo vale si el estado es `ELECCION` (no se puede elegir mientras se apunta, por ejemplo).
2. Valida el índice (que exista esa habilidad en el repertorio).
3. Si no alcanza el maná, avisa en el mensaje y **no cambia nada**.
4. Si es un escape y el combate es contra un jefe, avisa y **no gasta maná**.
5. Si todo está bien: **gasta el maná**, recuerda la habilidad (`habilidadActual`) y:
   - Si usa barra → crea la `BarraPrecision` (con la Destreza y la dificultad de la habilidad) y pasa a `APUNTANDO`.
   - Si no (curación, escape) → se resuelve de inmediato con calidad `NORMAL`.

```java
public void actualizar(double dt) {
    if (estado == APUNTANDO) {
        barra.actualizar(dt);
    }
}
```

```java
public void detenerBarra() {
    if (estado != APUNTANDO) {
        return;
    }
    int calidad = barra.detener();
    resolver(calidad);
}
```

```java
private void resolver(int calidad) {
    mensaje = habilidadActual.aplicar(jugador, enemigo, calidad);
    turnos.desencolar();
    if (habilidadActual.esEscape()) {
        estado = HUIDA;
    } else {
        estado = RESULTADO;
    }
}
```

- `actualizar(dt)`: solo mueve la barra mientras se está apuntando.
- `detenerBarra()`: si se está apuntando, detiene la barra y obtiene la calidad.
- `resolver(calidad)`: **aplica la habilidad** (`habilidadActual.aplicar(...)`, que es polimórfico), guarda el texto que devuelve como mensaje, **saca al jugador de la cola** (`turnos.desencolar()`) y decide el estado: `HUIDA` si la habilidad es un escape; `RESULTADO` en los demás casos.

```java
public void continuar() {
    if (estado == RESULTADO) {
        if (enemigo.estaMuerto()) {
            jugador.cambiarOro(enemigo.getRecompensaOro());
            mensaje = "¡Derrotaste a " + enemigo.getNombre() + "! +" + enemigo.getRecompensaOro() + " de oro.";
            estado = VICTORIA;
        } else if (jugador.estaMuerto()) {
            mensaje = "Caes herido en el sendero...";
            estado = DERROTA;
        } else {
            prepararSiguienteTurno();
        }
    } else if (estado == TURNO_ENEMIGO) {
        ejecutarTurnoEnemigo();
    }
}
```

```java
public void ejecutarTurnoEnemigo() {
    if (estado != TURNO_ENEMIGO) {
        return;
    }
    mensaje = enemigo.atacar(jugador);
    turnos.desencolar();
    estado = RESULTADO;
}
```

- `continuar()` se llama cuando el jugador lee el mensaje y avanza. Según el estado:
  - En `RESULTADO`: si el **enemigo murió** → `VICTORIA` (y se suma el oro de recompensa); si el **jugador murió** → `DERROTA`; si nadie murió → `prepararSiguienteTurno()` (mira quién sigue en la cola).
  - En `TURNO_ENEMIGO`: ejecuta el turno del enemigo.
- `ejecutarTurnoEnemigo()`: llama a `enemigo.atacar(jugador)` (polimórfico: cada enemigo ataca a su modo), **saca al enemigo de la cola** y pasa a `RESULTADO` para mostrar lo ocurrido.

Un detalle importante: la victoria se evalúa en `continuar`, **después** de que el jugador lee el mensaje del golpe final. Así siempre se ve el último golpe antes de ganar.

```java
public ColaEnlazada<Integer> getOrdenDeTurnos() {
    ColaEnlazada<Integer> copia = new ColaEnlazada<>();
    int cantidad = turnos.tamanio();
    for (int i = 0; i < cantidad; i++) {
        int quien = turnos.desencolar();
        copia.encolar(quien);
        turnos.encolar(quien);
    }
    return copia;
}
```

La pantalla quiere **mostrar** los próximos turnos, pero una cola solo deja ver el frente. La solución: se **desencola todo y se vuelve a encolar** en la cola original y en una copia, en el mismo orden. Al terminar, la original quedó igual y la copia sirve para recorrer. `cantidad` se calcula antes del `for` porque el tamaño no cambia (sacamos uno y metemos uno), pero así queda explícito.

### 6.5 Eventos: `EfectoEvento`, `OpcionEvento`, `EventoNarrativo`

Un **evento** (el `?` del mapa y los cofres) es una escena con un relato y 2 o 3 **opciones**. Cada opción tiene una **lista de efectos**. Son tres clases pequeñas que se contienen entre sí:

```
EventoNarrativo  --tiene 2 o 3-->  OpcionEvento  --tiene una lista de-->  EfectoEvento
(título, relato)                   (texto, resultado)                      (tipo, valor)
```

**`EfectoEvento`:** un cambio concreto: `VIDA:+10`, `ORO:-15`, `FUERZA:+1`, `HABILIDAD:CURACION`, `COMBATE:LOBO`.

```java
public enum Tipo {
    VIDA, MANA, ORO, FUERZA, DESTREZA, INTELIGENCIA, HABILIDAD, COMBATE
}

// Atributos
private final Tipo tipo;
private final int valor;
private final String texto;

// Constructor: "valor" para los numéricos, "texto" para HABILIDAD (código) y COMBATE (nombre del enemigo)
public EfectoEvento(Tipo tipo, int valor, String texto) {
    if (tipo == null) {
        throw new IllegalArgumentException("el efecto necesita un tipo");
    }
    if (tipo == Tipo.HABILIDAD && !CatalogoHabilidades.esValido(texto)) {
        throw new IllegalArgumentException("habilidad desconocida: " + texto);
    }
    if (tipo == Tipo.COMBATE && (texto == null || texto.isEmpty())) {
        throw new IllegalArgumentException("el combate necesita el nombre del enemigo");
    }
    this.tipo = tipo;
    this.valor = valor;
    this.texto = texto;
}
```

- El `enum Tipo` anidado lista los ocho efectos posibles.
- `valor` es el número (para los numéricos) y `texto` es el dato de los que no lo son (el código de la habilidad, o el nombre del enemigo de la emboscada).
- El constructor valida: un efecto `HABILIDAD` necesita un código válido del catálogo; un `COMBATE` necesita un nombre de enemigo.

```java
public String aplicar(Jugador jugador) {
    if (tipo == Tipo.VIDA) {
        jugador.cambiarVida(valor);
        return signo(valor) + " vida";
    }
    if (tipo == Tipo.MANA) {
        jugador.cambiarMana(valor);
        return signo(valor) + " maná";
    }
    if (tipo == Tipo.ORO) {
        jugador.cambiarOro(valor);
        return signo(valor) + " oro";
    }
    if (tipo == Tipo.FUERZA || tipo == Tipo.DESTREZA || tipo == Tipo.INTELIGENCIA) {
        return aplicarAtributo(jugador);
    }
    if (tipo == Tipo.HABILIDAD) {
        return aplicarHabilidad(jugador);
    }
    return null;
}
```

```java
private String aplicarAtributo(Jugador jugador) {
    int cual = Jugador.FUERZA;
    String nombre = "Fuerza";
    if (tipo == Tipo.DESTREZA) {
        cual = Jugador.DESTREZA;
        nombre = "Destreza";
    } else if (tipo == Tipo.INTELIGENCIA) {
        cual = Jugador.INTELIGENCIA;
        nombre = "Inteligencia";
    }
    if (valor > 0) {
        jugador.mejorarAtributo(cual, valor);
    }
    return signo(valor) + " " + nombre;
}
```

```java
private String aplicarHabilidad(Jugador jugador) {
    if (jugador.conoceHabilidad(texto)) {
        return "ya conocías esa habilidad";
    }
    boolean aprendida = jugador.aprenderHabilidad(CatalogoHabilidades.crear(texto));
    if (!aprendida) {
        return "no tienes lugar para otra habilidad";
    }
    return "aprendes " + CatalogoHabilidades.crear(texto).getNombre();
}
```

- `aplicar(jugador)` ejecuta el efecto y **devuelve una descripción corta** (`"+5 vida"`, `"-15 oro"`, `"aprendes Luz sanadora"`) que luego se junta con las demás para mostrar el resumen. `COMBATE` no cambia nada al aplicarse (devuelve `null`): la emboscada la dispara la pantalla.
- Los atributos solo se suben si el valor es positivo (en la práctica nunca bajan).
- Para una habilidad: si ya la conoce, avisa; si no hay lugar, avisa; si no, la aprende.

**`OpcionEvento`:** el texto del botón, el relato del resultado y la lista de efectos.

```java
public boolean puedeElegir(Jugador jugador) {
    int costo = 0;
    for (int i = 0; i < efectos.tamanio(); i++) {
        EfectoEvento efecto = efectos.obtener(i);
        if (efecto.getTipo() == EfectoEvento.Tipo.ORO && efecto.getValor() < 0) {
            costo = costo - efecto.getValor();
        }
    }
    return jugador.getOro() >= costo;
}
```

```java
public String aplicar(Jugador jugador) {
    String resumen = "";
    for (int i = 0; i < efectos.tamanio(); i++) {
        String parte = efectos.obtener(i).aplicar(jugador);
        if (parte != null) {
            if (!resumen.isEmpty()) {
                resumen = resumen + ", ";
            }
            resumen = resumen + parte;
        }
    }
    return resumen;
}
```

```java
public String getEmboscada() {
    for (int i = 0; i < efectos.tamanio(); i++) {
        EfectoEvento efecto = efectos.obtener(i);
        if (efecto.getTipo() == EfectoEvento.Tipo.COMBATE) {
            return efecto.getTexto();
        }
    }
    return null;
}
```

- `puedeElegir`: suma todos los costos de oro de la opción (los efectos `ORO` con valor negativo) y comprueba que el jugador los pueda pagar. Así una opción "Comprar un elixir (15 oro)" aparece en gris si no hay oro.
- `aplicar`: aplica cada efecto en orden y arma el **resumen** uniendo las descripciones con coma (`"+5 vida, -2 maná"`). Esto es una concatenación manual de `String` (no `StringBuilder`, para mantener el código simple: con 2 o 3 efectos no importa).
- `getEmboscada`: recorre los efectos buscando uno de tipo `COMBATE` y devuelve el nombre del enemigo, o `null`.

**`EventoNarrativo`:** título, relato y la **lista enlazada** de opciones.

```java
public EventoNarrativo(String titulo, String relato, ListaEnlazada<OpcionEvento> opciones) {
    if (titulo == null || titulo.isEmpty()) {
        throw new IllegalArgumentException("el evento necesita un título");
    }
    if (opciones == null || opciones.tamanio() < MIN_OPCIONES || opciones.tamanio() > MAX_OPCIONES) {
        throw new IllegalArgumentException("un evento necesita entre 2 y 3 opciones");
    }
    this.titulo = titulo;
    this.relato = relato == null ? "" : relato;
    this.opciones = opciones;
}
```

El constructor exige **2 o 3 opciones** (`MIN_OPCIONES` y `MAX_OPCIONES`), tal como pide el enunciado. Si el archivo de eventos trae uno con 1 o con 4 opciones, esta validación lanza `IllegalArgumentException` y el cargador lo descarta.

### 6.6 `Registro` y `Partida`

**`Registro`** es una línea del diario: el id del nodo donde ocurrió y un texto. Es **inmutable** (sus atributos son `final`, no hay setters): una vez escrito no cambia, como en un diario real.

**`Partida`** es el corazón del estado del juego: reúne el mapa, el héroe y las **dos pilas**.

```java
public class Partida {

    // Atributos
    private final long semilla;
    private final MapaBosque mapa;
    private final Jugador jugador;
    private final PilaEnlazada<Integer> ruta;
    private final PilaEnlazada<Registro> diario;
    private final ListaEnlazada<EventoNarrativo> eventos;
    private int derrotados;
```

- **`ruta`: `PilaEnlazada<Integer>`**: los ids de los nodos que el héroe visitó. **El tope es el lugar actual.**
- **`diario`: `PilaEnlazada<Registro>`**: lo que fue pasando. **El tope es lo más reciente.**
- `eventos` es la lista de eventos disponibles (cargados de `eventos.txt`).
- La partida **no guarda el nodo actual aparte**: es `ruta.tope()`. Así hay una sola fuente de verdad (si hubiera dos, podrían contradecirse).

```java
public Partida(long semilla, Jugador jugador, PilaEnlazada<Integer> ruta, PilaEnlazada<Registro> diario,
               int derrotados, ListaEnlazada<EventoNarrativo> eventos) {
    if (jugador == null || ruta == null || diario == null || eventos == null) {
        throw new IllegalArgumentException("faltan datos para crear la partida");
    }
    if (ruta.estaVacia()) {
        throw new IllegalArgumentException("la ruta debe tener al menos el nodo de inicio");
    }
    if (derrotados < 0) {
        throw new IllegalArgumentException("derrotados no puede ser negativo: " + derrotados);
    }
    this.semilla = semilla;
    this.mapa = new MapaBosque(semilla);
    this.jugador = jugador;
    this.ruta = ruta;
    this.diario = diario;
    this.derrotados = derrotados;
    this.eventos = eventos;
    marcarVisitados();
}
```

```java
public static Partida nueva(long semilla, ListaEnlazada<EventoNarrativo> eventos) {
    MapaBosque mapaTemporal = new MapaBosque(semilla);
    PilaEnlazada<Integer> ruta = new PilaEnlazada<>();
    ruta.apilar(mapaTemporal.getIdInicio());
    PilaEnlazada<Registro> diario = new PilaEnlazada<>();
    diario.apilar(new Registro(mapaTemporal.getIdInicio(), "Entras al Sendero Oscuro."));
    return new Partida(semilla, new Jugador(), ruta, diario, 0, eventos);
}
```

- El constructor valida que nada sea `null`, que la ruta no esté vacía (siempre hay un lugar actual) y que `derrotados` no sea negativo. Después **genera el mapa con la semilla** (`new MapaBosque(semilla)`) y marca los nodos visitados.
- `nueva(semilla, eventos)` arma una partida desde cero: la ruta con solo el nodo de inicio, el diario con "Entras al Sendero Oscuro." y un `Jugador` nuevo. (Crea un mapa provisorio solo para saber el id del inicio; es barato.)

```java
private void marcarVisitados() {
    PilaEnlazada<Integer> auxiliar = new PilaEnlazada<>();
    while (!ruta.estaVacia()) {
        int id = ruta.desapilar();
        mapa.getNodo(id).setVisitado(true);
        auxiliar.apilar(id);
    }
    while (!auxiliar.estaVacia()) {
        ruta.apilar(auxiliar.desapilar());
    }
}
```

**Cómo recorrer una pila sin destruirla** (una pila solo deja ver el tope):

1. Se **desapila todo** pasando cada elemento a una pila **auxiliar**, y de paso se hace lo que haga falta con cada uno (acá, marcar el nodo como visitado).
2. Se **desapila la auxiliar** devolviendo cada elemento a la pila original. Como pasar de una pila a otra **invierte** el orden, hacerlo dos veces lo deja como estaba.

Este patrón de "pila auxiliar" se repite en `getUltimosRegistros`, `getDiarioCompleto` y `getRuta`. Es la respuesta a "¿cómo mirás adentro de una pila?".

```java
public int getNodoActual() {
    return ruta.tope();
}
```

```java
public ListaEnlazada<Integer> getAdyacentes() {
    return mapa.getGrafo().idsAdyacentes(getNodoActual());
}

public boolean puedeMoverA(int id) {
    if (id < 0 || id >= mapa.cantidadNodos()) {
        return false;
    }
    return mapa.getGrafo().existeArista(getNodoActual(), id);
}

public void moverA(int id) {
    if (!puedeMoverA(id)) {
        throw new IllegalArgumentException("no hay camino desde el nodo " + getNodoActual() + " al " + id);
    }
    NodoMapa destino = mapa.getNodo(id);
    destino.setVisitado(true);
    ruta.apilar(id);
    registrar(id, "Piso " + destino.getPiso() + ": llegas a " + destino.getTipo().nombre() + ".");
}
```

- `getNodoActual()` es `ruta.tope()`.
- `getAdyacentes()` le pide al **grafo** los vecinos del lugar actual: son los **únicos destinos permitidos**.
- `puedeMoverA(id)` valida que el id exista y que haya una **arista** del lugar actual a ese id.
- `moverA(id)`: si no se puede, lanza `IllegalArgumentException`. Si se puede: marca el destino como visitado, **apila el id en la ruta** y **apila una línea en el diario**.

Así el mapa **no deja saltarse caminos**: la regla del grafo se aplica en la lógica, no solo en la pantalla.

```java
public ListaEnlazada<Registro> getUltimosRegistros(int cantidad) {
    ListaEnlazada<Registro> resultado = new ListaEnlazada<>();
    PilaEnlazada<Registro> auxiliar = new PilaEnlazada<>();
    while (!diario.estaVacia() && resultado.tamanio() < cantidad) {
        Registro registro = diario.desapilar();
        resultado.agregar(registro);
        auxiliar.apilar(registro);
    }
    while (!auxiliar.estaVacia()) {
        diario.apilar(auxiliar.desapilar());
    }
    return resultado;
}
```

Devuelve los últimos `cantidad` registros del **más reciente al más antiguo**. Mientras haya registros y no se llegó a la cantidad pedida: se desapila uno, se lo agrega al resultado **y** a la pila auxiliar. Después se devuelven todos a la pila original. El diario queda intacto. (La pantalla del diario usa esto para mostrar las últimas 8 entradas.)

```java
public ListaEnlazada<Registro> getDiarioCompleto() {
    ListaEnlazada<Registro> resultado = new ListaEnlazada<>();
    PilaEnlazada<Registro> auxiliar = new PilaEnlazada<>();
    while (!diario.estaVacia()) {
        auxiliar.apilar(diario.desapilar());
    }
    while (!auxiliar.estaVacia()) {
        Registro registro = auxiliar.desapilar();
        resultado.agregar(registro);
        diario.apilar(registro);
    }
    return resultado;
}
```

Devuelve **todo el diario del más antiguo al más nuevo**: primero se pasa toda la pila a la auxiliar (que la invierte, y queda el más antiguo arriba) y luego se la vacía en una lista mientras se devuelve cada elemento a la pila original. Así sale en el orden correcto para escribirlo en el archivo (y al cargar, apilando en ese orden, el último queda arriba). `getRuta()` es idéntico, para los ids.

```java
public Enemigo crearEnemigoDeNodo(NodoMapa nodo) {
    if (nodo.getTipo() == TipoNodo.JEFE) {
        return FabricaEnemigos.crearJefe();
    }
    Random azar = new Random(semilla * 1000 + nodo.getId());
    return FabricaEnemigos.crearAleatorio(nodo.getPiso(), azar);
}
```

```java
public EventoNarrativo crearEventoDeNodo(NodoMapa nodo) {
    if (eventos.estaVacia()) {
        return crearEventoCofre();
    }
    Random azar = new Random(semilla * 7919 + nodo.getId());
    return eventos.obtener(azar.nextInt(eventos.tamanio()));
}
```

**El mismo lugar siempre contiene lo mismo.** El enemigo (o evento) de un nodo se calcula con una semilla propia (`semilla * 1000 + id`). Si el jugador carga la partida, el nodo tiene exactamente el mismo enemigo: no sirve para "tirar de nuevo". En el nodo jefe siempre está el Rey Oscuro.

```java
public EventoNarrativo crearEventoCofre() {
    ListaEnlazada<OpcionEvento> opciones = new ListaEnlazada<>();

    ListaEnlazada<EfectoEvento> pergamino = new ListaEnlazada<>();
    String proxima = jugador.proximaHabilidadPorAprender();
    if (proxima != null) {
        pergamino.agregar(new EfectoEvento(EfectoEvento.Tipo.HABILIDAD, 0, proxima));
    } else {
        pergamino.agregar(new EfectoEvento(EfectoEvento.Tipo.INTELIGENCIA, 1, null));
    }
    opciones.agregar(new OpcionEvento("Tomar el pergamino sellado",
            "Rompes el sello y las runas se graban en tu mente.", pergamino));

    ListaEnlazada<EfectoEvento> monedas = new ListaEnlazada<>();
    monedas.agregar(new EfectoEvento(EfectoEvento.Tipo.ORO, 20, null));
    opciones.agregar(new OpcionEvento("Llevarte las monedas",
            "Las monedas tintinean en tu bolsa.", monedas));

    ListaEnlazada<EfectoEvento> pocion = new ListaEnlazada<>();
    pocion.agregar(new EfectoEvento(EfectoEvento.Tipo.VIDA, 12, null));
    pocion.agregar(new EfectoEvento(EfectoEvento.Tipo.MANA, 5, null));
    opciones.agregar(new OpcionEvento("Beber la poción azul",
            "Un calor agradable recorre tu cuerpo.", pocion));

    return new EventoNarrativo("Un cofre olvidado",
            "Entre las raíces de un roble hay un cofre de hierro entreabierto. Dentro ves tres cosas.", opciones);
}
```

El **cofre** no sale del archivo de eventos: se arma en el código, porque su primera opción **depende del estado del héroe**: enseña la próxima habilidad del catálogo que le falta (`proximaHabilidadPorAprender`), o, si ya las sabe todas, da +1 de Inteligencia. Las otras dos opciones son fijas (20 de oro; una poción que da vida y maná). Ver cómo se arma la lista de opciones con `agregar`: **todo evento es una lista enlazada de opciones, cada una con una lista enlazada de efectos**.

```java
public String sumarDerrotado() {
    derrotados++;
    if (derrotados % 2 != 0) {
        return null;
    }
    int cual = (derrotados / 2 - 1) % 3;
    jugador.mejorarAtributo(cual, 1);
    if (cual == Jugador.FUERZA) {
        return "Te sientes más fuerte: +1 Fuerza.";
    }
    if (cual == Jugador.DESTREZA) {
        return "Tus reflejos mejoran: +1 Destreza.";
    }
    return "Tu mente se agudiza: +1 Inteligencia.";
}
```

**El héroe crece.** Cada enemigo derrotado se cuenta; **cada 2** se mejora un atributo, rotando: 2.º → Fuerza, 4.º → Destreza, 6.º → Inteligencia, 8.º → Fuerza... La cuenta es `(derrotados / 2 - 1) % 3`: con 2 derrotados `2/2 - 1 = 0` (Fuerza), con 4 `= 1` (Destreza), con 6 `= 2` (Inteligencia), con 8 `= 3 % 3 = 0` (Fuerza otra vez). Devuelve el mensaje de la mejora o `null` si todavía no toca.

---

## 7. Paquete `juego.archivos`: `GestorArchivos`

**Para qué sirve:** toda la **persistencia**: guardar y cargar la partida (`guardado.txt`) y leer los eventos (`eventos.txt`). Usa las clases de `java.io` que pide la cátedra: `PrintWriter` para escribir y `BufferedReader` para leer.

**Principio de diseño:** que falte o esté dañado un archivo **no debe romper el juego**. Es una situación esperable, no un error del programa. Por eso `cargar` devuelve `null` en lugar de lanzar excepciones hacia afuera.

### 7.1 Atributos y rutas

```java
private static final String VERSION = "1";
private static final String NOMBRE_GUARDADO = "guardado.txt";
private static final String NOMBRE_EVENTOS = "eventos.txt";

// Atributos
private final String carpeta;

// Constructor
public GestorArchivos(String carpeta) {
    if (carpeta == null || carpeta.isEmpty()) {
        throw new IllegalArgumentException("la carpeta no puede estar vacía");
    }
    this.carpeta = carpeta;
}

// Rutas
public String rutaGuardado() {
    return carpeta + File.separator + NOMBRE_GUARDADO;
}

public String rutaEventos() {
    return carpeta + File.separator + NOMBRE_EVENTOS;
}
```

- `VERSION = "1"` se escribe en la primera línea del guardado. Si en el futuro el formato cambia, se sube el número y los guardados viejos se detectan.
- `carpeta` es la carpeta donde viven los archivos (`datos`). El constructor rechaza una carpeta vacía.
- `File.separator` es la barra del sistema operativo (`/` en Linux, `\` en Windows). Así las rutas funcionan en ambos.

```java
public boolean existeGuardado() {
    return new File(rutaGuardado()).isFile();
}
```

```java
public boolean borrarGuardado() {
    File archivo = new File(rutaGuardado());
    return archivo.isFile() && archivo.delete();
}
```

- `File` representa una ruta del disco (no abre nada). `isFile()` es `true` si existe y es un archivo.
- `borrarGuardado` se usa cuando el héroe muere o gana: **en un roguelike, la partida termina** y no se puede reanudar. Devuelve `true` solo si había archivo y se pudo borrar.

### 7.2 Guardar

```java
public boolean guardar(Partida partida) {
    new File(carpeta).mkdirs();
    try (PrintWriter salida = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaGuardado()), "UTF-8"))) {
        Jugador j = partida.getJugador();
        salida.println("VERSION=" + VERSION);
        salida.println("SEMILLA=" + partida.getSemilla());
        salida.println("FUERZA=" + j.getFuerza());
        salida.println("DESTREZA=" + j.getDestreza());
        salida.println("INTELIGENCIA=" + j.getInteligencia());
        salida.println("VIDA=" + j.getVida());
        salida.println("MANA=" + j.getMana());
        salida.println("ORO=" + j.getOro());
        salida.println("DERROTADOS=" + partida.getDerrotados());
        salida.println("NODO_ACTUAL=" + partida.getNodoActual());
        salida.println("HABILIDADES=" + textoHabilidades(j));
        salida.println("RUTA=" + textoRuta(partida.getRuta()));
        ListaEnlazada<Registro> diario = partida.getDiarioCompleto();
        for (int i = 0; i < diario.tamanio(); i++) {
            Registro r = diario.obtener(i);
            salida.println("REGISTRO=" + r.getIdNodo() + "|" + limpiar(r.getTexto()));
        }
        return !salida.checkError();
    } catch (IOException e) {
        return false;
    }
}
```

Paso a paso:

1. `new File(carpeta).mkdirs()`: crea la carpeta si no existe (no falla si ya está).
2. `try (PrintWriter salida = ...)`: **try-with-resources**. Lo que se declara entre paréntesis se **cierra solo** al terminar el bloque, aunque haya una excepción. Si no se cierra, el archivo puede quedar a medias.
3. La cadena `new PrintWriter(new OutputStreamWriter(new FileOutputStream(ruta), "UTF-8"))` es una **cadena de adaptadores**: `FileOutputStream` escribe bytes en el archivo; `OutputStreamWriter` los convierte desde texto con la codificación UTF-8 (para que las tildes y la `ñ` se guarden bien en cualquier computadora); `PrintWriter` agrega `println`.
4. Cada `salida.println("CLAVE=" + valor)` escribe **una línea**: formato `CLAVE=valor`. Es texto plano y se puede abrir con el bloc de notas.
5. El diario se escribe con un `for` sobre `getDiarioCompleto()` (del más antiguo al más nuevo), una línea `REGISTRO=id|texto` cada uno. `limpiar` reemplaza saltos de línea del texto por espacios: un salto de línea rompería el formato "una línea por dato".
6. `return !salida.checkError()`: `PrintWriter` **no lanza** excepciones al escribir; guarda un indicador de error que se consulta con `checkError()`. Si hubo error devolvemos `false`.
7. `catch (IOException e)`: si ni siquiera se pudo abrir el archivo, también `false`.

**No se guarda el grafo**: se guarda la **semilla** y el mapa se regenera igual.

Ejemplo del archivo que produce:

```
VERSION=1
SEMILLA=2024
FUERZA=5
...
HABILIDADES=ATAQUE,CURACION
RUTA=0,2,6
REGISTRO=0|Entras al Sendero Oscuro.
```

### 7.3 Cargar

```java
public Partida cargar(ListaEnlazada<EventoNarrativo> eventos) {
    ListaEnlazada<String> lineas = leerLineas(rutaGuardado());
    if (lineas == null) {
        return null;
    }
    try {
        if (!VERSION.equals(valorDe(lineas, "VERSION"))) {
            return null;
        }
        long semilla = Long.parseLong(valorDe(lineas, "SEMILLA"));
        int fuerza = Integer.parseInt(valorDe(lineas, "FUERZA"));
        int destreza = Integer.parseInt(valorDe(lineas, "DESTREZA"));
        int inteligencia = Integer.parseInt(valorDe(lineas, "INTELIGENCIA"));
        int vida = Integer.parseInt(valorDe(lineas, "VIDA"));
        int mana = Integer.parseInt(valorDe(lineas, "MANA"));
        int oro = Integer.parseInt(valorDe(lineas, "ORO"));
        int derrotados = Integer.parseInt(valorDe(lineas, "DERROTADOS"));
        int nodoActual = Integer.parseInt(valorDe(lineas, "NODO_ACTUAL"));
        if (vida <= 0) {
            return null;
        }

        Jugador jugador = new Jugador(fuerza, destreza, inteligencia, vida, mana, oro);
        if (!cargarHabilidades(jugador, valorDe(lineas, "HABILIDADES"))) {
            return null;
        }
        PilaEnlazada<Integer> ruta = leerRuta(valorDe(lineas, "RUTA"), semilla);
        if (ruta == null || ruta.tope() != nodoActual) {
            return null;
        }
        PilaEnlazada<Registro> diario = leerDiario(lineas);
        return new Partida(semilla, jugador, ruta, diario, derrotados, eventos);
    } catch (RuntimeException e) {
        // NumberFormatException (texto en lugar de número), IllegalArgumentException (valores imposibles),
        // NullPointerException (falta una clave)... todo significa "archivo dañado".
        return null;
    }
}
```

1. `leerLineas` devuelve **todas las líneas** en una `ListaEnlazada<String>` o `null` si no se pudo leer. Si es `null`, `cargar` devuelve `null`.
2. Dentro del `try`, para cada dato se pide su valor con `valorDe(lineas, "CLAVE")` y se lo convierte con `Integer.parseInt` o `Long.parseLong`. **Si el texto no es un número** (por ejemplo `FUERZA=abc`) esas funciones lanzan `NumberFormatException`.
3. Se reconstruye el `Jugador` con el constructor de carga, se le cargan las habilidades, se reconstruye la ruta (como pila) y el diario (como pila) y se crea la `Partida`.
4. **`catch (RuntimeException e) { return null; }`**: cualquier error inesperado al interpretar los datos significa que el archivo está dañado. `NumberFormatException` (número mal escrito), `IllegalArgumentException` (valores imposibles, como Fuerza 0, que lanza el constructor de `Jugador`) y `NullPointerException` (falta una clave: `valorDe` devuelve `null`) son todas `RuntimeException`.
5. Validaciones extra: `vida <= 0` (una partida guardada de un muerto), la versión, que la ruta termine en el nodo actual.

```java
private String valorDe(ListaEnlazada<String> lineas, String clave) {
    String prefijo = clave + "=";
    for (int i = 0; i < lineas.tamanio(); i++) {
        String linea = lineas.obtener(i);
        if (linea.startsWith(prefijo)) {
            return linea.substring(prefijo.length()).trim();
        }
    }
    return null;
}
```

```java
private boolean cargarHabilidades(Jugador jugador, String texto) {
    Tokenizador t = new Tokenizador(texto, ',');
    while (t.hayMas()) {
        String codigo = t.siguiente().trim();
        if (!CatalogoHabilidades.esValido(codigo)) {
            return false;
        }
        if (!jugador.aprenderHabilidad(CatalogoHabilidades.crear(codigo))) {
            return false;
        }
    }
    // Todo héroe sabe atacar: un guardado sin ATAQUE fue modificado a mano
    return jugador.conoceHabilidad("ATAQUE");
}
```

- `valorDe(lineas, clave)`: busca la primera línea que empiece con `"CLAVE="` y devuelve lo que sigue (`substring` desde después del `=`), sin espacios sobrantes (`trim`). Si no la encuentra, devuelve `null`.
- `cargarHabilidades`: usa el `Tokenizador` para partir `"ATAQUE,CURACION"` por comas. Rechaza códigos inventados (`esValido`) y repertorios que no incluyan `ATAQUE` (**todo héroe sabe atacar**: si falta, el archivo fue modificado a mano).

```java
private PilaEnlazada<Integer> leerRuta(String texto, long semilla) {
    MapaBosque mapa = new MapaBosque(semilla);
    PilaEnlazada<Integer> ruta = new PilaEnlazada<>();
    Tokenizador t = new Tokenizador(texto, ',');
    int anterior = -1;
    while (t.hayMas()) {
        int id = Integer.parseInt(t.siguiente().trim());
        if (anterior == -1) {
            if (id != mapa.getIdInicio()) {
                return null;
            }
        } else if (!mapa.getGrafo().existeArista(anterior, id)) {
            return null;
        }
        ruta.apilar(id);
        anterior = id;
    }
    if (ruta.estaVacia()) {
        return null;
    }
    return ruta;
}
```

**La validación más importante.** Como el mapa se regenera con la semilla, se comprueba que la ruta guardada sea un **camino real del grafo**:

- El primer id debe ser el nodo de inicio.
- Cada id siguiente debe tener una **arista** desde el anterior (`existeArista`).

Si el archivo fue editado y la ruta salta de un lugar a otro sin camino, devuelve `null`. Al ir **apilando** los ids en orden, el último queda en el **tope**, que es el lugar actual.

```java
private PilaEnlazada<Registro> leerDiario(ListaEnlazada<String> lineas) {
    PilaEnlazada<Registro> diario = new PilaEnlazada<>();
    String prefijo = "REGISTRO=";
    for (int i = 0; i < lineas.tamanio(); i++) {
        String linea = lineas.obtener(i);
        if (linea.startsWith(prefijo)) {
            String resto = linea.substring(prefijo.length());
            int barra = resto.indexOf('|');
            if (barra > 0) {
                int id = Integer.parseInt(resto.substring(0, barra).trim());
                diario.apilar(new Registro(id, resto.substring(barra + 1)));
            }
        }
    }
    return diario;
}
```

Para cada línea `REGISTRO=id|texto`: se busca la **primera** barra `|` con `indexOf` (así el texto puede contener más barras sin problema), se separa el id y el texto, y se **apila** el registro. Como el archivo viene del más antiguo al más nuevo, el último queda arriba.

### 7.4 Leer los eventos (`eventos.txt`)

```java
public ListaEnlazada<EventoNarrativo> cargarEventos() {
    ListaEnlazada<String> lineas = leerLineas(rutaEventos());
    ListaEnlazada<EventoNarrativo> eventos = new ListaEnlazada<>();
    if (lineas != null) {
        eventos = interpretarEventos(lineas);
    }
    if (eventos.estaVacia()) {
        eventos = interpretarEventos(textoDeRespaldo());
    }
    return eventos;
}
```

Lee el archivo; si falta, o si no quedó ningún evento válido, usa **los eventos de respaldo** (dos eventos escritos en el propio código). El juego siempre tiene eventos.

```java
public ListaEnlazada<EventoNarrativo> interpretarEventos(ListaEnlazada<String> lineas) {
    ListaEnlazada<EventoNarrativo> resultado = new ListaEnlazada<>();
    String titulo = null;
    String relato = "";
    ListaEnlazada<OpcionEvento> opciones = new ListaEnlazada<>();
    boolean valido = true;

    for (int i = 0; i < lineas.tamanio(); i++) {
        String linea = lineas.obtener(i).trim();
        if (linea.isEmpty() || linea.startsWith("#")) {
            continue;
        }
        Tokenizador t = new Tokenizador(linea, '|');
        String tipo = t.siguiente();
        if (tipo.equals("EVENTO")) {
            cerrarEvento(resultado, titulo, relato, opciones, valido);
            titulo = t.hayMas() ? t.siguiente() : "";
            relato = t.hayMas() ? t.siguiente() : "";
            opciones = new ListaEnlazada<>();
            valido = true;
        } else if (tipo.equals("OPCION") && titulo != null) {
            try {
                String texto = t.siguiente();
                String efectos = t.hayMas() ? t.siguiente() : "";
                String textoResultado = t.hayMas() ? t.siguiente() : "";
                opciones.agregar(new OpcionEvento(texto, textoResultado, interpretarEfectos(efectos)));
            } catch (RuntimeException e) {
                valido = false;
            }
        }
    }
    cerrarEvento(resultado, titulo, relato, opciones, valido);
    return resultado;
}
```

Es una pequeña **máquina de lectura**. Recorre las líneas una por una:

- Las líneas vacías y las que empiezan con `#` (comentarios) se saltan con `continue`.
- Cada línea se parte con un `Tokenizador` por `|` y el primer trozo dice qué es.
- **`EVENTO`**: cierra el evento anterior (lo agrega a la lista si está completo y es válido) y empieza uno nuevo con su título y relato.
- **`OPCION`**: crea una opción y la agrega al evento actual. Está dentro de un `try/catch (RuntimeException)`: si la línea está mal escrita (un efecto inexistente, un número con letras, faltan trozos), `valido` pasa a `false` y **ese evento completo se descarta**, pero los demás se cargan igual.
- Al final se cierra el último evento.

```java
private void cerrarEvento(ListaEnlazada<EventoNarrativo> destino, String titulo, String relato,
                          ListaEnlazada<OpcionEvento> opciones, boolean valido) {
    if (titulo == null || !valido) {
        return;
    }
    try {
        destino.agregar(new EventoNarrativo(titulo, relato, opciones));
    } catch (IllegalArgumentException e) {
        // evento con título vacío o con menos de 2 / más de 3 opciones: se descarta
    }
}
```

```java
public ListaEnlazada<EfectoEvento> interpretarEfectos(String texto) {
    ListaEnlazada<EfectoEvento> efectos = new ListaEnlazada<>();
    Tokenizador porEfecto = new Tokenizador(texto, ';');
    while (porEfecto.hayMas()) {
        String par = porEfecto.siguiente().trim();
        if (par.isEmpty()) {
            continue;
        }
        Tokenizador partes = new Tokenizador(par, ':');
        EfectoEvento.Tipo tipo = EfectoEvento.Tipo.valueOf(partes.siguiente().trim());
        String dato = partes.siguiente().trim();
        if (tipo == EfectoEvento.Tipo.HABILIDAD || tipo == EfectoEvento.Tipo.COMBATE) {
            efectos.agregar(new EfectoEvento(tipo, 0, dato));
        } else {
            efectos.agregar(new EfectoEvento(tipo, Integer.parseInt(dato), null));
        }
    }
    return efectos;
}
```

- `cerrarEvento`: si hay título y fue válido, intenta crear el `EventoNarrativo`. Si tiene menos de 2 o más de 3 opciones, su constructor lanza `IllegalArgumentException` y **se descarta** (el `catch` está vacío a propósito, con un comentario que lo explica).
- `interpretarEfectos("VIDA:-3;ORO:10")`: parte por `;` en efectos y cada efecto por `:`. `EfectoEvento.Tipo.valueOf("VIDA")` convierte el texto en el valor del `enum` y **lanza `IllegalArgumentException` si no existe** (`"FUEGO"`). `Integer.parseInt("-3")` convierte el número (admite negativos). Los efectos `HABILIDAD` y `COMBATE` traen un texto en vez de número.

**Ventaja de tener los eventos en un archivo:** agregar o cambiar un evento **no requiere recompilar**. Es la diferencia entre *datos* y *código*.

### 7.5 Leer un archivo de texto

```java
public ListaEnlazada<String> leerLineas(String ruta) {
    File archivo = new File(ruta);
    if (!archivo.isFile()) {
        return null;
    }
    ListaEnlazada<String> lineas = new ListaEnlazada<>();
    try (BufferedReader entrada = new BufferedReader(
            new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
        String linea = entrada.readLine();
        while (linea != null) {
            lineas.agregar(linea);
            linea = entrada.readLine();
        }
        return lineas;
    } catch (IOException e) {
        return null;
    }
}
```

- `try-with-resources` otra vez: el `BufferedReader` se cierra solo.
- `new InputStreamReader(new FileInputStream(archivo), "UTF-8")` es el equivalente de lectura: bytes → texto en UTF-8.
- `BufferedReader` lee con un buffer (más rápido) y ofrece `readLine()`, que devuelve **una línea** y **`null` al terminar el archivo**. El `while (linea != null)` es el patrón estándar de lectura.
- Se agrega cada línea a una `ListaEnlazada<String>`.
- Si no hay archivo o falla la lectura devuelve `null`.

---

## 8. Paquete `juego.graficos`

Todo el dibujo **sin archivos externos**. Se usa `Graphics2D` de Java: rectángulos, óvalos, líneas, degradados y texto.

### 8.1 `Paleta`

Una letra = un color. Es lo que permite dibujar sprites con **texto**.

```java
public static Color deLetra(char letra) {
    switch (letra) {
        case 'K': return NEGRO;
        case 'W': return HUESO;
        case 'w': return new Color(170, 160, 140);
```

- Los colores con nombre (`Paleta.ORO`, `Paleta.PERGAMINO`...) son constantes `public static final`, usadas en todas las pantallas para que el aspecto sea **coherente**: verdes bosque, gris piedra, marrón madera, dorado, pergamino y negro, como pedía el enunciado.
- `deLetra(char)` devuelve el color de una letra con un `switch`. Para el punto `.` (y cualquier otro carácter) devuelve `null`, que significa **transparente**.

### 8.2 `Sprite`

Un sprite es un dibujo de 16×16 píxeles escrito como **16 filas de 16 letras**.

```java
public Sprite(String texto) {
    if (texto == null || texto.isEmpty()) {
        throw new IllegalArgumentException("el sprite necesita al menos una fila");
    }
    Tokenizador t = new Tokenizador(texto, '/');
    while (t.hayMas()) {
        filas.agregar(t.siguiente());
    }
    this.alto = filas.tamanio();
    this.ancho = filas.obtener(0).length();
    for (int i = 0; i < alto; i++) {
        if (filas.obtener(i).length() != ancho) {
            throw new IllegalArgumentException("la fila " + i + " mide " + filas.obtener(i).length()
                    + " y debería medir " + ancho);
        }
    }
}
```

```java
public BufferedImage crearImagen(int escala) {
    if (escala < 1) {
        throw new IllegalArgumentException("la escala debe ser al menos 1: " + escala);
    }
    BufferedImage imagen = new BufferedImage(ancho * escala, alto * escala, BufferedImage.TYPE_INT_ARGB);
    for (int fila = 0; fila < alto; fila++) {
        String texto = filas.obtener(fila);
        for (int col = 0; col < ancho; col++) {
            Color color = Paleta.deLetra(texto.charAt(col));
            if (color != null) {
                int argb = color.getRGB();
                for (int dy = 0; dy < escala; dy++) {
                    for (int dx = 0; dx < escala; dx++) {
                        imagen.setRGB(col * escala + dx, fila * escala + dy, argb);
                    }
                }
            }
        }
    }
    return imagen;
}
```

- El constructor recibe todo el dibujo en **un solo `String`** con las filas separadas por `/`. Lo parte con el `Tokenizador` (**sin `split`, sin arreglos**) y guarda las filas en una `ListaEnlazada<String>`. Valida que **todas las filas midan lo mismo** (`IllegalArgumentException` si no).
- `crearImagen(escala)` crea una `BufferedImage` (una imagen en memoria) de `ancho × escala` por `alto × escala` y recorre cada letra: pide su color a la paleta y pinta un **cuadrado** de `escala × escala` píxeles. Como no se suaviza nada, queda el aspecto **pixel-art** nítido.
- `TYPE_INT_ARGB` significa que cada píxel tiene componentes **A**lfa (transparencia), **R**ojo, **G**reen y **B**lue. El píxel transparente simplemente no se pinta.

### 8.3 `CatalogoSprites`

Contiene los **15 dibujos** del juego (héroe, 4 enemigos, 5 íconos del mapa, altar, árbol, antorcha, corazón y poción). Cada uno es una constante `String`. Por ejemplo, el cofre:

```java
private static final String DIBUJO_COFRE =
        "................/" +
        "................/" +
        "....KKKKKKKK..../" +
        "..KKBBBBBBBBKK../" +
        ".KBbbbbbbbbbbBK./" +
        ".KBbbbbbbbbbbBK./" +
        ".KYYYYYYYYYYYYK./" +
        ".KBBBBBYYBBBBBK./" +
        ".KBbbbbYKYbbbBK./" +
        ".KBbbbbYYYbbbBK./" +
        ".KBbbbbbbbbbbBK./" +
        ".KBbbbbbbbbbbBK./" +
        ".KNBBBBBBBBBBNK./" +
        "..KKKKKKKKKKKK../" +
        "................/" +
        "................";
```

Cada letra es un color de la paleta (`K` negro, `B` marrón, `b` marrón claro, `Y` dorado...) y `.` es transparente. Se leen como una imagen: de arriba hacia abajo.

Además tiene:

- Constantes con el nombre de cada sprite (`CatalogoSprites.JUGADOR = "jugador"`), para no escribir textos sueltos en el resto del código.
- `existe(nombre)` y `nombrePorOrden(i)`.
- `proceduralDe(nombre)`: devuelve el `Sprite` armado desde el dibujo de texto; lanza `IllegalArgumentException` si no existe.

**Por qué los sprites son texto:** no dependen de ningún archivo (el juego **nunca falla** por falta de imágenes), se ven y se corrigen en cualquier editor, y se pueden defender línea a línea.

### 8.4 `GestorSprites`: el cargador con respaldo

Es el "cargador simple de sprites" que pedía el enunciado: busca `assets/<nombre>.png` y, si no está, usa el dibujo hecho con Graphics2D.

```java
public BufferedImage obtener(String nombre, int escala) {
    if (escala < 1) {
        throw new IllegalArgumentException("la escala debe ser al menos 1: " + escala);
    }
    for (int i = 0; i < cache.tamanio(); i++) {
        Guardado g = cache.obtener(i);
        if (g.escala == escala && g.nombre.equals(nombre)) {
            return g.imagen;
        }
    }
    BufferedImage imagen = cargarArchivo(nombre, escala);
    if (imagen == null) {
        imagen = CatalogoSprites.proceduralDe(nombre).crearImagen(escala);
    }
    cache.agregar(new Guardado(nombre, escala, imagen));
    return imagen;
}
```

1. Busca en el **caché**: una `ListaEnlazada` de `Guardado` (clase anidada con nombre, escala e imagen). Si ya se construyó esa imagen a esa escala, la devuelve. Esto es importante: dibujar 60 veces por segundo y reconstruir cada vez el sprite sería un desperdicio enorme.
2. Si no estaba, intenta `cargarArchivo` (el `.png`).
3. Si devuelve `null` (no hay archivo o está dañado), usa **el respaldo**: `CatalogoSprites.proceduralDe(nombre).crearImagen(escala)`.
4. Guarda el resultado en el caché y lo devuelve.

```java
private BufferedImage cargarArchivo(String nombre, int escala) {
    File archivo = new File(carpeta, nombre + ".png");
    if (!archivo.isFile()) {
        return null;
    }
    try {
        BufferedImage original = ImageIO.read(archivo);
        if (original == null) {
            return null;
        }
        int lado = LADO * escala;
        BufferedImage resultado = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < lado; y++) {
            for (int x = 0; x < lado; x++) {
                int origenX = x * original.getWidth() / lado;
                int origenY = y * original.getHeight() / lado;
                resultado.setRGB(x, y, original.getRGB(origenX, origenY));
            }
        }
        return resultado;
    } catch (IOException e) {
        return null;
    }
}
```

- Si el archivo no existe → `null` (usar respaldo).
- `ImageIO.read(archivo)` lee el `.png`. Si el archivo no es una imagen válida devuelve `null`; si hay un error de lectura lanza `IOException` → también se devuelve `null`. **Un `.png` dañado nunca rompe el juego.**
- Se **agranda sin suavizar**: para cada píxel del resultado (`lado = 16 × escala`) se toma el píxel proporcional de la imagen original (`x * ancho / lado`). Es el método del "vecino más cercano" y mantiene los píxeles nítidos aunque el `.png` tenga otro tamaño (por ejemplo, 32×32).

### 8.5 `TextoPixel`

Dibuja texto con letra gruesa de aspecto retro y una **sombra negra** desplazada 2 píxeles (se lee bien sobre cualquier fondo).

```java
public static ListaEnlazada<String> cortarEnLineas(Graphics2D g, String texto, int anchoMax, int tamanio) {
    ListaEnlazada<String> lineas = new ListaEnlazada<>();
    Tokenizador palabras = new Tokenizador(texto, ' ');
    String actual = "";
    while (palabras.hayMas()) {
        String palabra = palabras.siguiente();
        String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
        if (!actual.isEmpty() && anchoDe(g, prueba, tamanio) > anchoMax) {
            lineas.agregar(actual);
            actual = palabra;
        } else {
            actual = prueba;
        }
    }
    if (!actual.isEmpty()) {
        lineas.agregar(actual);
    }
    return lineas;
}
```

`cortarEnLineas` es el **corte de párrafos**: toma el texto, lo parte en **palabras** (con el `Tokenizador` por espacios) y va armando un renglón. Cuando agregar la próxima palabra haría que el renglón **supere el ancho máximo** (`anchoDe` mide en píxeles con `FontMetrics`), cierra el renglón y empieza otro. Devuelve la **lista de renglones**.

`parrafo` dibuja esos renglones uno debajo del otro. `parrafoEscribiendo` hace lo mismo pero mostrando solo las primeras *N* letras: es el **efecto máquina de escribir**. Los renglones se calculan con el texto **completo** para que las palabras no "salten" de renglón mientras se escribe.

### 8.6 `DibujoMedieval`

Funciones estáticas de dibujo. Es "el ejemplo de renderizado medieval en Graphics2D" del enunciado.

```java
public static BufferedImage crearFondoBosque(GestorSprites sprites, int ancho, int alto, long semilla) {
    BufferedImage fondo = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = fondo.createGraphics();
    Random azar = new Random(semilla);

    g.setPaint(new GradientPaint(0, 0, new Color(14, 34, 28), 0, alto, new Color(34, 78, 44)));
    g.fillRect(0, 0, ancho, alto);

    // Pasto: cuadraditos de verdes parecidos
    for (int i = 0; i < ancho * alto / 260; i++) {
        int x = azar.nextInt(ancho);
        int y = azar.nextInt(alto);
        int brillo = azar.nextInt(30);
        g.setColor(new Color(30 + brillo / 2, 70 + brillo, 38 + brillo / 2, 140));
        g.fillRect(x, y, 4, 4);
    }

    // Árboles: filas desde el fondo (arriba) hacia el frente (abajo)
    for (int y = 30; y < alto + 60; y += 78) {
        for (int x = -30; x < ancho + 30; x += 96) {
            int px = x + azar.nextInt(50);
            int py = y + azar.nextInt(30);
            g.drawImage(sprites.obtener(CatalogoSprites.ARBOL, 6), px, py - 96, null);
        }
    }

    sombrearBordes(g, ancho, alto);
    g.dispose();
    return fondo;
}
```

Construye **una sola vez** el fondo del bosque en una imagen (después se dibuja de un solo golpe con `drawImage`):

1. Degradado vertical (`GradientPaint`): verde muy oscuro arriba, más claro abajo.
2. "Ruido" de pasto: miles de cuadraditos de 4×4 de verdes parecidos en posiciones al azar.
3. **Árboles** en filas, del fondo (arriba) hacia el frente (abajo): como los de abajo se dibujan al final, tapan a los de arriba y se genera **profundidad**.
4. Los bordes se oscurecen con `sombrearBordes` (efecto "viñeta").

Se usa `new Random(semilla)` otra vez: el mismo bosque se ve igual en cada pasada.

```java
public static void sendero(Graphics2D g, double x1, double y1, double x2, double y2, Color color, int punto) {
    QuadCurve2D.Double curva = curva(x1, y1, x2, y2);
    double largo = Math.hypot(x2 - x1, y2 - y1);
    int pasos = (int) Math.max(4, largo / (punto * 2.6));
    g.setColor(color);
    for (int i = 1; i < pasos; i++) {
        double t = (double) i / pasos;
        double px = (1 - t) * (1 - t) * curva.x1 + 2 * (1 - t) * t * curva.ctrlx + t * t * curva.x2;
        double py = (1 - t) * (1 - t) * curva.y1 + 2 * (1 - t) * t * curva.ctrly + t * t * curva.y2;
        g.fillRect((int) Math.round(px - punto / 2.0), (int) Math.round(py - punto / 2.0), punto, punto);
    }
}
```

```java
public static QuadCurve2D.Double curva(double x1, double y1, double x2, double y2) {
    double desvio = ((int) (x1 * 7 + y2 * 13) % 31) - 15;
    double mx = (x1 + x2) / 2;
    double my = (y1 + y2) / 2 + desvio;
    return new QuadCurve2D.Double(x1, y1, mx, my, x2, y2);
}
```

**Los senderos punteados del mapa:**

- `curva` define una **curva cuadrática** (`QuadCurve2D`) entre los dos lugares: tiene un punto de control en el medio, corrido un poco hacia arriba o abajo. El corrimiento sale de las coordenadas (no del azar), así el mismo camino se ve **siempre igual**.
- `sendero` recorre la curva con un parámetro `t` de 0 a 1 y, en cada paso, calcula el punto con la fórmula de la curva de Bézier cuadrática y dibuja un **cuadradito**: el resultado es una línea punteada pixel-art. (Se hace a mano porque `BasicStroke` punteado requiere un **arreglo** de longitudes, y evitamos arreglos.)

```java
public static void cuadroDialogo(Graphics2D g, int x, int y, int ancho, int alto) {
    g.setColor(new Color(8, 6, 14, 235));
    g.fillRect(x, y, ancho, alto);
    g.setColor(Paleta.ORO_OSCURO);
    g.fillRect(x, y, ancho, 4);
    g.fillRect(x, y + alto - 4, ancho, 4);
    g.fillRect(x, y, 4, alto);
    g.fillRect(x + ancho - 4, y, 4, alto);
    g.setColor(Paleta.PIEDRA_CLARA);
    g.drawRect(x + 7, y + 7, ancho - 15, alto - 15);
    // Esquinas doradas
    g.setColor(Paleta.ORO);
    g.fillRect(x - 2, y - 2, 12, 12);
    g.fillRect(x + ancho - 10, y - 2, 12, 12);
    g.fillRect(x - 2, y + alto - 10, 12, 12);
    g.fillRect(x + ancho - 10, y + alto - 10, 12, 12);
}
```

```java
public static void flecha(Graphics2D g, double x, double y, double tamanio) {
    Path2D.Double triangulo = new Path2D.Double();
    triangulo.moveTo(x, y - tamanio);
    triangulo.lineTo(x + tamanio * 1.4, y);
    triangulo.lineTo(x, y + tamanio);
    triangulo.closePath();
    g.setColor(Paleta.NEGRO);
    g.setStroke(new BasicStroke(4));
    g.draw(triangulo);
    g.setColor(Paleta.ORO);
    g.fill(triangulo);
}
```

- `cuadroDialogo`: rectángulo casi negro con transparencia, un borde dorado de 4 píxeles, una línea interior gris piedra y cuatro **esquinas doradas**. Es el cuadro oscuro estilo RPG clásico.
- `flecha`: el triángulo dorado de selección. Se arma con un `Path2D` (`moveTo` y `lineTo` unen los tres vértices; `closePath` cierra) y se dibuja dos veces: primero el borde negro y después el relleno dorado. Se usa `Path2D` en lugar de `fillPolygon` porque este último recibe **arreglos** de coordenadas.
- `marcoPergamino`: marco de madera, papel con degradado y rodillos dorados arriba y abajo.
- `barra`: barra de vida o maná con borde negro y un brillo claro en el tercio de arriba.

### 8.7 `ExportadorSprites`

Una herramienta aparte (tiene su propio `main`): guarda los 15 sprites como `.png` en `assets/` para poder retocarlos en un editor de pixel-art. Después, `GestorSprites` los prefiere automáticamente.

---

## 9. Paquetes `juego.recursos` y `juego.pantallas`

### 9.1 `Recursos`

```java
public Recursos(String carpetaDatos, String carpetaAssets) {
    this.archivos = new GestorArchivos(carpetaDatos);
    this.sprites = new GestorSprites(carpetaAssets);
    this.eventos = archivos.cargarEventos();
}
```

```java
public BufferedImage getFondoMapa(MapaBosque mapa) {
    if (fondoMapa == null || semillaFondoMapa != mapa.getSemilla()) {
        fondoMapa = DibujoMedieval.crearFondoMapa(sprites, mapa);
        semillaFondoMapa = mapa.getSemilla();
    }
    return fondoMapa;
}
```

Reúne los objetos que **comparten todas las pantallas**: el gestor de archivos, el gestor de sprites, la lista de eventos (se lee **una sola vez** al iniciar) y los fondos ya dibujados. Se crea una vez en `Main` y se va pasando de pantalla en pantalla.

`getFondoMapa` vuelve a dibujar el fondo solo si **cambió la semilla** (es decir, si es otra partida). Por eso volver al mapa después de un combate es instantáneo.

**Por qué es un paquete aparte:** si `Recursos` estuviera en el paquete `juego` junto con `Main`, habría un ciclo (`juego` ↔ `juego.pantallas`).

### 9.2 `Main`

```java
public class Main {

    // Punto de entrada: abre la ventana con el menú principal.
    // Las carpetas "datos" (eventos.txt y guardado.txt) y "assets" (imágenes opcionales) están en la raíz del proyecto.
    public static void main(String[] args) {
        Recursos recursos = new Recursos("datos", "assets");
        Ventana.abrir("El Sendero Oscuro", 1024, 640, new EscenaMenu(recursos));
    }
}
```

Crea los recursos y abre la ventana del kit con una **escena inicial** (el menú). `Ventana.abrir(titulo, ancho, alto, escena)` arma la ventana de 1024×640 y arranca el bucle del juego.

## 10. El motor del kit (lo mínimo que hay que saber)

El kit del profesor resuelve la ventana, el reloj y el teclado. Lo que hay que entender para leer las pantallas:

- Una **`Escena`** es una pantalla. Las nuestras **extienden** `Escena` y escriben tres métodos:
  - `alEntrar()`: se ejecuta una vez al abrir la escena (opcional).
  - `actualizar(double dt)`: se ejecuta **120 veces por segundo**; `dt` (1/120 s) es el tiempo transcurrido. Acá se lee la entrada y se actualiza el estado.
  - `dibujar(Graphics2D g)`: se ejecuta para pintar el cuadro. **Solo dibuja**, no cambia el estado.
- `getEntrada()` da acceso al teclado y al mouse:
  - `fuePresionada(tecla)`: `true` **solo en el cuadro en que se apretó** (para acciones de una sola vez: elegir, confirmar).
  - `estaPresionada(tecla)`: `true` mientras se mantiene apretada (para movimiento continuo).
  - `huboClick()`, `getMouseX()`, `getMouseY()`.
- `getContexto().irA(otraEscena)`: cambia de pantalla. El cambio se hace al **comienzo del próximo paso**, no de inmediato.
- `getContexto().salir()`: cierra el juego.

Como las escenas solo dependen de `Escena` y `Entrada`, **se pueden probar sin ventana**: `Motor` tiene `simular(segundos)` (avanza el reloj) y `capturar()` (dibuja en una imagen en memoria). Es lo que hace `PruebaJuego`.

### 9.3 `Hud` y `Adornos`

`Hud.dibujarFranja` dibuja arriba el panel de estado: corazón con barra de vida, gota con barra de maná, oro y atributos (FUE, DES, INT, DEF). La comparten el mapa, los eventos y el combate para que se vea **siempre igual**. `Adornos.antorcha` dibuja una antorcha con **llama que parpadea**: un halo naranja de 4 capas cuyo radio cambia con `Math.sin(tiempo)` mezclando dos frecuencias para que el parpadeo parezca irregular.

### 9.4 `EscenaMenu`

```java
public void actualizar(double dt) {
    tiempo += dt;
    if (tiempoAviso > 0) {
        tiempoAviso -= dt;
    }
    Entrada entrada = getEntrada();

    if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)) {
        seleccion = (seleccion + OPCIONES - 1) % OPCIONES;
    }
    if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)) {
        seleccion = (seleccion + 1) % OPCIONES;
    }

    // Mouse: pasar por encima selecciona, hacer click confirma
    for (int i = 0; i < OPCIONES; i++) {
        if (estaSobreOpcion(entrada, i)) {
            if (entrada.getMouseX() != 0 || entrada.getMouseY() != 0) {
                seleccion = i;
            }
            if (entrada.huboClick()) {
                seleccion = i;
                confirmar();
                return;
            }
        }
    }
    if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
        confirmar();
    }
}
```

```java
private void confirmar() {
    GestorArchivos archivos = recursos.getArchivos();
    if (seleccion == NUEVA) {
        long semilla = System.currentTimeMillis() % 100000;
        Partida partida = Partida.nueva(semilla, recursos.getEventos());
        getContexto().irA(new EscenaMapa(recursos, partida, false));
    } else if (seleccion == CONTINUAR) {
        Partida partida = archivos.existeGuardado() ? archivos.cargar(recursos.getEventos()) : null;
        if (partida == null) {
            aviso = archivos.existeGuardado() ? "El archivo de guardado está dañado." : "No hay una partida guardada.";
            tiempoAviso = 3;
        } else {
            getContexto().irA(new EscenaMapa(recursos, partida, false));
        }
    } else {
        getContexto().salir();
    }
}
```

- `actualizar`: cada cuadro suma `dt` al `tiempo` (para las animaciones) y lee la entrada. Las flechas cambian `seleccion` con **aritmética modular**: `(seleccion + 1) % OPCIONES` vuelve a 0 después de la última; `(seleccion + OPCIONES - 1) % OPCIONES` vuelve a la última antes de la primera (se suma `OPCIONES` para que nunca sea negativo). El mouse también selecciona y confirma.
- `alEntrar` deja marcada "Continuar" si ya hay una partida guardada.
- `confirmar`:
  - **Nueva partida**: semilla = `System.currentTimeMillis() % 100000` (el reloj da un número distinto cada vez) y `Partida.nueva(...)` → abre el mapa.
  - **Continuar**: si existe el archivo, intenta `cargar`. Si devuelve `null` muestra "El archivo de guardado está dañado" **sin cerrar el juego**; si no, abre el mapa con la partida cargada.
  - **Salir**: `getContexto().salir()`.

### 9.5 `EscenaMapa`: dibujar el grafo y navegarlo

```java
public void alEntrar() {
    fondo = recursos.getFondoMapa(partida.getMapa());
    vecinos = partida.getAdyacentes();
    ruta = partida.getRuta();
    if (autoguardar) {
        guardar("Aventura guardada automáticamente.");
    }
}
```

```java
private void avanzar() {
    int destino = vecinos.obtener(seleccion);
    partida.moverA(destino);
    NodoMapa nodo = partida.getMapa().getNodo(destino);
    TipoNodo tipo = nodo.getTipo();
    if (tipo == TipoNodo.COMBATE || tipo == TipoNodo.JEFE) {
        Enemigo enemigo = partida.crearEnemigoDeNodo(nodo);
        getContexto().irA(new EscenaCombate(recursos, partida, enemigo, tipo == TipoNodo.JEFE));
    } else if (tipo == TipoNodo.EVENTO) {
        EventoNarrativo evento = partida.crearEventoDeNodo(nodo);
        getContexto().irA(new EscenaEvento(recursos, partida, evento, false));
    } else {
        getContexto().irA(new EscenaEvento(recursos, partida, partida.crearEventoCofre(), true));
    }
}
```

- `alEntrar` pide el fondo, la lista de **vecinos** (`partida.getAdyacentes()`: los destinos posibles desde el lugar actual) y la **ruta**. Si es el regreso de un combate o evento (`autoguardar` en `true`) **guarda la partida automáticamente**.
- Las teclas arriba/abajo (o izquierda/derecha) cambian la selección **entre los vecinos**, no entre todos los nodos: el jugador solo puede elegir caminos que existen en el grafo.
- `avanzar`: llama a `partida.moverA(destino)` (que apila en la ruta y en el diario) y, según el **`TipoNodo`** del destino, abre la escena que corresponde: combate (también el jefe), evento (`?`) o cofre.

```java
private void dibujarSenderos(Graphics2D g) {
    Grafo<NodoMapa> grafo = partida.getMapa().getGrafo();
    int actual = partida.getNodoActual();
    for (int id = 0; id < grafo.cantidadVertices(); id++) {
        NodoMapa origen = grafo.obtener(id);
        ListaEnlazada<Integer> destinos = grafo.idsAdyacentes(id);
        for (int k = 0; k < destinos.tamanio(); k++) {
            NodoMapa destino = grafo.obtener(destinos.obtener(k));
            Color color = new Color(215, 195, 140, 150);
            int punto = 4;
            if (fueRecorrida(id, destino.getId())) {
                color = Paleta.ORO;
                punto = 6;
            } else if (id == actual) {
                color = Paleta.HUESO;
                punto = 6;
            }
            DibujoMedieval.sendero(g, origen.getX(), origen.getY(), destino.getX(), destino.getY(), color, punto);
        }
    }
}
```

```java
private boolean fueRecorrida(int desde, int hasta) {
    for (int i = 0; i + 1 < ruta.tamanio(); i++) {
        if (ruta.obtener(i) == desde && ruta.obtener(i + 1) == hasta) {
            return true;
        }
    }
    return false;
}
```

**Cómo se dibuja un grafo:** se **recorren los vértices** y, para cada uno, **su lista de adyacentes**. Cada par (origen, destino) es una arista y se dibuja un sendero punteado. El color indica el estado:

- **Dorado**: camino ya recorrido (`fueRecorrida` mira la ruta buscando dos nodos consecutivos).
- **Blanco**: caminos que salen del lugar actual (las opciones).
- **Beige tenue**: el resto.

```java
private void dibujarNodos(Graphics2D g) {
    Grafo<NodoMapa> grafo = partida.getMapa().getGrafo();
    for (int id = 0; id < grafo.cantidadVertices(); id++) {
        NodoMapa nodo = grafo.obtener(id);
        int posicion = vecinos.indiceDe(id);
        dibujarNodo(g, nodo, posicion >= 0, posicion >= 0 && posicion == seleccion);
    }
}
```

Cada lugar es un **disco** (borde negro + relleno según el tipo) con el **ícono** encima: calavera (combate), `?` (evento), cofre, jefe con corona, o fogata (inicio). Según el estado:

- **Visitado**: anillo dorado e ícono tenue (`AlphaComposite` con 45% de opacidad).
- **Vecino elegible**: anillo blanco que **pulsa** con `Math.sin(tiempo * 5)`. El elegido tiene el anillo más grueso, una flecha y su nombre debajo.

`dibujarHeroe` pinta al héroe encima del lugar actual, **saltando** con `Math.abs(Math.sin(tiempo * 3))`.

La tecla **H** abre el diario: usa `partida.getUltimosRegistros(8)` (la pila inspeccionada con la auxiliar).

### 9.6 `EscenaEvento`

```java
private void elegir() {
    OpcionEvento opcion = evento.getOpcion(seleccion);
    if (!opcion.puedeElegir(partida.getJugador())) {
        return;
    }
    elegida = opcion;
    resumen = opcion.aplicar(partida.getJugador());
    NodoMapa nodo = partida.getNodoActualCompleto();
    String nota = evento.getTitulo() + ": " + opcion.getTexto();
    if (!resumen.isEmpty()) {
        nota = nota + " (" + resumen + ")";
    }
    partida.registrar(nodo.getId(), nota);
    fase = RESULTADO;
    tiempo = 0;
}
```

```java
private void terminar() {
    String emboscada = elegida.getEmboscada();
    if (emboscada != null) {
        NodoMapa nodo = partida.getNodoActualCompleto();
        Enemigo enemigo = FabricaEnemigos.crearPorNombre(emboscada, nodo.getPiso());
        getContexto().irA(new EscenaCombate(recursos, partida, enemigo, false));
    } else {
        getContexto().irA(new EscenaMapa(recursos, partida, true));
    }
}
```

- Tiene **dos fases**: `ELEGIR` (se ve el relato y las opciones) y `RESULTADO` (se ve lo que pasó).
- `elegir()`: si la opción no es elegible (falta oro), **no hace nada**. Si lo es: la **aplica** (cambia al héroe) y guarda el resumen, **apila una línea en el diario** (`"Altares gemelos: Rezar ante el altar blanco (+10 vida)"`) y pasa a la fase de resultado.
- `terminar()`: si la opción tenía un efecto `COMBATE` (emboscada), crea el enemigo con `FabricaEnemigos.crearPorNombre` y abre el **combate**. Si no, **vuelve al mapa** con autoguardado.
- El texto aparece **letra por letra** (máquina de escribir): `letrasVisibles = tiempo × 55`.
- Las teclas `1`, `2`, `3` eligen directamente: `KeyEvent.VK_1 + i` aprovecha que los códigos de las teclas numéricas son consecutivos.

### 9.7 `EscenaCombate`

Es la pantalla más grande, pero casi toda su lógica es **dibujo**: las reglas están en `Combate`. La escena hace de **intermediaria**: lee teclas → llama a `Combate` → dibuja el estado.

```java
public void actualizar(double dt) {
    tiempo += dt;
    tiempoTexto += dt;
    Entrada entrada = getEntrada();
    combate.actualizar(dt);
    detectarGolpes();
    actualizarFlotantes(dt);

    int estado = combate.getEstado();
    if (estado == Combate.ELECCION) {
        manejarMenu(entrada);
    } else if (estado == Combate.APUNTANDO) {
        if (entrada.fuePresionada(KeyEvent.VK_SPACE) || entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.huboClick()) {
            combate.detenerBarra();
            tiempoTexto = 0;
        }
    } else if (estado == Combate.TURNO_ENEMIGO) {
        esperaEnemigo += dt;
        if (esperaEnemigo >= ESPERA_ENEMIGO || teclaContinuar(entrada)) {
            esperaEnemigo = 0;
            combate.continuar();
            tiempoTexto = 0;
        }
    } else {
        manejarFinDeMensaje(entrada, estado);
    }
}
```

`actualizar` es un `if/else` por el **estado** del combate:

| Estado | Qué hace la escena |
|---|---|
| `ELECCION` | `manejarMenu`: mueve la selección y llama `usar(i)` → `combate.elegirHabilidad(i)` |
| `APUNTANDO` | Si se aprieta ESPACIO (o ENTER o click) → `combate.detenerBarra()` |
| `TURNO_ENEMIGO` | Espera 0,9 segundos y llama `combate.continuar()` (o antes, si el jugador aprieta ENTER) |
| los demás | `manejarFinDeMensaje`: espera ENTER y decide a dónde ir |

Todos los cuadros se llama `combate.actualizar(dt)` (mueve la barra), `detectarGolpes()` y `actualizarFlotantes(dt)`.

```java
private void manejarFinDeMensaje(Entrada entrada, int estado) {
    if (estado == Combate.VICTORIA && !victoriaRegistrada) {
        registrarVictoria();
    }
    if (!teclaContinuar(entrada)) {
        return;
    }
    if (tiempoTexto < 0.25) {
        return;
    }
    if (estado == Combate.RESULTADO) {
        combate.continuar();
        tiempoTexto = 0;
    } else if (estado == Combate.VICTORIA) {
        if (combate.esJefe()) {
            recursos.getArchivos().borrarGuardado();
            getContexto().irA(new EscenaFin(recursos, partida, true));
        } else {
            getContexto().irA(new EscenaMapa(recursos, partida, true));
        }
    } else if (estado == Combate.HUIDA) {
        partida.registrar(partida.getNodoActual(), "Huiste de " + combate.getEnemigo().getNombre() + ".");
        getContexto().irA(new EscenaMapa(recursos, partida, true));
    } else if (estado == Combate.DERROTA) {
        recursos.getArchivos().borrarGuardado();
        getContexto().irA(new EscenaFin(recursos, partida, false));
    }
}
```

```java
private void registrarVictoria() {
    victoriaRegistrada = true;
    mejora = partida.sumarDerrotado();
    partida.registrar(partida.getNodoActual(), "Derrotaste a " + combate.getEnemigo().getNombre() + ".");
    tiempoTexto = 0;
}
```

Al terminar el combate:

- **Victoria**: se registra **una sola vez** (`victoriaRegistrada`) el enemigo derrotado (`partida.sumarDerrotado()`, que puede mejorar un atributo) y se anota en el diario. Con ENTER: si era el **jefe** se **borra el guardado** y se muestra la pantalla de victoria; si no, se vuelve al mapa con autoguardado.
- **Huida**: se anota en el diario y se vuelve al mapa.
- **Derrota**: se **borra el guardado** (roguelike: muerte permanente) y se muestra la pantalla final.
- `if (tiempoTexto < 0.25) return;` evita que el ENTER con que se confirmó la acción anterior salte este mensaje sin leerlo.

```java
private void detectarGolpes() {
    Enemigo enemigo = combate.getEnemigo();
    Jugador jugador = partida.getJugador();
    if (enemigo.getVida() < vidaEnemigoPrevia) {
        sacudidaEnemigo = 0.4;
        flotantes.agregar(new Flotante("-" + (vidaEnemigoPrevia - enemigo.getVida()), 740, 230, Paleta.ORO));
    }
    if (jugador.getVida() < vidaJugadorPrevia) {
        sacudidaJugador = 0.4;
        flotantes.agregar(new Flotante("-" + (vidaJugadorPrevia - jugador.getVida()), 250, 260, new Color(255, 90, 90)));
    } else if (jugador.getVida() > vidaJugadorPrevia) {
        flotantes.agregar(new Flotante("+" + (jugador.getVida() - vidaJugadorPrevia), 250, 260, new Color(120, 255, 140)));
    }
    vidaEnemigoPrevia = enemigo.getVida();
    vidaJugadorPrevia = jugador.getVida();
}
```

**Sacudida y números flotantes:** cada cuadro se compara la vida actual con la del cuadro anterior (`vidaEnemigoPrevia`, `vidaJugadorPrevia`). Si bajó, se activa la **sacudida** (un movimiento rápido con `Math.sin(tiempo * 90)` que se apaga en 0,4 s) y se agrega a la lista un **número flotante** (`-8`). Si la vida del jugador subió, el número es verde (`+15`). Cada `Flotante` sube y se desvanece; cuando su vida llega a 0 se **elimina de la lista** (un `while` con índice que solo avanza si no se eliminó nada).

La escena no sabe *por qué* cambió la vida; solo observa el cambio. Eso la mantiene separada de la lógica.

```java
private void dibujarOrdenDeTurnos(Graphics2D g) {
    ColaEnlazada<Integer> orden = combate.getOrdenDeTurnos();
    TextoPixel.izquierda(g, "Turnos:", 640, 104, 14, Paleta.PERGAMINO);
    int x = 720;
    boolean primero = true;
    while (!orden.estaVacia()) {
        int quien = orden.desencolar();
        Color color = quien == Combate.JUGADOR ? Paleta.AZUL : Paleta.ROJO;
        g.setColor(Paleta.NEGRO);
        g.fillOval(x - 3, 86, 28, 28);
        g.setColor(color);
        g.fillOval(x, 89, 22, 22);
        if (primero) {
            g.setColor(Paleta.ORO);
            g.drawOval(x - 3, 86, 28, 28);
            primero = false;
        }
        TextoPixel.izquierda(g, quien == Combate.JUGADOR ? "T" : "E", x + 6, 106, 14, Paleta.HUESO);
        x = x + 36;
    }
}
```

**La cola de turnos en pantalla:** `combate.getOrdenDeTurnos()` devuelve una **copia** de la cola. Se la vacía con un `while (!orden.estaVacia())` dibujando una ficha por turno: azul con `T` (turno del jugador) o roja con `E` (enemigo). La primera tiene un borde dorado (es quien actúa ahora). Con el Lobo se ven tres fichas: T, E, E.

```java
private void dibujarBarra(Graphics2D g, BarraPrecision barra) {
    int x = (ANCHO - (int) BarraPrecision.ANCHO) / 2;
    int y = 510;
    int alto = 56;
    g.setColor(Paleta.NEGRO);
    g.fillRect(x - 5, y - 5, (int) BarraPrecision.ANCHO + 10, alto + 10);
    g.setColor(new Color(60, 30, 34));
    g.fillRect(x, y, (int) BarraPrecision.ANCHO, alto);
    double centro = x + BarraPrecision.ANCHO / 2;
    g.setColor(new Color(176, 130, 40));
    g.fillRect((int) (centro - barra.getAnchoNormal() / 2), y, (int) barra.getAnchoNormal(), alto);
    g.setColor(new Color(235, 80, 50));
    g.fillRect((int) (centro - barra.getAnchoCritico() / 2), y, (int) barra.getAnchoCritico(), alto);
    g.setColor(Paleta.HUESO);
    g.fillRect((int) centro - 1, y, 3, alto);

    // Cursor
    int cursorX = (int) (x + barra.getPosicion());
    g.setColor(Paleta.NEGRO);
    g.fillRect(cursorX - 7, y - 10, 14, alto + 20);
    g.setColor(Color.WHITE);
    g.fillRect(cursorX - 4, y - 7, 8, alto + 14);
    TextoPixel.centrado(g, "¡ESPACIO para frenar!", ANCHO / 2.0, 604, 18, Paleta.PERGAMINO);
}
```

**La barra de precisión:** se pintan tres rectángulos centrados: la zona de fondo (rojo oscuro), la **zona normal** (dorado) con el ancho que dice la barra, y la **zona crítica** (naranja) encima. Una línea blanca marca el centro. El **cursor** es un rectángulo blanco con borde negro en `x + barra.getPosicion()`. Todos los anchos vienen de la propia `BarraPrecision`: la pantalla **no calcula nada**, solo dibuja lo que la lógica le dice; por eso con más Destreza la zona crítica se ve más ancha.

### 9.8 `EscenaFin`

Muestra la victoria o la derrota con el resumen (enemigos derrotados, lugares recorridos, oro, atributos y el último registro del diario, tomado con `getUltimosRegistros(1)`). Con ENTER vuelve al menú. Ignora la entrada durante el primer medio segundo para evitar saltearla sin querer.

---

## 11. `PruebaJuego`: cómo se prueba todo sin abrir la ventana

`juego/pruebas/PruebaJuego.java` ejecuta **265 comprobaciones** automáticas. Se corre con `java -cp out juego.pruebas.PruebaJuego`. Imprime solo las que fallan y al final el total.

La función clave es:

```java
private static void comprobar(String descripcion, boolean condicion) {
    total++;
    if (!condicion) {
        fallas++;
        System.out.println("FALLA: " + descripcion);
    }
}
```

Cada prueba llama a `comprobar("lo que debería pasar", condicion)`. Si la condición es falsa, se imprime `FALLA: ...` y se cuenta. El programa termina con código 1 si hubo fallas.

Para comprobar que algo **lanza una excepción** se usa este patrón:

```java
try {
    new Jugador(0, 3, 3, 10, 10, 0);
    comprobar("jugador: fuerza 0 debe fallar", false);   // si llegó acá, NO lanzó: es un error
} catch (IllegalArgumentException ex) {
    comprobar("jugador: atributo 0 lanza IllegalArgumentException", true);   // lanzó lo esperado
}
```

Qué cubren los grupos de pruebas:

| Grupo | Qué verifica |
|---|---|
| `pruebasGrafo` | Aristas, dirección, lazos, BFS, DFS, `hayCamino`, errores |
| `pruebasListaYTokenizador` | Inserción, borrado, casos borde del tokenizador |
| `pruebasJugador` | Cada fórmula con los valores exactos, límites de vida/maná/oro, el repertorio de 4 |
| `pruebasHabilidades` / `pruebasEnemigos` | Daño normal y crítico, curación, brujo que se cura cada 3, rey que pega doble cada 4 |
| `pruebasBarra` | Zonas, efecto de la Destreza, rebote, detención |
| `pruebasCombate` | Cola de turnos, estados, escape, victoria, derrota |
| `pruebasMapa` | **40 semillas**: todos los nodos alcanzables, sin salidas muertas, cofres en pisos 2 y 4; la misma semilla da el mismo mapa |
| `pruebasEventos` | Lectura de eventos, descarte de los mal escritos, el `eventos.txt` real |
| `pruebasPartida` | Movimiento, pilas de ruta y diario, determinismo por nodo, cofre, crecimiento |
| `pruebasArchivos` | Guardar y cargar (ida y vuelta), **13 casos de archivo dañado** |
| `pruebasSprites` | Los 15 sprites miden 16×16, `.png` propio, `.png` dañado, respaldo |
| `pruebasPantallas` | Las escenas con el motor sin ventana: teclas simuladas y `capturar()` |
| `pruebasAventuraCompleta` | Un **jugador robot** juega 60 aventuras completas |

**El jugador robot:** elige caminos, decide eventos al azar, cura cuando le falta vida y frena la barra con una puntería regular. Sirve para comprobar dos cosas: que **ninguna partida se traba** (siempre se puede avanzar) y que el juego **no es imposible ni trivial** (el robot gana algunas y pierde otras). Con él se ajustaron las cifras de los enemigos.

---

## 12. Una partida de punta a punta

Seguimos una partida completa nombrando qué clase hace cada cosa:

1. **`Main`** crea `Recursos` (lee `datos/eventos.txt` con `GestorArchivos.cargarEventos`) y abre la ventana con `EscenaMenu`.
2. En el menú, el jugador elige **Nueva partida**. `EscenaMenu.confirmar` llama a `Partida.nueva(semilla, eventos)`:
   - `MapaBosque(semilla)` genera el **grafo** con `Random(semilla)`.
   - Se crea un `Jugador` (3/3/3, vida 35, maná 15, solo sabe atacar).
   - Se apila el nodo de inicio en la **pila de la ruta** y la primera línea en la **pila del diario**.
3. `EscenaMapa` dibuja el grafo. El jugador elige un vecino y aprieta ENTER → `partida.moverA(id)` apila en las dos pilas.
4. Si el lugar es un **combate**, `EscenaCombate` crea un `Combate`:
   - Se arma la **cola de turnos** `[J, E]`.
   - El jugador elige *Ataque* → `Combate.elegirHabilidad(0)` → se crea la `BarraPrecision` (la **Destreza** define su dificultad).
   - Aprieta ESPACIO → `detenerBarra()` → la barra devuelve una `Calidad` → `HabilidadAtaque.aplicar` usa la **Fuerza** (`getDanioFisico`) y el enemigo pierde vida.
   - Se desencola `J` y el frente es `E`: el enemigo ataca (`Esqueleto.accionar`) y la **Defensa** (Destreza ÷ 2) reduce el daño.
   - Cuando el enemigo cae: `VICTORIA`, oro, `partida.sumarDerrotado()` (cada 2, +1 atributo).
5. Se vuelve al mapa: **autoguardado** → `GestorArchivos.guardar` escribe `datos/guardado.txt` con `PrintWriter`.
6. Si el lugar es un **evento**, `EscenaEvento` muestra 2 o 3 opciones (una lista enlazada); la elegida aplica sus efectos (otra lista) y se anota en el diario.
7. Si el lugar es un **cofre**, `Partida.crearEventoCofre()` arma el evento con la próxima habilidad por aprender: el repertorio (**lista**) crece hasta 4.
8. El jugador cierra el juego. Al volver, elige **Continuar**: `GestorArchivos.cargar` lee el archivo con `BufferedReader`, regenera el mapa con la semilla, valida que la ruta sea un camino real del grafo y reconstruye las pilas.
9. Llega al **jefe** (el único destino del último piso). Si vence al Rey Oscuro: se **borra el guardado** y aparece `EscenaFin` con la victoria. Si cae: lo mismo con la pantalla de derrota.

Esto muestra cómo las **cuatro estructuras** cumplen un papel distinto y necesario:

| Momento | Estructura |
|---|---|
| Elegir adónde ir | **Grafo** (aristas = caminos posibles) |
| Qué habilidades tengo / qué opciones ofrece el evento | **Lista** |
| Quién actúa ahora en el combate | **Cola** |
| Dónde estuve y qué decidí | **Pila** |

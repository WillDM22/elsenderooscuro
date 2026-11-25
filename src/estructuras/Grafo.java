package estructuras;

// Grafo dirigido con listas de adyacencia propias.
// Cada vértice guarda un dato (por ejemplo un nodo del mapa) y la lista de vértices a los que se puede ir.
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

    // Altas
    // Devuelve el identificador del vértice (0, 1, 2, ...)
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

    // Consultas
    public T obtener(int id) {
        return buscar(id).dato;
    }

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
        return buscar(id).adyacentes.tamanio();
    }

    public int cantidadVertices() {
        return vertices.tamanio();
    }

    public int cantidadAristas() {
        return aristas;
    }

    // Recorrido en anchura (BFS): usa una COLA. Devuelve los ids en el orden en que se visitan.
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
        buscar(destino);
        ListaEnlazada<Integer> alcanzables = recorridoEnAnchura(origen);
        return alcanzables.indiceDe(destino) != -1;
    }

    // Auxiliares privados
    private Vertice<T> buscar(int id) {
        if (id < 0 || id >= vertices.tamanio()) {
            throw new IllegalArgumentException("no existe el vértice " + id);
        }
        return vertices.obtener(id);
    }

    private void limpiarMarcas() {
        for (int i = 0; i < vertices.tamanio(); i++) {
            vertices.obtener(i).visitado = false;
        }
    }
}

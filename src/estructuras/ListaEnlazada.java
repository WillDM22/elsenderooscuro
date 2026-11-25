package estructuras;

// Lista simplemente enlazada: cada nodo guarda un dato y una referencia al nodo siguiente.
// Se usa para el repertorio de habilidades, las opciones de un evento y muchas listas más.
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

    // Altas
    @Override
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

    @Override
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

    // Consultas
    @Override
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
        return tamanio;
    }

    @Override
    public boolean estaVacia() {
        return tamanio == 0;
    }

    // Bajas
    @Override
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
        primero = null;
        ultimo = null;
        tamanio = 0;
    }

    // Auxiliares privados
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
    private boolean iguales(T a, T b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}

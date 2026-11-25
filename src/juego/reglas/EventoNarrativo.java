package juego.reglas;

import estructuras.ListaEnlazada;

// Escena de decisión: un título, un relato breve y de 2 a 3 opciones (guardadas en una lista enlazada).
public class EventoNarrativo {

    // Constantes
    public static final int MIN_OPCIONES = 2;
    public static final int MAX_OPCIONES = 3;

    // Atributos
    private final String titulo;
    private final String relato;
    private final ListaEnlazada<OpcionEvento> opciones;

    // Constructor
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

    // Getters
    public String getTitulo() {
        return titulo;
    }

    public String getRelato() {
        return relato;
    }

    public int cantidadOpciones() {
        return opciones.tamanio();
    }

    public OpcionEvento getOpcion(int indice) {
        return opciones.obtener(indice);
    }
}

package juego.reglas;

// Una línea del diario de la aventura: dónde ocurrió (id del nodo) y qué pasó.
// Es inmutable: una vez creada no cambia.
public class Registro {

    // Atributos
    private final int idNodo;
    private final String texto;

    // Constructor
    public Registro(int idNodo, String texto) {
        if (texto == null || texto.isEmpty()) {
            throw new IllegalArgumentException("el registro necesita un texto");
        }
        this.idNodo = idNodo;
        this.texto = texto;
    }

    public int getIdNodo() {
        return idNodo;
    }

    public String getTexto() {
        return texto;
    }
}

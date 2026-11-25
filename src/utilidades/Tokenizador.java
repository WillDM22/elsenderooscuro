package utilidades;

// Mini-librería propia: parte un texto en trozos separados por un carácter.
// Hace lo mismo que String.split pero sin arreglos: se pide un trozo por vez.
//
// Ejemplo:  Tokenizador t = new Tokenizador("a|b|c", '|');
//           while (t.hayMas()) { String trozo = t.siguiente(); }
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

    // ¿Falta algún trozo por leer?
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
        Tokenizador t = new Tokenizador(texto, separador);
        int total = 0;
        while (t.hayMas()) {
            t.siguiente();
            total++;
        }
        return total;
    }
}

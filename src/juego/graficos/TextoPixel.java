package juego.graficos;

import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import utilidades.Tokenizador;

// Texto con letra gruesa de aspecto retro y una sombra negra (se lee bien sobre cualquier fondo).
public final class TextoPixel {

    // Escala global del texto (la cambia la pantalla de opciones): 1.0 = normal
    private static double escala = 1.0;

    // Sin instancias
    private TextoPixel() {
    }

    public static void setEscala(double nueva) {
        if (!(nueva > 0.5 && nueva < 2.0)) {
            throw new IllegalArgumentException("escala de texto fuera de rango: " + nueva);
        }
        escala = nueva;
    }

    public static double getEscala() {
        return escala;
    }

    public static void izquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        izquierda(g, texto, x, y, tamanio, color, true);
    }

    // Con o sin sombra (sobre el pergamino claro la sombra negra queda fea)
    public static void izquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                 boolean conSombra) {
        preparar(g, tamanio);
        if (conSombra) {
            g.setColor(Paleta.NEGRO);
            g.drawString(texto, (float) x + 2, (float) y + 2);
        }
        g.setColor(color);
        g.drawString(texto, (float) x, (float) y);
    }

    public static void centrado(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        centrado(g, texto, x, y, tamanio, color, true);
    }

    public static void centrado(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                boolean conSombra) {
        izquierda(g, texto, x - anchoDe(g, texto, tamanio) / 2.0, y, tamanio, color, conSombra);
    }

    public static void derecha(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        izquierda(g, texto, x - anchoDe(g, texto, tamanio), y, tamanio, color);
    }

    // Ancho en píxeles que ocupa el texto
    public static int anchoDe(Graphics2D g, String texto, int tamanio) {
        preparar(g, tamanio);
        FontMetrics medidas = g.getFontMetrics();
        return medidas.stringWidth(texto);
    }

    // Escribe un párrafo que baja de renglón al llegar a anchoMax. Devuelve la "y" donde terminó.
    public static double parrafo(Graphics2D g, String texto, double x, double y, int anchoMax, int tamanio, Color color) {
        return parrafo(g, texto, x, y, anchoMax, tamanio, color, true);
    }

    public static double parrafo(Graphics2D g, String texto, double x, double y, int anchoMax, int tamanio, Color color,
                                 boolean conSombra) {
        ListaEnlazada<String> lineas = cortarEnLineas(g, texto, anchoMax, tamanio);
        double interlineado = tamanio * 1.35;
        double actual = y;
        for (int i = 0; i < lineas.tamanio(); i++) {
            izquierda(g, lineas.obtener(i), x, actual, tamanio, color, conSombra);
            actual = actual + interlineado;
        }
        return actual;
    }

    // Igual que parrafo, pero muestra solo las primeras "letras" letras (efecto máquina de escribir).
    // Los renglones se calculan con el texto completo, así las palabras no saltan de línea mientras se escribe.
    public static double parrafoEscribiendo(Graphics2D g, String texto, double x, double y, int anchoMax,
                                            int tamanio, Color color, int letras, boolean conSombra) {
        ListaEnlazada<String> lineas = cortarEnLineas(g, texto, anchoMax, tamanio);
        double interlineado = tamanio * 1.35;
        double actual = y;
        int restantes = letras;
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i);
            if (restantes > 0) {
                int visibles = Math.min(restantes, linea.length());
                izquierda(g, linea.substring(0, visibles), x, actual, tamanio, color, conSombra);
                restantes = restantes - linea.length() - 1;
            }
            actual = actual + interlineado;
        }
        return actual;
    }

    public static double parrafoEscribiendo(Graphics2D g, String texto, double x, double y, int anchoMax,
                                            int tamanio, Color color, int letras) {
        return parrafoEscribiendo(g, texto, x, y, anchoMax, tamanio, color, letras, true);
    }

    // Parte un texto en renglones que no superen anchoMax (corta entre palabras)
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

    private static void preparar(Graphics2D g, int tamanio) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(6, (int) Math.round(tamanio * escala))));
    }
}

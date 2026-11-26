package juego.graficos;

import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import utilidades.Tokenizador;

// Texto del juego con las fuentes de src/ui/Fuentes (ver la clase Fuentes) y una sombra negra
// (se lee bien sobre cualquier fondo).
//   - Los métodos normales (izquierda, centrado, derecha, parrafo...) usan la letra de CUERPO.
//   - Los métodos "titulo..." usan la letra gótica; los caracteres que ella no tiene (tildes, números)
//     se dibujan con la letra de cuerpo y, si tampoco, con una fuente del sistema.
public final class TextoPixel {

    // Las fuentes pixel-art se ven más chicas que una fuente común del mismo tamaño: se agrandan un poco
    private static final double AJUSTE_CUERPO = 1.22;
    private static final double AJUSTE_TITULO = 1.0;

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

    // Texto de cuerpo
    public static void izquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        izquierda(g, texto, x, y, tamanio, color, true);
    }

    // Con o sin sombra (sobre el pergamino claro la sombra negra queda fea)
    public static void izquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                 boolean conSombra) {
        escribir(g, texto, x, y, cadena(false, tamanio, 1.0), color, conSombra, tamanio);
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
        return (int) Math.round(recorrer(g, texto, 0, 0, cadena(false, tamanio, 1.0), false));
    }

    // Texto con letra gótica (títulos y opciones)
    public static void tituloIzquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        tituloIzquierda(g, texto, x, y, tamanio, color, true);
    }

    public static void tituloIzquierda(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                       boolean conSombra) {
        escribir(g, texto, x, y, cadena(true, tamanio, 1.0), color, conSombra, tamanio);
    }

    public static void tituloCentrado(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        tituloCentrado(g, texto, x, y, tamanio, color, true);
    }

    public static void tituloCentrado(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                      boolean conSombra) {
        tituloIzquierda(g, texto, x - anchoTitulo(g, texto, tamanio) / 2.0, y, tamanio, color, conSombra);
    }

    public static int anchoTitulo(Graphics2D g, String texto, int tamanio) {
        return (int) Math.round(recorrer(g, texto, 0, 0, cadena(true, tamanio, 1.0), false));
    }

    // Título gótico "de libro": la primera letra de cada palabra más grande y de otro color (como en los títulos
    // de los eventos). Se dibuja centrado en x, sin sombra.
    public static void tituloConIniciales(Graphics2D g, String texto, double x, double y, int tamanio,
                                          Color colorLetras, Color colorInicial) {
        Tokenizador palabras = new Tokenizador(texto, ' ');
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        while (palabras.hayMas()) {
            lista.agregar(palabras.siguiente());
        }
        Font[] normal = cadena(true, tamanio, 1.0);
        Font[] grande = cadena(true, tamanio, 1.3);
        double espacio = recorrer(g, " ", 0, 0, normal, false);
        double total = 0;
        for (int i = 0; i < lista.tamanio(); i++) {
            String palabra = lista.obtener(i);
            total = total + recorrer(g, palabra.substring(0, 1), 0, 0, grande, false)
                    + recorrer(g, palabra.substring(1), 0, 0, normal, false);
            if (i + 1 < lista.tamanio()) {
                total = total + espacio;
            }
        }
        double cursor = x - total / 2.0;
        for (int i = 0; i < lista.tamanio(); i++) {
            String palabra = lista.obtener(i);
            g.setColor(colorInicial);
            cursor = cursor + recorrer(g, palabra.substring(0, 1), cursor, y, grande, true);
            g.setColor(colorLetras);
            cursor = cursor + recorrer(g, palabra.substring(1), cursor, y, normal, true) + espacio;
        }
    }

    // Párrafos
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

    // Párrafo en el que las palabras escritas TODAS EN MAYÚSCULAS (de 4 letras o más, como ENERGÍA o MANÁ)
    // se resaltan con otro color. Devuelve la "y" donde terminó.
    public static double parrafoResaltado(Graphics2D g, String texto, double x, double y, int anchoMax, int tamanio,
                                          Color color, Color resaltado) {
        ListaEnlazada<String> lineas = cortarEnLineas(g, texto, anchoMax, tamanio);
        double interlineado = tamanio * 1.35;
        double actual = y;
        for (int i = 0; i < lineas.tamanio(); i++) {
            Tokenizador palabras = new Tokenizador(lineas.obtener(i), ' ');
            double cursor = x;
            double espacio = anchoDe(g, " ", tamanio);
            while (palabras.hayMas()) {
                String palabra = palabras.siguiente();
                Color c = esResaltada(palabra) ? resaltado : color;
                izquierda(g, palabra, cursor, actual, tamanio, c, true);
                cursor = cursor + anchoDe(g, palabra, tamanio) + espacio;
            }
            actual = actual + interlineado;
        }
        return actual;
    }

    // Una sola línea con las palabras TODAS EN MAYÚSCULAS resaltadas, centrada en x
    public static void centradoResaltado(Graphics2D g, String texto, double x, double y, int tamanio, Color color,
                                         Color resaltado) {
        double cursor = x - anchoDe(g, texto, tamanio) / 2.0;
        double espacio = anchoDe(g, " ", tamanio);
        Tokenizador palabras = new Tokenizador(texto, ' ');
        while (palabras.hayMas()) {
            String palabra = palabras.siguiente();
            izquierda(g, palabra, cursor, y, tamanio, esResaltada(palabra) ? resaltado : color, true);
            cursor = cursor + anchoDe(g, palabra, tamanio) + espacio;
        }
    }

    // Una palabra se resalta si tiene 4 letras o más y todas las letras son mayúsculas (el punto final no cuenta)
    private static boolean esResaltada(String palabra) {
        int letras = 0;
        for (int i = 0; i < palabra.length(); i++) {
            char c = palabra.charAt(i);
            if (Character.isLetter(c)) {
                if (!Character.isUpperCase(c)) {
                    return false;
                }
                letras++;
            }
        }
        return letras >= 4;
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

    // Auxiliares
    // Lista de fuentes a probar, en orden: la primera que tenga el carácter es la que lo dibuja
    private static Font[] cadena(boolean titulo, int tamanio, double extra) {
        double ajuste = titulo ? AJUSTE_TITULO : AJUSTE_CUERPO;
        float lado = (float) Math.max(6, Math.round(tamanio * escala * ajuste * extra));
        if (titulo) {
            return new Font[] {Fuentes.titulo(lado), Fuentes.cuerpo((float) Math.round(lado * AJUSTE_CUERPO)),
                Fuentes.respaldo(lado)};
        }
        return new Font[] {Fuentes.cuerpo(lado), Fuentes.respaldo(lado)};
    }

    // Dibuja el texto con sombra opcional
    private static void escribir(Graphics2D g, String texto, double x, double y, Font[] fuentes, Color color,
                                 boolean conSombra, int tamanio) {
        if (conSombra) {
            int desvio = tamanio >= 20 ? 2 : 1;
            g.setColor(Paleta.NEGRO);
            recorrer(g, texto, x + desvio, y + desvio, fuentes, true);
        }
        g.setColor(color);
        recorrer(g, texto, x, y, fuentes, true);
    }

    // Recorre el texto por tramos (cada tramo con la fuente que tiene sus caracteres). Si "dibujar" es true
    // los escribe; en cualquier caso devuelve el ancho total.
    private static double recorrer(Graphics2D g, String texto, double x, double y, Font[] fuentes, boolean dibujar) {
        double cursor = x;
        int i = 0;
        while (i < texto.length()) {
            int f = indiceFuente(fuentes, texto.charAt(i));
            int j = i + 1;
            while (j < texto.length() && indiceFuente(fuentes, texto.charAt(j)) == f) {
                j++;
            }
            String tramo = texto.substring(i, j);
            g.setFont(fuentes[f]);
            if (dibujar) {
                g.drawString(tramo, (float) cursor, (float) y);
            }
            cursor = cursor + g.getFontMetrics(fuentes[f]).stringWidth(tramo);
            i = j;
        }
        return cursor - x;
    }

    private static int indiceFuente(Font[] fuentes, char c) {
        for (int i = 0; i < fuentes.length - 1; i++) {
            if (fuentes[i].canDisplay(c)) {
                return i;
            }
        }
        return fuentes.length - 1;
    }
}

package juego.graficos;

import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.image.BufferedImage;
import utilidades.Tokenizador;

// Un dibujo pixel-art hecho con texto: cada fila es un String y cada letra, un píxel (ver Paleta).
// El texto completo trae las filas separadas por '/'.
public class Sprite {

    // Atributos
    private final ListaEnlazada<String> filas = new ListaEnlazada<>();
    private final int ancho;
    private final int alto;

    // Constructor: todas las filas deben medir lo mismo
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

    // Letra (color) de un píxel
    public char letraEn(int fila, int columna) {
        return filas.obtener(fila).charAt(columna);
    }

    // Convierte el texto en una imagen agrandada "escala" veces (cada píxel pasa a ser un cuadrado)
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

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }
}

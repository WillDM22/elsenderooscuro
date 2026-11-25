package juego.graficos;

import estructuras.ListaEnlazada;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// Cargador simple de sprites.
//   1) Busca assets/<nombre>.png. Si existe, lo usa (agrandado sin suavizar, para conservar el pixel-art).
//   2) Si el archivo no existe o está dañado, usa el dibujo hecho con texto de CatalogoSprites.
// Así el juego nunca falla por falta de imágenes. Las imágenes ya agrandadas se guardan en una lista (caché).
public class GestorSprites {

    // Tamaño base de un sprite en píxeles antes de agrandarlo
    public static final int LADO = 16;

    // Entrada del caché: qué sprite, a qué escala y su imagen
    private static class Guardado {
        private final String nombre;
        private final int escala;
        private final BufferedImage imagen;

        private Guardado(String nombre, int escala, BufferedImage imagen) {
            this.nombre = nombre;
            this.escala = escala;
            this.imagen = imagen;
        }
    }

    // Atributos
    private final String carpeta;
    private final ListaEnlazada<Guardado> cache = new ListaEnlazada<>();

    // Constructor
    public GestorSprites(String carpeta) {
        if (carpeta == null || carpeta.isEmpty()) {
            throw new IllegalArgumentException("la carpeta de assets no puede estar vacía");
        }
        this.carpeta = carpeta;
    }

    // Imagen del sprite agrandada "escala" veces
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

    // Dibuja el sprite centrado en (cx, cy)
    public void dibujar(Graphics2D g, String nombre, int escala, double cx, double cy) {
        BufferedImage imagen = obtener(nombre, escala);
        g.drawImage(imagen, (int) Math.round(cx - imagen.getWidth() / 2.0),
                (int) Math.round(cy - imagen.getHeight() / 2.0), null);
    }

    // ¿Hay un .png propio para este sprite?
    public boolean tieneArchivo(String nombre) {
        return new File(carpeta, nombre + ".png").isFile();
    }

    public String getCarpeta() {
        return carpeta;
    }

    // Lee el .png y lo agranda píxel por píxel. Devuelve null si no existe o no se puede leer.
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
}

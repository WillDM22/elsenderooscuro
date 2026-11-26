package juego.recursos;

import estructuras.ListaEnlazada;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import juego.archivos.GestorArchivos;
import juego.graficos.DibujoMedieval;
import juego.graficos.GestorSprites;
import juego.graficos.ImagenesUi;
import juego.graficos.TextoPixel;
import juego.opciones.Opciones;
import juego.reglas.EventoNarrativo;
import juego.reglas.MapaBosque;

// Objetos compartidos por todas las pantallas: archivos, sprites, eventos y fondos ya dibujados.
// Se crean una sola vez en Main y se van pasando de una escena a otra.
public class Recursos {

    // Atributos
    private final GestorArchivos archivos;
    private final GestorSprites sprites;
    private final ImagenesUi ui = new ImagenesUi();
    private final ListaEnlazada<EventoNarrativo> eventos;
    private final Opciones opciones;
    private BufferedImage fondoBosque;
    private BufferedImage fondoMapa;
    private long semillaFondoMapa = -1;

    // Constructor: lee eventos.txt una vez
    public Recursos(String carpetaDatos, String carpetaAssets) {
        this.archivos = new GestorArchivos(carpetaDatos);
        this.sprites = new GestorSprites(carpetaAssets);
        this.eventos = archivos.cargarEventos();
        this.opciones = archivos.cargarOpciones();
        TextoPixel.setEscala(opciones.getEscalaTexto());
    }

    // Se llama cada vez que cambia una opción: aplica el tamaño de texto y guarda el archivo.
    // (La resolución y el brillo los lee la ventana directamente del objeto Opciones.)
    public void aplicarOpciones() {
        TextoPixel.setEscala(opciones.getEscalaTexto());
        archivos.guardarOpciones(opciones);
    }

    // Fondo de bosque para menú, eventos y combates (se dibuja la primera vez que se pide)
    public BufferedImage getFondoBosque() {
        if (fondoBosque == null) {
            fondoBosque = DibujoMedieval.crearFondoBosque(sprites, MapaBosque.ANCHO, MapaBosque.ALTO, 7);
        }
        return fondoBosque;
    }

    // Fondo del mapa: el muro del calabozo con los senderos y los claros. Se vuelve a dibujar solo si cambió la
    // semilla (otra partida). Si falta la imagen del muro se usa el bosque dibujado por código.
    public BufferedImage getFondoMapa(MapaBosque mapa) {
        if (fondoMapa == null || semillaFondoMapa != mapa.getSemilla()) {
            BufferedImage nuevo = new BufferedImage(MapaBosque.ANCHO, MapaBosque.ALTO, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = nuevo.createGraphics();
            if (ui.dibujarEstirada(g, "Fondos/FondoMinimapa.png", 0, 0, MapaBosque.ANCHO, MapaBosque.ALTO)) {
                DibujoMedieval.dibujarSenderosYClaros(g, mapa);
                g.dispose();
                fondoMapa = nuevo;
            } else {
                g.dispose();
                fondoMapa = DibujoMedieval.crearFondoMapa(sprites, mapa);
            }
            semillaFondoMapa = mapa.getSemilla();
        }
        return fondoMapa;
    }

    // Getters
    public GestorArchivos getArchivos() {
        return archivos;
    }

    public GestorSprites getSprites() {
        return sprites;
    }

    // Imágenes de la interfaz (menús, HUD, fondos, personajes)
    public ImagenesUi getUi() {
        return ui;
    }

    public Opciones getOpciones() {
        return opciones;
    }

    public ListaEnlazada<EventoNarrativo> getEventos() {
        return eventos;
    }
}

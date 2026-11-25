package juego.recursos;

import estructuras.ListaEnlazada;
import java.awt.image.BufferedImage;
import juego.archivos.GestorArchivos;
import juego.graficos.DibujoMedieval;
import juego.graficos.GestorSprites;
import juego.reglas.EventoNarrativo;
import juego.reglas.MapaBosque;

// Objetos compartidos por todas las pantallas: archivos, sprites, eventos y fondos ya dibujados.
// Se crean una sola vez en Main y se van pasando de una escena a otra.
public class Recursos {

    // Atributos
    private final GestorArchivos archivos;
    private final GestorSprites sprites;
    private final ListaEnlazada<EventoNarrativo> eventos;
    private BufferedImage fondoBosque;
    private BufferedImage fondoMapa;
    private long semillaFondoMapa = -1;

    // Constructor: lee eventos.txt una vez
    public Recursos(String carpetaDatos, String carpetaAssets) {
        this.archivos = new GestorArchivos(carpetaDatos);
        this.sprites = new GestorSprites(carpetaAssets);
        this.eventos = archivos.cargarEventos();
    }

    // Fondo de bosque para menú, eventos y combates (se dibuja la primera vez que se pide)
    public BufferedImage getFondoBosque() {
        if (fondoBosque == null) {
            fondoBosque = DibujoMedieval.crearFondoBosque(sprites, MapaBosque.ANCHO, MapaBosque.ALTO, 7);
        }
        return fondoBosque;
    }

    // Fondo del mapa: se vuelve a dibujar solo si cambió la semilla (otra partida)
    public BufferedImage getFondoMapa(MapaBosque mapa) {
        if (fondoMapa == null || semillaFondoMapa != mapa.getSemilla()) {
            fondoMapa = DibujoMedieval.crearFondoMapa(sprites, mapa);
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

    public ListaEnlazada<EventoNarrativo> getEventos() {
        return eventos;
    }
}

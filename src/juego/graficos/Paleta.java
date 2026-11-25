package juego.graficos;

import java.awt.Color;

// Paleta medieval: cada letra de un sprite de texto corresponde a un color.
// Con esto un dibujo pixel-art se escribe como una grilla de caracteres.
public final class Paleta {

    // Colores con nombre (los usan también las pantallas)
    public static final Color NEGRO = new Color(20, 16, 24);
    public static final Color PERGAMINO = new Color(230, 210, 160);
    public static final Color PERGAMINO_OSCURO = new Color(196, 168, 112);
    public static final Color ORO = new Color(240, 200, 60);
    public static final Color ORO_OSCURO = new Color(180, 130, 30);
    public static final Color MADERA = new Color(110, 70, 40);
    public static final Color MADERA_OSCURA = new Color(60, 36, 24);
    public static final Color PIEDRA = new Color(120, 120, 130);
    public static final Color PIEDRA_OSCURA = new Color(70, 70, 86);
    public static final Color PIEDRA_CLARA = new Color(170, 170, 180);
    public static final Color BOSQUE = new Color(46, 100, 52);
    public static final Color BOSQUE_OSCURO = new Color(24, 56, 36);
    public static final Color BOSQUE_CLARO = new Color(84, 150, 70);
    public static final Color ROJO = new Color(190, 40, 40);
    public static final Color ROJO_OSCURO = new Color(110, 20, 30);
    public static final Color AZUL = new Color(60, 100, 200);
    public static final Color VIOLETA = new Color(110, 60, 150);
    public static final Color HUESO = new Color(232, 224, 200);

    // Sin instancias
    private Paleta() {
    }

    // Color de una letra; null significa "transparente" (el punto '.')
    public static Color deLetra(char letra) {
        switch (letra) {
            case 'K': return NEGRO;
            case 'W': return HUESO;
            case 'w': return new Color(170, 160, 140);
            case 'G': return BOSQUE;
            case 'g': return BOSQUE_CLARO;
            case 'D': return BOSQUE_OSCURO;
            case 'B': return MADERA;
            case 'b': return new Color(160, 110, 60);
            case 'N': return MADERA_OSCURA;
            case 'Y': return ORO;
            case 'y': return ORO_OSCURO;
            case 'R': return ROJO;
            case 'r': return ROJO_OSCURO;
            case 'S': return PIEDRA;
            case 's': return PIEDRA_OSCURA;
            case 'L': return PIEDRA_CLARA;
            case 'P': return VIOLETA;
            case 'p': return new Color(60, 30, 90);
            case 'F': return new Color(230, 180, 140);
            case 'f': return new Color(190, 140, 100);
            case 'U': return AZUL;
            case 'u': return new Color(30, 50, 120);
            case 'O': return new Color(230, 130, 40);
            case 'C': return PERGAMINO;
            case 'c': return PERGAMINO_OSCURO;
            case 'E': return new Color(255, 80, 60);
            default: return null;
        }
    }
}

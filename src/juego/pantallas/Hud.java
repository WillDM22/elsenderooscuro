package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import juego.recursos.Recursos;
import juego.entidades.Jugador;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;

// Panel de estado del héroe: vida, maná, oro y atributos. Lo comparten el mapa, los eventos y el combate.
public final class Hud {

    // Sin instancias
    private Hud() {
    }

    // Franja superior de 64 px de alto (sin barra de energía: solo se usa en combate)
    public static void dibujarFranja(Graphics2D g, Recursos recursos, Jugador jugador, int ancho) {
        dibujarFranja(g, recursos, jugador, ancho, false);
    }

    // Con conEnergia en true se agrega la barra de energía (verde) al lado de la clase del héroe
    public static void dibujarFranja(Graphics2D g, Recursos recursos, Jugador jugador, int ancho, boolean conEnergia) {
        DibujoMedieval.cuadroDialogo(g, 8, 6, ancho - 16, 62);
        g.drawImage(recursos.getSprites().obtener(CatalogoSprites.CORAZON, 2), 24, 14, null);
        DibujoMedieval.barra(g, 64, 20, 160, 14, jugador.getVida(), jugador.getVidaMaxima(), Paleta.ROJO, Paleta.ROJO_OSCURO);
        TextoPixel.izquierda(g, jugador.getVida() + "/" + jugador.getVidaMaxima(), 70, 32, 13, Paleta.HUESO);
        g.drawImage(recursos.getSprites().obtener(CatalogoSprites.POCION, 2), 24, 36, null);
        DibujoMedieval.barra(g, 64, 46, 160, 14, jugador.getMana(), jugador.getManaMaximo(), Paleta.AZUL, new Color(20, 30, 70));
        TextoPixel.izquierda(g, jugador.getMana() + "/" + jugador.getManaMaximo(), 70, 58, 13, Paleta.HUESO);

        TextoPixel.izquierda(g, "ORO " + jugador.getOro(), 260, 40, 20, Paleta.ORO);
        TextoPixel.izquierda(g, "FUE " + jugador.getFuerza(), 430, 32, 15, Paleta.HUESO);
        TextoPixel.izquierda(g, "DES " + jugador.getDestreza(), 430, 56, 15, Paleta.HUESO);
        TextoPixel.izquierda(g, "INT " + jugador.getInteligencia(), 520, 32, 15, Paleta.HUESO);
        TextoPixel.izquierda(g, "DEF " + jugador.getDefensa(), 520, 56, 15, Paleta.HUESO);
        TextoPixel.izquierda(g, jugador.getClase().getNombre(), 640, 32, 16, Paleta.ORO);
        if (conEnergia) {
            TextoPixel.izquierda(g, "ENE", 640, 58, 13, Paleta.HUESO);
            DibujoMedieval.barra(g, 690, 46, 160, 14, jugador.getEnergia(), jugador.getEnergiaMaxima(),
                    new Color(90, 190, 70), new Color(24, 70, 30));
            TextoPixel.izquierda(g, jugador.getEnergia() + "/" + jugador.getEnergiaMaxima(), 696, 58, 13, Paleta.HUESO);
        }
    }
}

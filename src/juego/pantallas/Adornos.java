package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import juego.recursos.Recursos;
import juego.graficos.CatalogoSprites;

// Detalles animados que decoran varias pantallas.
public final class Adornos {

    // Sin instancias
    private Adornos() {
    }

    // Antorcha con llama que parpadea: un halo naranja que cambia de tamaño con el tiempo
    public static void antorcha(Graphics2D g, Recursos recursos, double cx, double cy, double tiempo, double fase) {
        double parpadeo = 0.5 + 0.5 * Math.sin(tiempo * 9 + fase) * Math.sin(tiempo * 5.3 + fase * 2);
        int radio = (int) (70 + 18 * parpadeo);
        for (int i = 4; i >= 1; i--) {
            int r = radio * i / 4;
            g.setColor(new Color(255, 150, 40, 14 + (4 - i) * 6));
            g.fillOval((int) cx - r, (int) cy - 20 - r, r * 2, r * 2);
        }
        g.drawImage(recursos.getSprites().obtener(CatalogoSprites.ANTORCHA, 6), (int) cx - 48, (int) cy - 56, null);
    }
}

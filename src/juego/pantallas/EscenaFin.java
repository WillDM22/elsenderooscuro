package juego.pantallas;

import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.recursos.Recursos;
import juego.entidades.Jugador;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.reglas.Partida;
import juego.reglas.Registro;
import motor.Entrada;
import motor.Escena;

// Pantalla final: victoria sobre el Rey Oscuro o derrota (en un roguelike, al caer se pierde el guardado).
public class EscenaFin extends Escena {

    // Constantes
    private static final int ANCHO = 1024;

    // Atributos
    private final Recursos recursos;
    private final Partida partida;
    private final boolean victoria;
    private double tiempo;

    // Constructor
    public EscenaFin(Recursos recursos, Partida partida, boolean victoria) {
        this.recursos = recursos;
        this.partida = partida;
        this.victoria = victoria;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        Entrada entrada = getEntrada();
        if (tiempo > 0.5 && (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)
                || entrada.huboClick())) {
            getContexto().irA(new EscenaMenu(recursos));
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(victoria ? new Color(40, 30, 0, 120) : new Color(40, 0, 0, 170));
        g.fillRect(0, 0, ANCHO, 640);

        if (victoria) {
            Adornos.antorcha(g, recursos, 150, 330, tiempo, 0);
            Adornos.antorcha(g, recursos, ANCHO - 150, 330, tiempo, 2);
            TextoPixel.centrado(g, "¡VICTORIA!", ANCHO / 2.0, 110, 62, Paleta.ORO);
            TextoPixel.centrado(g, "El Rey Oscuro cae y el bosque vuelve a respirar.", ANCHO / 2.0, 154, 20, Paleta.PERGAMINO);
            DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(CatalogoSprites.JUGADOR, 9),
                    ANCHO / 2.0, 330 + Math.sin(tiempo * 2) * 6);
        } else {
            TextoPixel.centrado(g, "HAS CAÍDO", ANCHO / 2.0, 110, 62, new Color(220, 70, 70));
            TextoPixel.centrado(g, "El Sendero Oscuro guarda un héroe más entre sus raíces.", ANCHO / 2.0, 154, 20, Paleta.PERGAMINO);
            DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(CatalogoSprites.CALAVERA, 9),
                    ANCHO / 2.0, 330 + Math.sin(tiempo * 2) * 6);
        }

        // Resumen
        Jugador j = partida.getJugador();
        DibujoMedieval.cuadroDialogo(g, 130, 440, ANCHO - 260, 150);
        TextoPixel.izquierda(g, "Enemigos derrotados: " + partida.getDerrotados(), 170, 480, 20, Paleta.HUESO);
        TextoPixel.izquierda(g, "Lugares recorridos: " + (partida.getRuta().tamanio() - 1), 170, 512, 20, Paleta.HUESO);
        TextoPixel.izquierda(g, "Oro: " + j.getOro(), 170, 544, 20, Paleta.ORO);
        TextoPixel.izquierda(g, "FUE " + j.getFuerza() + "  DES " + j.getDestreza() + "  INT " + j.getInteligencia(),
                560, 480, 20, Paleta.HUESO);
        ListaEnlazada<Registro> ultimos = partida.getUltimosRegistros(1);
        if (!ultimos.estaVacia()) {
            TextoPixel.izquierda(g, "Último registro:", 560, 512, 16, Paleta.PERGAMINO);
            TextoPixel.parrafo(g, ultimos.obtener(0).getTexto(), 560, 536, 380, 15, Paleta.HUESO);
        }
        TextoPixel.centrado(g, "ENTER para volver al menú", ANCHO / 2.0, 616, 16, Paleta.PERGAMINO);
    }

    public boolean esVictoria() {
        return victoria;
    }
}

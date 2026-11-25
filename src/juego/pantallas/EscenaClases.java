package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.entidades.CatalogoHabilidades;
import juego.entidades.Clase;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.recursos.Recursos;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Elección de clase al empezar una partida nueva: Caballero, Mago, Druida o Ladrón.
// ←→ elige, ENTER confirma, ESC vuelve al menú. Cada clase cambia atributos, arma y primer hechizo.
public class EscenaClases extends Escena {

    // Constantes de dibujo
    private static final int ANCHO = 1024;
    private static final int ANCHO_TARJETA = 230;
    private static final int ALTO_TARJETA = 400;
    private static final int X_INICIAL = 30;
    private static final int SEPARACION = 245;
    private static final int Y_TARJETA = 120;

    // Atributos
    private final Recursos recursos;
    private int seleccion;
    private double tiempo;

    // Constructor
    public EscenaClases(Recursos recursos) {
        this.recursos = recursos;
    }

    private int xTarjeta(int i) {
        return X_INICIAL + i * SEPARACION;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        Entrada entrada = getEntrada();

        if (entrada.fuePresionada(KeyEvent.VK_LEFT) || entrada.fuePresionada(KeyEvent.VK_A)) {
            seleccion = (seleccion + Clase.CANTIDAD - 1) % Clase.CANTIDAD;
        }
        if (entrada.fuePresionada(KeyEvent.VK_RIGHT) || entrada.fuePresionada(KeyEvent.VK_D)) {
            seleccion = (seleccion + 1) % Clase.CANTIDAD;
        }
        for (int i = 0; i < Clase.CANTIDAD; i++) {
            if (entrada.fuePresionada(KeyEvent.VK_1 + i)) {
                seleccion = i;
            }
            boolean encima = entrada.getMouseX() >= xTarjeta(i) && entrada.getMouseX() <= xTarjeta(i) + ANCHO_TARJETA
                    && entrada.getMouseY() >= Y_TARJETA && entrada.getMouseY() <= Y_TARJETA + ALTO_TARJETA;
            if (encima && entrada.huboClick()) {
                seleccion = i;
                empezar();
                return;
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            empezar();
        } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
            getContexto().irA(new EscenaMenu(recursos));
        }
    }

    // Arranca la aventura con la clase elegida. La semilla sale del reloj: cada aventura tiene su propio mapa.
    private void empezar() {
        long semilla = System.currentTimeMillis() % 100000;
        Partida partida = Partida.nueva(semilla, recursos.getEventos(), Clase.porOrden(seleccion));
        getContexto().irA(new EscenaMapa(recursos, partida, false));
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(0, 0, ANCHO, 640);
        TextoPixel.centrado(g, "ELIGE TU CLASE", ANCHO / 2.0, 70, 42, Paleta.ORO);
        for (int i = 0; i < Clase.CANTIDAD; i++) {
            dibujarTarjeta(g, i);
        }
        TextoPixel.centrado(g, "← → elegir    ENTER empezar    ESC volver", ANCHO / 2.0, 604, 16, Paleta.PERGAMINO);
    }

    private void dibujarTarjeta(Graphics2D g, int i) {
        Clase clase = Clase.porOrden(i);
        int x = xTarjeta(i);
        boolean elegida = i == seleccion;
        int subida = elegida ? -10 : 0;
        DibujoMedieval.cuadroDialogo(g, x, Y_TARJETA + subida, ANCHO_TARJETA, ALTO_TARJETA);
        if (elegida) {
            g.setColor(Paleta.ORO);
            g.drawRect(x - 3, Y_TARJETA + subida - 3, ANCHO_TARJETA + 6, ALTO_TARJETA + 6);
        }
        double centro = x + ANCHO_TARJETA / 2.0;
        double flota = elegida ? Math.sin(tiempo * 4) * 4 : 0;
        DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(clase.getSprite(), 7), centro, Y_TARJETA + 80 + subida + flota);
        TextoPixel.centrado(g, clase.getNombre(), centro, Y_TARJETA + 160 + subida, 24, elegida ? Paleta.ORO : Paleta.HUESO);

        int y = Y_TARJETA + 190 + subida;
        // Las líneas de la tarjeta son largas: se compensa la escala global del texto para que sigan entrando
        int chico = (int) Math.round(14 / TextoPixel.getEscala());
        int parrafo = (int) Math.round(13 / TextoPixel.getEscala());
        TextoPixel.izquierda(g, "FUE " + clase.getFuerza() + "  DES " + clase.getDestreza() + "  INT " + clase.getInteligencia(),
                x + 18, y, chico, Paleta.HUESO);
        TextoPixel.izquierda(g, "Arma: " + clase.getNombreArma(), x + 18, y + 22, chico, Paleta.PERGAMINO);
        TextoPixel.izquierda(g, "Hechizo: " + CatalogoHabilidades.crear(clase.getHechizoInicial()).getNombre(),
                x + 18, y + 42, chico, Paleta.PERGAMINO);
        TextoPixel.parrafo(g, clase.getDescripcion(), x + 18, y + 72, ANCHO_TARJETA - 34, parrafo, Paleta.HUESO);
    }

    // Para las pruebas
    public int getSeleccion() {
        return seleccion;
    }
}

package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.entidades.Clase;
import juego.graficos.ImagenesUi;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.recursos.Recursos;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Elección de clase al empezar una partida nueva: Caballero, Mago, Druida o Pícaro.
// ←→ elige, ENTER confirma, ESC vuelve al menú. Cada clase cambia atributos, arma y primer hechizo.
public class EscenaClases extends Escena {

    // Constantes de dibujo: las cartas son imágenes de 252 x 471 (la parte visible) y van una al lado de la otra
    private static final int ANCHO = 1024;
    private static final int ANCHO_TARJETA = 252;
    private static final int ALTO_TARJETA = 471;
    private static final int X_INICIAL = 9;
    private static final int SEPARACION = 249;
    private static final int Y_TARJETA = 101;

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
        ImagenesUi ui = recursos.getUi();
        if (!ui.dibujarEstirada(g, "Fondos/FondoMinimapa.png", 0, 0, ANCHO, 640)) {
            g.setColor(Paleta.NEGRO);
            g.fillRect(0, 0, ANCHO, 640);
        }
        ui.dibujar(g, "MenuClase/Titulo.png", 316, 14);
        // Primero las no elegidas y al final la elegida, para que su resplandor quede por encima de las vecinas
        for (int i = 0; i < Clase.CANTIDAD; i++) {
            if (i != seleccion) {
                dibujarTarjeta(g, i);
            }
        }
        dibujarTarjeta(g, seleccion);
        TextoPixel.centrado(g, "← → elegir     ENTER empezar     ESC volver", ANCHO / 2.0, 612, 15, Paleta.PERGAMINO);
    }

    private void dibujarTarjeta(Graphics2D g, int i) {
        ImagenesUi ui = recursos.getUi();
        Clase clase = Clase.porOrden(i);
        int x = xTarjeta(i);
        boolean elegida = i == seleccion;
        double subida = elegida ? -8 + Math.sin(tiempo * 3) * 2 : 0;
        if (elegida) {
            // Resplandor dorado alrededor de la carta elegida
            for (int capa = 5; capa >= 1; capa--) {
                g.setColor(new Color(255, 214, 70, 22 + (5 - capa) * 8));
                g.fillRoundRect(x + 6 - capa * 3, (int) (Y_TARJETA + subida) + 14 - capa * 3,
                        ANCHO_TARJETA - 12 + capa * 6, ALTO_TARJETA - 28 + capa * 6, 24, 24);
            }
        }
        ui.dibujar(g, "MenuClase/" + archivoDe(clase) + ".png", x, Y_TARJETA + subida);
    }

    // Nombre del archivo de la carta de cada clase
    private String archivoDe(Clase clase) {
        if (clase == Clase.CABALLERO) {
            return "Caballero";
        }
        if (clase == Clase.MAGO) {
            return "Mago";
        }
        if (clase == Clase.DRUIDA) {
            return "Druida";
        }
        return "Picaro";
    }

    // Para las pruebas
    public int getSeleccion() {
        return seleccion;
    }
}

package juego.pantallas;

import estructuras.ListaEnlazada;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.recursos.Recursos;
import juego.entidades.Jugador;
import juego.graficos.ImagenesUi;
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
        ImagenesUi ui = recursos.getUi();
        // Fondo, título y cuadro de resumen: la victoria y la derrota usan el mismo diseño con imágenes distintas
        double yCuadro;
        if (victoria) {
            if (!ui.dibujarEstirada(g, "PantallaVictoria/FondoVictoria.png", 0, 0, ANCHO, 640)) {
                g.setColor(Paleta.NEGRO);
                g.fillRect(0, 0, ANCHO, 640);
            }
            ui.dibujar(g, "PantallaVictoria/TituloVictoria.png", 365, 6);
            yCuadro = 404;
            ui.dibujar(g, "PantallaVictoria/ContenedorVictoria.png", 112, yCuadro);
        } else {
            if (!ui.dibujarVisibleEstirada(g, "PantallaDerrota/FondoDerrota.png", 0, 0, ANCHO, 640, 0)) {
                g.setColor(Paleta.NEGRO);
                g.fillRect(0, 0, ANCHO, 640);
            }
            yCuadro = 370;
            ui.dibujar(g, "PantallaDerrota/ContenedorDerrota.png", 115, yCuadro);
        }

        // Resumen: las etiquetas en blanco y los números en dorado
        Jugador j = partida.getJugador();
        double y = yCuadro + 97;
        dato(g, 156, y, "Enemigos derrotados: ", String.valueOf(partida.getDerrotados()));
        dato(g, 156, y + 35, "Lugares recorridos: ", String.valueOf(partida.getRuta().tamanio() - 1));
        dato(g, 156, y + 70, "Oro recolectado: ", String.valueOf(j.getOro()));
        dato(g, 488, y, "FUE: ", j.getFuerza() + "   DES: " + j.getDestreza() + "   INT: " + j.getInteligencia());
        ListaEnlazada<Registro> ultimos = partida.getUltimosRegistros(1);
        if (!ultimos.estaVacia()) {
            TextoPixel.izquierda(g, "Último registro:", 488, y + 44, 15, Paleta.HUESO);
            TextoPixel.parrafo(g, ultimos.obtener(0).getTexto(), 488, y + 72, 380, 19, Paleta.ORO);
        }
        TextoPixel.centrado(g, "ENTER para volver al menú", ANCHO / 2.0, victoria ? 632 : 612, 15, Paleta.HUESO);
    }

    // Una línea del resumen: texto blanco y, a continuación, el dato en dorado
    private void dato(Graphics2D g, double x, double y, String etiqueta, String valor) {
        TextoPixel.izquierda(g, etiqueta, x, y, 20, Paleta.HUESO);
        TextoPixel.izquierda(g, valor, x + TextoPixel.anchoDe(g, etiqueta, 20), y, 20, Paleta.ORO);
    }

    public boolean esVictoria() {
        return victoria;
    }
}

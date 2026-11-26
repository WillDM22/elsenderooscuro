package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import juego.graficos.ImagenesUi;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.opciones.Opciones;
import juego.recursos.Recursos;
import motor.Entrada;
import motor.Escena;

// Pantalla de opciones: modo de pantalla ("Resolución"), brillo y tamaño del texto. Se abre desde el menú principal y desde el mapa (tecla O).
// ↑↓ elige la fila, ←→ cambia el valor, ESC (o ENTER sobre "Volver") vuelve a la pantalla anterior.
// Cada cambio se aplica y se guarda en opciones.txt en el momento.
public class EscenaOpciones extends Escena {

    // Filas del menú
    private static final int RESOLUCION = 0;
    private static final int BRILLO = 1;
    private static final int TEXTO = 2;
    private static final int VOLVER = 3;
    private static final int FILAS = 4;

    // Constantes de dibujo
    private static final int ANCHO = 1024;
    private static final int X_FILA = 175;
    // Columnas fijas de cada fila: flecha ◀, valor (centrado) y flecha ▶. No dependen del largo del texto,
    // así "izquierda del valor = bajar" y "derecha del valor = subir" valen igual en todas las filas.
    private static final int X_FLECHA_IZQ = 516;
    private static final int X_VALOR = 688;
    private static final int X_FLECHA_DER = 862;
    private static final int X_FIN_FILA = 890;
    private static final int Y_FILA = 245;
    private static final int SEPARACION = 70;

    // Atributos
    private final Recursos recursos;
    private final Escena anterior;
    private final Opciones opciones;
    private int fila;
    private double tiempo;

    // Constructor: "anterior" es la pantalla a la que se vuelve
    public EscenaOpciones(Recursos recursos, Escena anterior) {
        this.recursos = recursos;
        this.anterior = anterior;
        this.opciones = recursos.getOpciones();
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        Entrada entrada = getEntrada();

        if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)) {
            fila = (fila + FILAS - 1) % FILAS;
        }
        if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)) {
            fila = (fila + 1) % FILAS;
        }
        if (entrada.fuePresionada(KeyEvent.VK_LEFT) || entrada.fuePresionada(KeyEvent.VK_A)) {
            cambiar(-1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_RIGHT) || entrada.fuePresionada(KeyEvent.VK_D)) {
            cambiar(1);
        }
        manejarMouse(entrada);
        if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
            volver();
        } else if ((entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) && fila == VOLVER) {
            volver();
        }
    }

    // Mouse: click sobre una fila la elige; si el click cae a la izquierda del valor (donde está la flecha ◀) baja el valor,
    // y si cae a la derecha (flecha ▶) lo sube
    private void manejarMouse(Entrada entrada) {
        if (!entrada.huboClick()) {
            return;
        }
        for (int i = 0; i < FILAS; i++) {
            int y = Y_FILA + i * SEPARACION;
            if (entrada.getMouseY() >= y - 34 && entrada.getMouseY() <= y + 14
                    && entrada.getMouseX() >= X_FILA && entrada.getMouseX() <= X_FIN_FILA) {
                fila = i;
                if (i == VOLVER) {
                    volver();
                } else if (entrada.getMouseX() < X_VALOR) {
                    cambiar(-1);
                } else {
                    cambiar(1);
                }
                return;
            }
        }
    }

    private void cambiar(int delta) {
        if (fila == RESOLUCION) {
            opciones.cambiarResolucion(delta);
        } else if (fila == BRILLO) {
            opciones.cambiarBrillo(delta);
        } else if (fila == TEXTO) {
            opciones.cambiarTexto(delta);
        } else {
            return;
        }
        recursos.aplicarOpciones();
    }

    private void volver() {
        getContexto().irA(anterior);
    }

    @Override
    public void dibujar(Graphics2D g) {
        ImagenesUi ui = recursos.getUi();
        if (!ui.dibujarEstirada(g, "Fondos/FondoMinimapa.png", 0, 0, ANCHO, 640)) {
            g.setColor(Paleta.NEGRO);
            g.fillRect(0, 0, ANCHO, 640);
        }
        ui.dibujar(g, "Opciones/CuadroOpciones.png", 108, 108);
        TextoPixel.tituloCentrado(g, "Opciones", ANCHO / 2.0, 190, 46, Paleta.ORO, false);

        dibujarFila(g, RESOLUCION, "Resolución", opciones.getNombreResolucion());
        dibujarFila(g, BRILLO, "Brillo", opciones.getBrilloPorcentaje() + "%");
        dibujarFila(g, TEXTO, "Tamaño del texto", opciones.getNombreTexto());

        Color colorVolver = fila == VOLVER ? Paleta.ORO : Paleta.HUESO;
        if (fila == VOLVER) {
            flecha(g, X_FILA - 24, Y_FILA + VOLVER * SEPARACION - 8, false);
        }
        TextoPixel.izquierda(g, "Volver", X_FILA, Y_FILA + VOLVER * SEPARACION, 24, colorVolver);

        TextoPixel.centrado(g, "↑ ↓ elegir fila     ← → cambiar valor     ESC volver", ANCHO / 2.0, 604, 15, Paleta.PERGAMINO);
    }

    private void dibujarFila(Graphics2D g, int indice, String nombre, String valor) {
        int y = Y_FILA + indice * SEPARACION;
        boolean elegida = indice == fila;
        if (elegida) {
            flecha(g, X_FILA - 24, y - 8, false);
        }
        TextoPixel.izquierda(g, nombre, X_FILA, y, 24, elegida ? Paleta.ORO : Paleta.HUESO);
        if (elegida) {
            flecha(g, X_FLECHA_IZQ, y - 8, true);
            flecha(g, X_FLECHA_DER, y - 8, false);
        }
        TextoPixel.centrado(g, valor, X_VALOR, y, 24, Paleta.ORO);
    }

    // Flecha dorada (la imagen apunta a la derecha; para "◀" se da vuelta). Se centra en (cx, cy).
    private void flecha(Graphics2D g, double cx, double cy, boolean haciaLaIzquierda) {
        ImagenesUi ui = recursos.getUi();
        double vaiven = Math.sin(tiempo * 8) * 1.5 * (haciaLaIzquierda ? -1 : 1);
        if (haciaLaIzquierda) {
            AffineTransform anterior = g.getTransform();
            g.translate(cx * 2, 0);
            g.scale(-1, 1);
            ui.dibujarCentrado(g, "Opciones/IconoActivo_1.png", cx + vaiven, cy);
            g.setTransform(anterior);
        } else {
            ui.dibujarCentrado(g, "Opciones/IconoActivo_1.png", cx + vaiven, cy);
        }
    }

    // Para las pruebas
    public int getFila() {
        return fila;
    }
}

package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.graficos.DibujoMedieval;
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
    private static final int X_FILA = 170;
    // Columnas fijas de cada fila: flecha ◀, valor (centrado) y flecha ▶. No dependen del largo del texto,
    // así "izquierda del valor = bajar" y "derecha del valor = subir" valen igual en todas las filas.
    private static final int X_FLECHA_IZQ = 510;
    private static final int X_VALOR = 685;
    private static final int X_FLECHA_DER = 860;
    private static final int X_FIN_FILA = 890;
    private static final int Y_FILA = 250;
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
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(0, 0, ANCHO, 640);
        DibujoMedieval.cuadroDialogo(g, 120, 130, ANCHO - 240, 420);
        TextoPixel.centrado(g, "OPCIONES", ANCHO / 2.0, 190, 40, Paleta.ORO);

        dibujarFila(g, RESOLUCION, "Resolución", opciones.getNombreResolucion());
        dibujarFila(g, BRILLO, "Brillo", opciones.getBrilloPorcentaje() + "%");
        dibujarFila(g, TEXTO, "Tamaño del texto", opciones.getNombreTexto());

        Color colorVolver = fila == VOLVER ? Paleta.ORO : Paleta.HUESO;
        if (fila == VOLVER) {
            DibujoMedieval.flecha(g, X_FILA - 30, Y_FILA + VOLVER * SEPARACION - 9, 11 + Math.sin(tiempo * 8) * 1.5);
        }
        TextoPixel.izquierda(g, "Volver", X_FILA, Y_FILA + VOLVER * SEPARACION, 26, colorVolver);

        TextoPixel.centrado(g, "↑ ↓ elegir fila    ← → cambiar valor    ESC volver", ANCHO / 2.0, 604, 16, Paleta.PERGAMINO);
    }

    private void dibujarFila(Graphics2D g, int indice, String nombre, String valor) {
        int y = Y_FILA + indice * SEPARACION;
        boolean elegida = indice == fila;
        Color color = elegida ? Paleta.ORO : Paleta.HUESO;
        if (elegida) {
            DibujoMedieval.flecha(g, X_FILA - 30, y - 9, 11 + Math.sin(tiempo * 8) * 1.5);
        }
        TextoPixel.izquierda(g, nombre, X_FILA, y, 26, color);
        TextoPixel.centrado(g, "◀", X_FLECHA_IZQ, y, 26, color);
        TextoPixel.centrado(g, valor, X_VALOR, y, 26, color);
        TextoPixel.centrado(g, "▶", X_FLECHA_DER, y, 26, color);
    }

    // Para las pruebas
    public int getFila() {
        return fila;
    }
}

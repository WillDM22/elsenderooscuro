package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.recursos.Recursos;
import juego.archivos.GestorArchivos;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Menú principal: Nueva partida / Continuar / Opciones / Salir.
public class EscenaMenu extends Escena {

    // Constantes
    private static final int NUEVA = 0;
    private static final int CONTINUAR = 1;
    private static final int AJUSTES = 2;
    private static final int SALIR = 3;
    private static final int CANTIDAD = 4;
    private static final int ANCHO = 1024;
    private static final int ALTO = 640;
    private static final int X_MENU = 360;
    private static final int Y_MENU = 360;
    private static final int SEPARACION = 54;

    // Atributos
    private final Recursos recursos;
    private int seleccion;
    private double tiempo;
    private String aviso = "";
    private double tiempoAviso;

    // Constructor
    public EscenaMenu(Recursos recursos) {
        this.recursos = recursos;
    }

    @Override
    public void alEntrar() {
        // Si hay una partida guardada, se deja marcada "Continuar"
        if (recursos.getArchivos().existeGuardado()) {
            seleccion = CONTINUAR;
        }
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (tiempoAviso > 0) {
            tiempoAviso -= dt;
        }
        Entrada entrada = getEntrada();

        if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)) {
            seleccion = (seleccion + CANTIDAD - 1) % CANTIDAD;
        }
        if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)) {
            seleccion = (seleccion + 1) % CANTIDAD;
        }

        // Mouse: pasar por encima selecciona, hacer click confirma
        for (int i = 0; i < CANTIDAD; i++) {
            if (estaSobreOpcion(entrada, i)) {
                if (entrada.getMouseX() != 0 || entrada.getMouseY() != 0) {
                    seleccion = i;
                }
                if (entrada.huboClick()) {
                    seleccion = i;
                    confirmar();
                    return;
                }
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            confirmar();
        }
    }

    private boolean estaSobreOpcion(Entrada entrada, int indice) {
        int y = Y_MENU + indice * SEPARACION;
        return entrada.getMouseX() >= X_MENU && entrada.getMouseX() <= X_MENU + 300
                && entrada.getMouseY() >= y - 30 && entrada.getMouseY() <= y + 12;
    }

    private void confirmar() {
        GestorArchivos archivos = recursos.getArchivos();
        if (seleccion == NUEVA) {
            getContexto().irA(new EscenaClases(recursos));
        } else if (seleccion == CONTINUAR) {
            Partida partida = archivos.existeGuardado() ? archivos.cargar(recursos.getEventos()) : null;
            if (partida == null) {
                aviso = archivos.existeGuardado() ? "El archivo de guardado está dañado." : "No hay una partida guardada.";
                tiempoAviso = 3;
            } else {
                getContexto().irA(new EscenaMapa(recursos, partida, false));
            }
        } else if (seleccion == AJUSTES) {
            getContexto().irA(new EscenaOpciones(recursos, this));
        } else if (seleccion == SALIR) {
            getContexto().salir();
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(0, 0, ANCHO, ALTO);

        Adornos.antorcha(g, recursos, 150, 330, tiempo, 0);
        Adornos.antorcha(g, recursos, ANCHO - 150, 330, tiempo, 2);

        // Título
        TextoPixel.centrado(g, "EL SENDERO", ANCHO / 2.0, 100, 56, Paleta.ORO);
        TextoPixel.centrado(g, "OSCURO", ANCHO / 2.0, 160, 56, Paleta.ORO);
        TextoPixel.centrado(g, "Un roguelike de decisiones", ANCHO / 2.0, 196, 18, Paleta.PERGAMINO);

        // Héroe flotando suavemente a la izquierda y calavera a la derecha
        double flota = Math.sin(tiempo * 2.2) * 6;
        DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(CatalogoSprites.CABALLERO, 8), 190, 420 + flota);
        DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(CatalogoSprites.REY, 8), ANCHO - 190, 420 - flota);

        // Opciones
        DibujoMedieval.cuadroDialogo(g, X_MENU - 60, Y_MENU - 56, 420, 244);
        boolean hayGuardado = recursos.getArchivos().existeGuardado();
        for (int i = 0; i < CANTIDAD; i++) {
            Color color = Paleta.HUESO;
            if (i == CONTINUAR && !hayGuardado) {
                color = Paleta.PIEDRA;
            }
            if (i == seleccion) {
                color = Paleta.ORO;
                DibujoMedieval.flecha(g, X_MENU - 30, Y_MENU + i * SEPARACION - 9, 11 + Math.sin(tiempo * 8) * 1.5);
            }
            TextoPixel.izquierda(g, nombreOpcion(i), X_MENU, Y_MENU + i * SEPARACION, 28, color);
        }

        TextoPixel.centrado(g, "↑ ↓ elegir    ENTER confirmar", ANCHO / 2.0, 604, 16, Paleta.PERGAMINO);
        if (tiempoAviso > 0) {
            TextoPixel.centrado(g, aviso, ANCHO / 2.0, 580, 18, new Color(255, 120, 100));
        }
    }

    private String nombreOpcion(int i) {
        if (i == NUEVA) {
            return "Nueva partida";
        }
        if (i == CONTINUAR) {
            return "Continuar";
        }
        if (i == AJUSTES) {
            return "Opciones";
        }
        return "Salir";
    }

    // Para las pruebas
    public int getSeleccion() {
        return seleccion;
    }
}

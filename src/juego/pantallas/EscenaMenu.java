package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.recursos.Recursos;
import juego.archivos.GestorArchivos;
import juego.graficos.ImagenesUi;
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
    // Los botones forman una grilla de 2 columnas x 2 filas: Nueva partida / Continuar a la izquierda,
    // Opciones / Salir a la derecha. Se ubican por su centro.
    private static final int CENTRO_X_IZQUIERDA = 307;
    private static final int CENTRO_X_DERECHA = 710;
    private static final int CENTRO_Y_ARRIBA = 355;
    private static final int CENTRO_Y_ABAJO = 448;
    private static final int ANCHO_BOTON = 298;
    private static final int ALTO_BOTON = 91;

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
        // ← → pasan de la columna izquierda a la derecha (y al revés) en la misma fila
        if (entrada.fuePresionada(KeyEvent.VK_LEFT) || entrada.fuePresionada(KeyEvent.VK_A)
                || entrada.fuePresionada(KeyEvent.VK_RIGHT) || entrada.fuePresionada(KeyEvent.VK_D)) {
            seleccion = (seleccion + 2) % CANTIDAD;
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

    // Posición (centro) de cada botón: la columna depende del índice (0 y 1 a la izquierda, 2 y 3 a la derecha)
    private int centroX(int indice) {
        return indice < 2 ? CENTRO_X_IZQUIERDA : CENTRO_X_DERECHA;
    }

    private int centroY(int indice) {
        return indice % 2 == 0 ? CENTRO_Y_ARRIBA : CENTRO_Y_ABAJO;
    }

    private boolean estaSobreOpcion(Entrada entrada, int indice) {
        return Math.abs(entrada.getMouseX() - centroX(indice)) <= ANCHO_BOTON / 2
                && Math.abs(entrada.getMouseY() - centroY(indice)) <= ALTO_BOTON / 2;
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
        ImagenesUi ui = recursos.getUi();
        // Fondo (una captura con borde de ventana: se le sacan 2 px por lado y se estira a toda la pantalla)
        if (!ui.dibujarVisibleEstirada(g, "MenuTitulo/Fondo.png", 0, 0, ANCHO, ALTO, 2)) {
            g.setColor(Paleta.NEGRO);
            g.fillRect(0, 0, ANCHO, ALTO);
        }

        // Logo con un suave vaivén
        double flota = Math.sin(tiempo * 1.6) * 3;
        ui.dibujarCentrado(g, "MenuTitulo/Logo Juego.png", ANCHO / 2.0, 168 + flota);

        // Botones: oscuros con rombos, amarillos el elegido y sin rombos "Continuar" si no hay partida guardada
        boolean hayGuardado = recursos.getArchivos().existeGuardado();
        for (int i = 0; i < CANTIDAD; i++) {
            String estado = "Activo";
            if (i == seleccion) {
                estado = "Hold";
            } else if (i == CONTINUAR && !hayGuardado) {
                estado = "Normal";
            }
            ui.dibujarCentrado(g, "MenuTitulo/" + archivoDe(i) + estado + ".png", centroX(i), centroY(i));
        }

        if (tiempoAviso > 0) {
            TextoPixel.centrado(g, aviso, ANCHO / 2.0, 590, 20, new Color(255, 140, 120));
        }
    }

    // Primera parte del nombre de los archivos de cada botón
    private String archivoDe(int i) {
        if (i == NUEVA) {
            return "NJuego";
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

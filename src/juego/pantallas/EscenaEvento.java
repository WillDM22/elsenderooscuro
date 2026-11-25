package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.recursos.Recursos;
import juego.entidades.Enemigo;
import juego.entidades.FabricaEnemigos;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.reglas.EventoNarrativo;
import juego.reglas.NodoMapa;
import juego.reglas.OpcionEvento;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Escena de decisión: un relato breve y 2 o 3 opciones. Sirve para los eventos "?" y para los cofres.
public class EscenaEvento extends Escena {

    // Constantes
    private static final int ANCHO = 1024;
    private static final int ELEGIR = 0;
    private static final int RESULTADO = 1;
    private static final double LETRAS_POR_SEGUNDO = 55;

    // Atributos
    private final Recursos recursos;
    private final Partida partida;
    private final EventoNarrativo evento;
    private final boolean esCofre;
    private int fase = ELEGIR;
    private int seleccion;
    private double tiempo;
    private OpcionEvento elegida;
    private String resumen = "";

    // Constructor
    public EscenaEvento(Recursos recursos, Partida partida, EventoNarrativo evento, boolean esCofre) {
        this.recursos = recursos;
        this.partida = partida;
        this.evento = evento;
        this.esCofre = esCofre;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        Entrada entrada = getEntrada();

        if (fase == ELEGIR) {
            int n = evento.cantidadOpciones();
            if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)) {
                seleccion = (seleccion + n - 1) % n;
            }
            if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)) {
                seleccion = (seleccion + 1) % n;
            }
            for (int i = 0; i < n; i++) {
                if (entrada.fuePresionada(KeyEvent.VK_1 + i)) {
                    seleccion = i;
                    elegir();
                    return;
                }
                if (estaSobreOpcion(entrada, i)) {
                    seleccion = i;
                    if (entrada.huboClick()) {
                        elegir();
                        return;
                    }
                }
            }
            if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
                elegir();
            }
        } else if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)
                || entrada.huboClick()) {
            terminar();
        }
    }

    private boolean estaSobreOpcion(Entrada entrada, int indice) {
        int y = 424 + indice * 46;
        return entrada.getMouseX() >= 60 && entrada.getMouseX() <= 964
                && entrada.getMouseY() >= y - 26 && entrada.getMouseY() <= y + 14;
    }

    // El jugador decide: se aplican los efectos y se anota en el diario (una PILA)
    private void elegir() {
        OpcionEvento opcion = evento.getOpcion(seleccion);
        if (!opcion.puedeElegir(partida.getJugador())) {
            return;
        }
        elegida = opcion;
        resumen = opcion.aplicar(partida.getJugador());
        NodoMapa nodo = partida.getNodoActualCompleto();
        String nota = evento.getTitulo() + ": " + opcion.getTexto();
        if (!resumen.isEmpty()) {
            nota = nota + " (" + resumen + ")";
        }
        partida.registrar(nodo.getId(), nota);
        fase = RESULTADO;
        tiempo = 0;
    }

    // Vuelve al mapa, salvo que la opción haya provocado una emboscada
    private void terminar() {
        String emboscada = elegida.getEmboscada();
        if (emboscada != null) {
            NodoMapa nodo = partida.getNodoActualCompleto();
            Enemigo enemigo = FabricaEnemigos.crearPorNombre(emboscada, nodo.getPiso());
            getContexto().irA(new EscenaCombate(recursos, partida, enemigo, false));
        } else {
            getContexto().irA(new EscenaMapa(recursos, partida, true));
        }
    }

    // Dibujo
    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(0, 0, ANCHO, 640);
        Hud.dibujarFranja(g, recursos, partida.getJugador(), ANCHO);

        // Decorado a los costados del pergamino
        String decorado = esCofre ? CatalogoSprites.COFRE : CatalogoSprites.ALTAR;
        double flota = Math.sin(tiempo * 2) * 4;
        DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(decorado, 7), 98, 250 + flota);
        DibujoMedieval.dibujarConSombra(g, recursos.getSprites().obtener(CatalogoSprites.JUGADOR, 7), ANCHO - 98, 250 - flota);

        // Pergamino con el relato
        DibujoMedieval.marcoPergamino(g, 190, 100, 644, 256);
        TextoPixel.centrado(g, evento.getTitulo(), ANCHO / 2.0, 146, 26, Paleta.MADERA_OSCURA, false);
        if (fase == ELEGIR) {
            TextoPixel.parrafoEscribiendo(g, evento.getRelato(), 226, 190, 572, 18, Paleta.MADERA_OSCURA, letrasVisibles(), false);
        } else {
            double y = TextoPixel.parrafoEscribiendo(g, elegida.getResultado(), 226, 190, 572, 18, Paleta.MADERA_OSCURA, letrasVisibles(), false);
            if (!resumen.isEmpty()) {
                TextoPixel.izquierda(g, resumen, 226, y + 14, 20, new Color(120, 40, 20), false);
            }
            if (elegida.getEmboscada() != null) {
                TextoPixel.izquierda(g, "¡Algo se acerca entre los árboles!", 226, y + 46, 18, Paleta.ROJO_OSCURO, false);
            }
        }

        // Opciones
        DibujoMedieval.cuadroDialogo(g, 40, 380, ANCHO - 80, 200);
        if (fase == ELEGIR) {
            dibujarOpciones(g);
        } else {
            TextoPixel.centrado(g, "ENTER para continuar", ANCHO / 2.0, 490, 22, Paleta.ORO);
        }
    }

    private void dibujarOpciones(Graphics2D g) {
        for (int i = 0; i < evento.cantidadOpciones(); i++) {
            OpcionEvento opcion = evento.getOpcion(i);
            boolean disponible = opcion.puedeElegir(partida.getJugador());
            Color color = disponible ? Paleta.HUESO : Paleta.PIEDRA;
            String texto = (i + 1) + ". " + opcion.getTexto();
            if (!disponible) {
                texto = texto + "  (te falta oro)";
            }
            if (i == seleccion) {
                if (disponible) {
                    color = Paleta.ORO;
                }
                DibujoMedieval.flecha(g, 66, 424 + i * 46 - 7, 11 + Math.sin(tiempo * 8) * 1.5);
            }
            TextoPixel.izquierda(g, texto, 100, 424 + i * 46, 21, color);
        }
        TextoPixel.centrado(g, "↑↓ o 1-3 para elegir, ENTER para decidir", ANCHO / 2.0, 566, 14, Paleta.PERGAMINO);
    }

    // Efecto máquina de escribir: cuántas letras se ven según el tiempo transcurrido
    private int letrasVisibles() {
        return (int) (tiempo * LETRAS_POR_SEGUNDO);
    }

    // Para las pruebas
    public int getFase() {
        return fase;
    }

    public int getSeleccion() {
        return seleccion;
    }
}

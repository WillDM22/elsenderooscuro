package juego.pantallas;

import estructuras.ColaEnlazada;
import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import juego.recursos.Recursos;
import juego.entidades.Enemigo;
import juego.entidades.Habilidad;
import juego.entidades.Jugador;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.reglas.BarraPrecision;
import juego.reglas.Combate;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Pantalla de combate. La lógica está en la clase Combate; esta escena solo lee las teclas,
// se las pasa al Combate y dibuja el estado (sprites, barras, menú de habilidades y la barra de precisión).
public class EscenaCombate extends Escena {

    // Número que aparece flotando sobre un personaje cuando recibe daño
    private static class Flotante {
        private final String texto;
        private final double x;
        private double y;
        private double vida = 1.1;
        private final Color color;

        private Flotante(String texto, double x, double y, Color color) {
            this.texto = texto;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    // Constantes
    private static final int ANCHO = 1024;
    private static final double LETRAS_POR_SEGUNDO = 70;
    private static final double ESPERA_ENEMIGO = 0.9;

    // Atributos
    private final Recursos recursos;
    private final Partida partida;
    private final Combate combate;
    private final ListaEnlazada<Flotante> flotantes = new ListaEnlazada<>();
    private int seleccion;
    private double tiempo;
    private double tiempoTexto;
    private double esperaEnemigo;
    private double sacudidaEnemigo;
    private double sacudidaJugador;
    private int vidaEnemigoPrevia;
    private int vidaJugadorPrevia;
    private String mejora;
    private boolean victoriaRegistrada;

    // Constructor
    public EscenaCombate(Recursos recursos, Partida partida, Enemigo enemigo, boolean esJefe) {
        this.recursos = recursos;
        this.partida = partida;
        this.combate = new Combate(partida.getJugador(), enemigo, esJefe);
        this.vidaEnemigoPrevia = enemigo.getVida();
        this.vidaJugadorPrevia = partida.getJugador().getVida();
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        tiempoTexto += dt;
        Entrada entrada = getEntrada();
        combate.actualizar(dt);
        detectarGolpes();
        actualizarFlotantes(dt);

        int estado = combate.getEstado();
        if (estado == Combate.ELECCION) {
            manejarMenu(entrada);
        } else if (estado == Combate.APUNTANDO) {
            if (entrada.fuePresionada(KeyEvent.VK_SPACE) || entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.huboClick()) {
                combate.detenerBarra();
                tiempoTexto = 0;
            }
        } else if (estado == Combate.TURNO_ENEMIGO) {
            esperaEnemigo += dt;
            if (esperaEnemigo >= ESPERA_ENEMIGO || teclaContinuar(entrada)) {
                esperaEnemigo = 0;
                combate.continuar();
                tiempoTexto = 0;
            }
        } else {
            manejarFinDeMensaje(entrada, estado);
        }
    }

    private boolean teclaContinuar(Entrada entrada) {
        return entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE) || entrada.huboClick();
    }

    // Menú de habilidades: 2 columnas, se mueve con las flechas o con los números 1 a 4
    private void manejarMenu(Entrada entrada) {
        Jugador jugador = partida.getJugador();
        int n = jugador.cantidadHabilidades();
        if (entrada.fuePresionada(KeyEvent.VK_LEFT) || entrada.fuePresionada(KeyEvent.VK_A)) {
            seleccion = Math.max(0, seleccion - 1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_RIGHT) || entrada.fuePresionada(KeyEvent.VK_D)) {
            seleccion = Math.min(n - 1, seleccion + 1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)) {
            seleccion = Math.max(0, seleccion - 2);
        }
        if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)) {
            seleccion = Math.min(n - 1, seleccion + 2);
        }
        for (int i = 0; i < n; i++) {
            if (entrada.fuePresionada(KeyEvent.VK_1 + i)) {
                seleccion = i;
                usar(i);
                return;
            }
            if (estaSobreHabilidad(entrada, i)) {
                seleccion = i;
                if (entrada.huboClick()) {
                    usar(i);
                    return;
                }
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            usar(seleccion);
        }
    }

    private void usar(int indice) {
        combate.elegirHabilidad(indice);
        tiempoTexto = 0;
    }

    private int xHabilidad(int i) {
        return 90 + (i % 2) * 440;
    }

    private int yHabilidad(int i) {
        return 492 + (i / 2) * 44;
    }

    private boolean estaSobreHabilidad(Entrada entrada, int i) {
        return Math.abs(entrada.getMouseX() - (xHabilidad(i) + 190)) < 200
                && Math.abs(entrada.getMouseY() - (yHabilidad(i) - 8)) < 20;
    }

    // Mensajes finales: después de leerlos se pasa a lo que sigue
    private void manejarFinDeMensaje(Entrada entrada, int estado) {
        if (estado == Combate.VICTORIA && !victoriaRegistrada) {
            registrarVictoria();
        }
        if (!teclaContinuar(entrada)) {
            return;
        }
        if (tiempoTexto < 0.25) {
            return;
        }
        if (estado == Combate.RESULTADO) {
            combate.continuar();
            tiempoTexto = 0;
        } else if (estado == Combate.VICTORIA) {
            if (combate.esJefe()) {
                recursos.getArchivos().borrarGuardado();
                getContexto().irA(new EscenaFin(recursos, partida, true));
            } else {
                getContexto().irA(new EscenaMapa(recursos, partida, true));
            }
        } else if (estado == Combate.HUIDA) {
            partida.registrar(partida.getNodoActual(), "Huiste de " + combate.getEnemigo().getNombre() + ".");
            getContexto().irA(new EscenaMapa(recursos, partida, true));
        } else if (estado == Combate.DERROTA) {
            recursos.getArchivos().borrarGuardado();
            getContexto().irA(new EscenaFin(recursos, partida, false));
        }
    }

    // Se cuenta el enemigo derrotado (y quizá el héroe mejora un atributo) una sola vez por combate
    private void registrarVictoria() {
        victoriaRegistrada = true;
        mejora = partida.sumarDerrotado();
        partida.registrar(partida.getNodoActual(), "Derrotaste a " + combate.getEnemigo().getNombre() + ".");
        tiempoTexto = 0;
    }

    // Sacudidas y números flotantes cuando cambia la vida de alguien
    private void detectarGolpes() {
        Enemigo enemigo = combate.getEnemigo();
        Jugador jugador = partida.getJugador();
        if (enemigo.getVida() < vidaEnemigoPrevia) {
            sacudidaEnemigo = 0.4;
            flotantes.agregar(new Flotante("-" + (vidaEnemigoPrevia - enemigo.getVida()), 740, 230, Paleta.ORO));
        }
        if (jugador.getVida() < vidaJugadorPrevia) {
            sacudidaJugador = 0.4;
            flotantes.agregar(new Flotante("-" + (vidaJugadorPrevia - jugador.getVida()), 250, 260, new Color(255, 90, 90)));
        } else if (jugador.getVida() > vidaJugadorPrevia) {
            flotantes.agregar(new Flotante("+" + (jugador.getVida() - vidaJugadorPrevia), 250, 260, new Color(120, 255, 140)));
        }
        vidaEnemigoPrevia = enemigo.getVida();
        vidaJugadorPrevia = jugador.getVida();
    }

    private void actualizarFlotantes(double dt) {
        sacudidaEnemigo = Math.max(0, sacudidaEnemigo - dt);
        sacudidaJugador = Math.max(0, sacudidaJugador - dt);
        int i = 0;
        while (i < flotantes.tamanio()) {
            Flotante f = flotantes.obtener(i);
            f.vida = f.vida - dt;
            f.y = f.y - 40 * dt;
            if (f.vida <= 0) {
                flotantes.eliminar(i);
            } else {
                i++;
            }
        }
    }

    // Dibujo
    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(recursos.getFondoBosque(), 0, 0, null);
        g.setColor(new Color(0, 0, 20, 120));
        g.fillRect(0, 0, ANCHO, 640);
        Hud.dibujarFranja(g, recursos, partida.getJugador(), ANCHO);

        dibujarOrdenDeTurnos(g);
        dibujarEnemigo(g);
        dibujarHeroe(g);
        for (int i = 0; i < flotantes.tamanio(); i++) {
            Flotante f = flotantes.obtener(i);
            TextoPixel.centrado(g, f.texto, f.x, f.y, 34, f.color);
        }
        dibujarPanelInferior(g);
    }

    // Cola de turnos: arriba se ve quién actúa ahora y quién sigue (la primera ficha es el frente de la COLA)
    private void dibujarOrdenDeTurnos(Graphics2D g) {
        ColaEnlazada<Integer> orden = combate.getOrdenDeTurnos();
        TextoPixel.izquierda(g, "Turnos:", 640, 104, 14, Paleta.PERGAMINO);
        int x = 720;
        boolean primero = true;
        while (!orden.estaVacia()) {
            int quien = orden.desencolar();
            Color color = quien == Combate.JUGADOR ? Paleta.AZUL : Paleta.ROJO;
            g.setColor(Paleta.NEGRO);
            g.fillOval(x - 3, 86, 28, 28);
            g.setColor(color);
            g.fillOval(x, 89, 22, 22);
            if (primero) {
                g.setColor(Paleta.ORO);
                g.drawOval(x - 3, 86, 28, 28);
                primero = false;
            }
            TextoPixel.izquierda(g, quien == Combate.JUGADOR ? "T" : "E", x + 6, 106, 14, Paleta.HUESO);
            x = x + 36;
        }
    }

    private void dibujarEnemigo(Graphics2D g) {
        Enemigo enemigo = combate.getEnemigo();
        double flota = Math.sin(tiempo * 2.5) * 5;
        double temblor = sacudidaEnemigo > 0 ? Math.sin(tiempo * 90) * 10 * sacudidaEnemigo / 0.4 : 0;
        boolean muerto = enemigo.estaMuerto();
        BufferedImage sprite = recursos.getSprites().obtener(enemigo.getSprite(), 10);
        if (!muerto || (int) (tiempo * 10) % 2 == 0) {
            DibujoMedieval.dibujarConSombra(g, sprite, 760 + temblor, 280 + flota);
        }
        TextoPixel.centrado(g, enemigo.getNombre(), 760, 134, 22, Paleta.HUESO);
        DibujoMedieval.barra(g, 660, 144, 200, 14, enemigo.getVida(), enemigo.getVidaMaxima(), Paleta.ROJO, Paleta.ROJO_OSCURO);
        TextoPixel.centrado(g, enemigo.getVida() + "/" + enemigo.getVidaMaxima(), 760, 156, 12, Paleta.HUESO);
    }

    private void dibujarHeroe(Graphics2D g) {
        double flota = Math.sin(tiempo * 2.1 + 1) * 4;
        double temblor = sacudidaJugador > 0 ? Math.sin(tiempo * 90) * 10 * sacudidaJugador / 0.4 : 0;
        BufferedImage sprite = recursos.getSprites().obtener(CatalogoSprites.JUGADOR, 10);
        DibujoMedieval.dibujarConSombra(g, sprite, 260 + temblor, 300 + flota);
    }

    private void dibujarPanelInferior(Graphics2D g) {
        DibujoMedieval.cuadroDialogo(g, 8, 430, ANCHO - 16, 202);
        int estado = combate.getEstado();
        if (estado == Combate.ELECCION) {
            dibujarMenuHabilidades(g);
        } else if (estado == Combate.APUNTANDO) {
            TextoPixel.centrado(g, combate.getMensaje(), ANCHO / 2.0, 470, 20, Paleta.ORO);
            dibujarBarra(g, combate.getBarra());
        } else {
            String texto = combate.getMensaje();
            if (estado == Combate.TURNO_ENEMIGO) {
                texto = combate.getEnemigo().getNombre() + " se prepara para atacar...";
            }
            if (estado == Combate.VICTORIA && mejora != null) {
                texto = texto + " " + mejora;
            }
            int letras = (int) (tiempoTexto * LETRAS_POR_SEGUNDO);
            TextoPixel.parrafoEscribiendo(g, texto, 40, 490, ANCHO - 90, 22, Paleta.HUESO, letras);
            if (estado != Combate.TURNO_ENEMIGO && letras > texto.length()) {
                TextoPixel.derecha(g, "ENTER ▶", ANCHO - 40, 612, 18, Paleta.ORO);
            }
        }
    }

    private void dibujarMenuHabilidades(Graphics2D g) {
        Jugador jugador = partida.getJugador();
        TextoPixel.izquierda(g, combate.getMensaje(), 30, 462, 16, Paleta.PERGAMINO);
        for (int i = 0; i < jugador.cantidadHabilidades(); i++) {
            Habilidad h = jugador.getHabilidad(i);
            boolean alcanza = jugador.puedePagar(h.getCostoMana());
            Color color = alcanza ? Paleta.HUESO : Paleta.PIEDRA;
            if (i == seleccion) {
                if (alcanza) {
                    color = Paleta.ORO;
                }
                DibujoMedieval.flecha(g, xHabilidad(i) - 30, yHabilidad(i) - 8, 10 + Math.sin(tiempo * 8) * 1.5);
            }
            String costo = h.getCostoMana() == 0 ? "" : "  (" + h.getCostoMana() + " maná)";
            TextoPixel.izquierda(g, (i + 1) + ". " + h.getNombre() + costo, xHabilidad(i), yHabilidad(i), 21, color);
        }
        TextoPixel.centrado(g, jugador.getHabilidad(seleccion).getDescripcion(), ANCHO / 2.0, 612, 15, Paleta.PERGAMINO);
    }

    // La barra de precisión estilo Undertale: zona normal, zona crítica central y cursor
    private void dibujarBarra(Graphics2D g, BarraPrecision barra) {
        int x = (ANCHO - (int) BarraPrecision.ANCHO) / 2;
        int y = 510;
        int alto = 56;
        g.setColor(Paleta.NEGRO);
        g.fillRect(x - 5, y - 5, (int) BarraPrecision.ANCHO + 10, alto + 10);
        g.setColor(new Color(60, 30, 34));
        g.fillRect(x, y, (int) BarraPrecision.ANCHO, alto);
        double centro = x + BarraPrecision.ANCHO / 2;
        g.setColor(new Color(176, 130, 40));
        g.fillRect((int) (centro - barra.getAnchoNormal() / 2), y, (int) barra.getAnchoNormal(), alto);
        g.setColor(new Color(235, 80, 50));
        g.fillRect((int) (centro - barra.getAnchoCritico() / 2), y, (int) barra.getAnchoCritico(), alto);
        g.setColor(Paleta.HUESO);
        g.fillRect((int) centro - 1, y, 3, alto);

        // Cursor
        int cursorX = (int) (x + barra.getPosicion());
        g.setColor(Paleta.NEGRO);
        g.fillRect(cursorX - 7, y - 10, 14, alto + 20);
        g.setColor(Color.WHITE);
        g.fillRect(cursorX - 4, y - 7, 8, alto + 14);
        TextoPixel.centrado(g, "¡ESPACIO para frenar!", ANCHO / 2.0, 604, 18, Paleta.PERGAMINO);
    }

    // Para las pruebas
    public Combate getCombate() {
        return combate;
    }

    public int getSeleccion() {
        return seleccion;
    }
}

package juego.pantallas;

import estructuras.ColaEnlazada;
import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import juego.entidades.Calidad;
import juego.entidades.Enemigo;
import juego.entidades.Habilidad;
import juego.entidades.Jugador;
import juego.graficos.DibujoMedieval;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.recursos.Recursos;
import juego.reglas.BarraPrecision;
import juego.reglas.Combate;
import juego.reglas.Dificultad;
import juego.reglas.Minijuego;
import juego.reglas.MinijuegoAnillo;
import juego.reglas.MinijuegoRafaga;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;

// Pantalla de combate. La lógica está en la clase Combate; esta escena solo lee las teclas,
// se las pasa al Combate y dibuja el estado (sprites, barras, menús y el minijuego en curso).
//
// El menú de acciones es de dos niveles, como en Pokémon:
//   PRINCIPAL: LUCHAR | MAGIA | DEFENDER | HUIR
//   LUCHAR:    arma | puños | volver      MAGIA: hechizos | volver
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

    // Menús
    private static final int PRINCIPAL = 0;
    private static final int LUCHAR = 1;
    private static final int MAGIA = 2;

    // Opciones del menú principal
    private static final int OP_LUCHAR = 0;
    private static final int OP_MAGIA = 1;
    private static final int OP_DEFENDER = 2;
    private static final int OP_HUIR = 3;

    // Constantes
    private static final int ANCHO = 1024;
    private static final double LETRAS_POR_SEGUNDO = 70;
    private static final double ESPERA_ENEMIGO = 0.9;

    // Atributos
    private final Recursos recursos;
    private final Partida partida;
    private final Combate combate;
    private final ListaEnlazada<Flotante> flotantes = new ListaEnlazada<>();
    private int menu = PRINCIPAL;
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

    // Constructor: el nivel de dificultad de los minijuegos es la cantidad de enemigos derrotados
    public EscenaCombate(Recursos recursos, Partida partida, Enemigo enemigo, boolean esJefe) {
        this(recursos, partida, enemigo, esJefe, new Random());
    }

    // Igual, pero con el generador de azar a elección (las pruebas lo fijan para que salga siempre la barra)
    public EscenaCombate(Recursos recursos, Partida partida, Enemigo enemigo, boolean esJefe, Random azar) {
        this.recursos = recursos;
        this.partida = partida;
        this.combate = new Combate(partida.getJugador(), enemigo, esJefe, partida.getDerrotados(), azar);
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
        if (estado != Combate.ELECCION) {
            menu = PRINCIPAL;
            seleccion = 0;
        }
        if (estado == Combate.ELECCION) {
            manejarMenu(entrada);
        } else if (estado == Combate.APUNTANDO) {
            if (entrada.fuePresionada(KeyEvent.VK_SPACE) || entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.huboClick()) {
                combate.pulsar();
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
        // Si un minijuego terminó solo por tiempo (ráfaga o anillo), se reinicia el texto del resultado
        if (estado == Combate.APUNTANDO && combate.getEstado() != Combate.APUNTANDO) {
            tiempoTexto = 0;
        }
    }

    private boolean teclaContinuar(Entrada entrada) {
        return entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE) || entrada.huboClick();
    }

    // Menús de acción
    // Cuántas filas tiene el menú actual
    private int cantidadItems() {
        if (menu == PRINCIPAL) {
            return 4;
        }
        if (menu == LUCHAR) {
            return 3;
        }
        return partida.getJugador().cantidadHabilidades() + 1;
    }

    // Posición de cada fila. El menú principal es una grilla de 2x2; los submenús son listas verticales.
    private int xItem(int i) {
        if (menu == PRINCIPAL) {
            return 560 + (i % 2) * 215;
        }
        return 580;
    }

    private int yItem(int i) {
        if (menu == PRINCIPAL) {
            return 505 + (i / 2) * 56;
        }
        return 478 + i * 33;
    }

    private boolean estaSobreItem(Entrada entrada, int i) {
        return Math.abs(entrada.getMouseX() - (xItem(i) + 100)) < 110
                && Math.abs(entrada.getMouseY() - (yItem(i) - 8)) < 17;
    }

    private void manejarMenu(Entrada entrada) {
        int n = cantidadItems();
        if (seleccion >= n) {
            seleccion = n - 1;
        }
        if (menu == PRINCIPAL) {
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
        } else {
            if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_W)
                    || entrada.fuePresionada(KeyEvent.VK_LEFT) || entrada.fuePresionada(KeyEvent.VK_A)) {
                seleccion = Math.max(0, seleccion - 1);
            }
            if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_S)
                    || entrada.fuePresionada(KeyEvent.VK_RIGHT) || entrada.fuePresionada(KeyEvent.VK_D)) {
                seleccion = Math.min(n - 1, seleccion + 1);
            }
            if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || entrada.fuePresionada(KeyEvent.VK_BACK_SPACE)) {
                volverAlPrincipal();
                return;
            }
        }
        for (int i = 0; i < n; i++) {
            if (entrada.fuePresionada(KeyEvent.VK_1 + i)) {
                seleccion = i;
                confirmar();
                return;
            }
            if (estaSobreItem(entrada, i)) {
                seleccion = i;
                if (entrada.huboClick()) {
                    confirmar();
                    return;
                }
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            confirmar();
        }
    }

    private void volverAlPrincipal() {
        int anterior = menu;
        menu = PRINCIPAL;
        seleccion = anterior == LUCHAR ? OP_LUCHAR : OP_MAGIA;
    }

    // Ejecuta la fila elegida del menú actual
    private void confirmar() {
        tiempoTexto = 0;
        if (menu == PRINCIPAL) {
            if (seleccion == OP_LUCHAR) {
                menu = LUCHAR;
                seleccion = 0;
            } else if (seleccion == OP_MAGIA) {
                menu = MAGIA;
                seleccion = 0;
            } else if (seleccion == OP_DEFENDER) {
                combate.defender();
            } else if (seleccion == OP_HUIR) {
                combate.huir();
            }
        } else if (menu == LUCHAR) {
            if (seleccion == 0) {
                combate.elegirArma();
            } else if (seleccion == 1) {
                combate.elegirPunos();
            } else {
                volverAlPrincipal();
            }
        } else {
            if (seleccion < partida.getJugador().cantidadHabilidades()) {
                combate.elegirHabilidad(seleccion);
            } else {
                volverAlPrincipal();
            }
        }
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
        Hud.dibujarFranja(g, recursos, partida.getJugador(), ANCHO, true);

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
        TextoPixel.izquierda(g, "Peligro " + combate.getNivel() + "/" + Dificultad.NIVEL_MAXIMO, 24, 104, 14, Paleta.PERGAMINO);
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
        if (enemigo.estaEnvenenado()) {
            TextoPixel.centrado(g, "ENVENENADO", 760, 176, 13, new Color(130, 230, 90));
        }
    }

    private void dibujarHeroe(Graphics2D g) {
        Jugador jugador = partida.getJugador();
        double flota = Math.sin(tiempo * 2.1 + 1) * 4;
        double temblor = sacudidaJugador > 0 ? Math.sin(tiempo * 90) * 10 * sacudidaJugador / 0.4 : 0;
        BufferedImage sprite = recursos.getSprites().obtener(jugador.getClase().getSprite(), 10);
        DibujoMedieval.dibujarConSombra(g, sprite, 260 + temblor, 300 + flota);
        if (jugador.estaEnGuardia()) {
            TextoPixel.centrado(g, "GUARDIA", 260, 395, 14, new Color(120, 170, 255));
        }
        if (jugador.estaEnvenenado()) {
            TextoPixel.centrado(g, "ENVENENADO", 260, 413, 13, new Color(130, 230, 90));
        }
    }

    private void dibujarPanelInferior(Graphics2D g) {
        DibujoMedieval.cuadroDialogo(g, 8, 430, ANCHO - 16, 202);
        int estado = combate.getEstado();
        if (estado == Combate.ELECCION) {
            dibujarMenu(g);
        } else if (estado == Combate.APUNTANDO) {
            dibujarMinijuego(g, combate.getMinijuego());
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

    // Menú de dos niveles: a la izquierda el mensaje y la ayuda de la fila elegida; a la derecha las filas
    private void dibujarMenu(Graphics2D g) {
        Jugador jugador = partida.getJugador();
        String titulo = "¿Qué hará " + jugador.getClase().getNombre() + "?";
        TextoPixel.izquierda(g, titulo, 34, 466, 20, Paleta.ORO);
        TextoPixel.parrafo(g, combate.getMensaje(), 34, 496, 500, 16, Paleta.HUESO);
        TextoPixel.parrafo(g, ayudaDeLaFila(), 34, 570, 500, 15, Paleta.PERGAMINO);

        int n = cantidadItems();
        for (int i = 0; i < n; i++) {
            boolean activo = filaDisponible(i);
            Color color = activo ? Paleta.HUESO : Paleta.PIEDRA;
            if (i == seleccion) {
                if (activo) {
                    color = Paleta.ORO;
                }
                DibujoMedieval.flecha(g, xItem(i) - 26, yItem(i) - 8, 10 + Math.sin(tiempo * 8) * 1.5);
            }
            int tamanio = menu == PRINCIPAL ? 24 : 20;
            TextoPixel.izquierda(g, textoDeLaFila(i), xItem(i), yItem(i), tamanio, color);
        }
        if (menu != PRINCIPAL) {
            TextoPixel.derecha(g, "ESC volver", ANCHO - 30, 620, 13, Paleta.PERGAMINO);
        }
    }

    private String textoDeLaFila(int i) {
        Jugador jugador = partida.getJugador();
        if (menu == PRINCIPAL) {
            if (i == OP_LUCHAR) {
                return "LUCHAR";
            }
            if (i == OP_MAGIA) {
                return "MAGIA";
            }
            if (i == OP_DEFENDER) {
                return "DEFENDER";
            }
            return "HUIR";
        }
        if (menu == LUCHAR) {
            if (i == 0) {
                return jugador.getArma().getNombre() + " (" + jugador.getArma().getCostoEnergia() + " ene)";
            }
            if (i == 1) {
                return jugador.getPunos().getNombre() + " (" + jugador.getPunos().getCostoEnergia() + " ene)";
            }
            return "◀ Volver";
        }
        if (i < jugador.cantidadHabilidades()) {
            Habilidad h = jugador.getHabilidad(i);
            return h.getNombre() + " (" + h.getCostoMana() + " maná)";
        }
        return "◀ Volver";
    }

    // Una fila está "apagada" si el héroe no puede pagar su costo
    private boolean filaDisponible(int i) {
        Jugador jugador = partida.getJugador();
        if (menu == PRINCIPAL) {
            if (i == OP_HUIR) {
                return !combate.esJefe();
            }
            return true;
        }
        if (menu == LUCHAR) {
            if (i == 0) {
                return jugador.puedePagarEnergia(jugador.getArma().getCostoEnergia());
            }
            if (i == 1) {
                return jugador.puedePagarEnergia(jugador.getPunos().getCostoEnergia());
            }
            return true;
        }
        if (i < jugador.cantidadHabilidades()) {
            return jugador.puedePagar(jugador.getHabilidad(i).getCostoMana());
        }
        return true;
    }

    private String ayudaDeLaFila() {
        Jugador jugador = partida.getJugador();
        if (menu == PRINCIPAL) {
            if (seleccion == OP_LUCHAR) {
                return "Ataca con tu arma o con los puños. Cuesta ENERGÍA.";
            }
            if (seleccion == OP_MAGIA) {
                return "Usa un hechizo de tu repertorio. Cuesta MANÁ.";
            }
            if (seleccion == OP_DEFENDER) {
                return "Recibes la mitad de daño esta ronda y recuperas " + Combate.ENERGIA_DEFENSA + " de energía.";
            }
            if (combate.esJefe()) {
                return "No se puede huir de un jefe.";
            }
            return "Intentas escapar: " + combate.getProbabilidadDeHuir() + "% de éxito. Si falla, pierdes el turno.";
        }
        if (menu == LUCHAR) {
            if (seleccion == 0) {
                return jugador.getArma().getDescripcion();
            }
            if (seleccion == 1) {
                return jugador.getPunos().getDescripcion();
            }
            return "Volver al menú anterior.";
        }
        if (seleccion < jugador.cantidadHabilidades()) {
            return jugador.getHabilidad(seleccion).getDescripcion();
        }
        return "Volver al menú anterior.";
    }

    // Cada minijuego se dibuja distinto; la escena pregunta el tipo (sin instanceof) y pide el objeto del tipo correcto
    private void dibujarMinijuego(Graphics2D g, Minijuego minijuego) {
        TextoPixel.centrado(g, minijuego.getInstruccion(), ANCHO / 2.0, 462, 20, Paleta.ORO);
        if (minijuego.getTipo() == Minijuego.TIPO_BARRA) {
            dibujarBarra(g, combate.getBarra());
        } else if (minijuego.getTipo() == Minijuego.TIPO_RAFAGA) {
            dibujarRafaga(g, combate.getRafaga());
        } else {
            dibujarAnillo(g, combate.getAnillo());
        }
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
    }

    // Ráfaga: una barra que se llena con cada golpe, con marcas para "normal" y "crítico", y el tiempo que queda
    private void dibujarRafaga(Graphics2D g, MinijuegoRafaga rafaga) {
        int x = 212;
        int y = 500;
        int ancho = 600;
        int alto = 44;
        g.setColor(Paleta.NEGRO);
        g.fillRect(x - 5, y - 5, ancho + 10, alto + 10);
        g.setColor(new Color(60, 30, 34));
        g.fillRect(x, y, ancho, alto);
        double llenado = Math.min(1.0, (double) rafaga.getGolpes() / rafaga.getObjetivoCritico());
        g.setColor(llenado * rafaga.getObjetivoCritico() >= rafaga.getObjetivoNormal() ? new Color(235, 80, 50) : new Color(176, 130, 40));
        g.fillRect(x, y, (int) (ancho * llenado), alto);
        int marcaNormal = x + ancho * rafaga.getObjetivoNormal() / rafaga.getObjetivoCritico();
        g.setColor(Paleta.HUESO);
        g.fillRect(marcaNormal - 1, y - 8, 3, alto + 16);
        TextoPixel.centrado(g, "normal", marcaNormal, y + alto + 22, 13, Paleta.PERGAMINO);
        TextoPixel.centrado(g, "crítico", x + ancho, y + alto + 22, 13, Paleta.PERGAMINO);
        TextoPixel.izquierda(g, rafaga.getGolpes() + " / " + rafaga.getObjetivoCritico(), x + 8, y + 31, 22, Paleta.HUESO);

        // Tiempo restante
        double resto = rafaga.getTiempoRestante() / MinijuegoRafaga.DURACION;
        g.setColor(Paleta.NEGRO);
        g.fillRect(x - 3, 598, ancho + 6, 16);
        g.setColor(new Color(90, 150, 220));
        g.fillRect(x, 601, (int) (ancho * resto), 10);
    }

    // Anillo: el anillo blanco se achica hacia la marca dorada (la zona crítica es el borde rojo de la marca).
    // Se dibuja al 60% del tamaño real para que entre en el panel.
    private void dibujarAnillo(Graphics2D g, MinijuegoAnillo anillo) {
        double escala = 0.6;
        int cx = ANCHO / 2;
        int cy = 545;
        double rObjetivo = MinijuegoAnillo.RADIO_OBJETIVO * escala;
        double rNormal = anillo.getToleranciaNormal() * escala;
        double rCritico = anillo.getToleranciaCritica() * escala;
        // Zona normal (dorada) y crítica (roja) alrededor de la marca
        g.setColor(new Color(176, 130, 40, 150));
        g.fillOval((int) (cx - rObjetivo - rNormal), (int) (cy - rObjetivo - rNormal),
                (int) ((rObjetivo + rNormal) * 2), (int) ((rObjetivo + rNormal) * 2));
        g.setColor(new Color(20, 16, 24));
        double interior = Math.max(1, rObjetivo - rNormal);
        g.fillOval((int) (cx - interior), (int) (cy - interior), (int) (interior * 2), (int) (interior * 2));
        g.setColor(new Color(235, 80, 50));
        g.drawOval((int) (cx - rObjetivo - rCritico), (int) (cy - rObjetivo - rCritico),
                (int) ((rObjetivo + rCritico) * 2), (int) ((rObjetivo + rCritico) * 2));
        g.drawOval((int) (cx - rObjetivo + rCritico), (int) (cy - rObjetivo + rCritico),
                (int) ((rObjetivo - rCritico) * 2), (int) ((rObjetivo - rCritico) * 2));
        // Anillo que se achica
        double r = anillo.getRadio() * escala;
        g.setColor(Color.WHITE);
        g.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g.drawOval((int) (cx - r + 1), (int) (cy - r + 1), (int) (r * 2 - 2), (int) (r * 2 - 2));
    }

    // Para las pruebas
    public Combate getCombate() {
        return combate;
    }

    public int getSeleccion() {
        return seleccion;
    }

    public int getMenu() {
        return menu;
    }
}

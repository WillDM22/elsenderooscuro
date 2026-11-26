package juego.pantallas;

import estructuras.ColaEnlazada;
import estructuras.ListaEnlazada;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import juego.entidades.Calidad;
import juego.entidades.Enemigo;
import juego.entidades.Habilidad;
import juego.entidades.Jugador;
import juego.graficos.ImagenesUi;
import juego.graficos.Paleta;
import juego.graficos.Personajes;
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
    private double ataqueEnemigo;
    private double ataqueJugador;
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

    // Posición de cada fila. El menú principal son 4 botones en grilla de 2x2 (imágenes de 222 x 85);
    // los submenús son listas verticales escritas sobre el panel, a la derecha.
    private static final int BOTON_X = 549;
    private static final int BOTON_Y = 444;
    private static final int BOTON_ANCHO = 222;
    private static final int BOTON_ALTO = 85;
    private static final int LISTA_X = 600;
    private static final int LISTA_Y = 478;
    private static final int LISTA_SEPARACION = 34;

    private int xItem(int i) {
        if (menu == PRINCIPAL) {
            return BOTON_X + (i % 2) * 228;
        }
        return LISTA_X;
    }

    private int yItem(int i) {
        if (menu == PRINCIPAL) {
            return BOTON_Y + (i / 2) * 89;
        }
        return LISTA_Y + i * LISTA_SEPARACION;
    }

    private boolean estaSobreItem(Entrada entrada, int i) {
        if (menu == PRINCIPAL) {
            return entrada.getMouseX() >= xItem(i) && entrada.getMouseX() <= xItem(i) + BOTON_ANCHO
                    && entrada.getMouseY() >= yItem(i) && entrada.getMouseY() <= yItem(i) + BOTON_ALTO;
        }
        return entrada.getMouseX() >= LISTA_X - 40 && entrada.getMouseX() <= 1000
                && Math.abs(entrada.getMouseY() - (yItem(i) - 8)) < LISTA_SEPARACION / 2;
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
            ataqueJugador = 0.7;
            flotantes.agregar(new Flotante("-" + (vidaEnemigoPrevia - enemigo.getVida()), 715, 250, Paleta.ORO));
        }
        if (jugador.getVida() < vidaJugadorPrevia) {
            sacudidaJugador = 0.4;
            ataqueEnemigo = 0.7;
            flotantes.agregar(new Flotante("-" + (vidaJugadorPrevia - jugador.getVida()), 310, 270, new Color(255, 90, 90)));
        } else if (jugador.getVida() > vidaJugadorPrevia) {
            flotantes.agregar(new Flotante("+" + (jugador.getVida() - vidaJugadorPrevia), 310, 270, new Color(120, 255, 140)));
        }
        vidaEnemigoPrevia = enemigo.getVida();
        vidaJugadorPrevia = jugador.getVida();
    }

    private void actualizarFlotantes(double dt) {
        sacudidaEnemigo = Math.max(0, sacudidaEnemigo - dt);
        sacudidaJugador = Math.max(0, sacudidaJugador - dt);
        ataqueEnemigo = Math.max(0, ataqueEnemigo - dt);
        ataqueJugador = Math.max(0, ataqueJugador - dt);
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
        ImagenesUi ui = recursos.getUi();
        String fondo = combate.esJefe() ? "Fondos/FondoJefeFinal.png" : "Fondos/FondoDungeon.png";
        if (!ui.dibujarEstirada(g, fondo, 0, 0, ANCHO, 640)) {
            g.setColor(Paleta.NEGRO);
            g.fillRect(0, 0, ANCHO, 640);
        }
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
        TextoPixel.izquierda(g, "Peligro " + combate.getNivel() + "/" + Dificultad.NIVEL_MAXIMO, 25, 152, 20, Paleta.ORO);
        ColaEnlazada<Integer> orden = combate.getOrdenDeTurnos();
        TextoPixel.izquierda(g, "Turnos:", 859, 152, 20, Paleta.ORO);
        int x = 861;
        boolean primero = true;
        while (!orden.estaVacia()) {
            int quien = orden.desencolar();
            Color color = quien == Combate.JUGADOR ? new Color(36, 150, 240) : new Color(226, 30, 30);
            g.setColor(Paleta.ORO);
            g.fillOval(x - 2, 164, 40, 40);
            g.setColor(color);
            g.fillOval(x, 166, 36, 36);
            if (primero) {
                g.setColor(Color.WHITE);
                g.drawOval(x - 2, 164, 39, 39);
                primero = false;
            }
            TextoPixel.tituloCentrado(g, quien == Combate.JUGADOR ? "T" : "E", x + 18, 192, 22, Color.WHITE);
            x = x + 44;
        }
    }

    // Pose de un personaje según lo que le pasó hace un instante
    private int poseDe(boolean muerto, double sacudida, double ataque) {
        if (muerto) {
            return Personajes.MUERTO;
        }
        if (sacudida > 0) {
            return Personajes.DANIADO;
        }
        if (ataque > 0) {
            return Personajes.ATAQUE;
        }
        return Personajes.NORMAL;
    }

    private void dibujarEnemigo(Graphics2D g) {
        Enemigo enemigo = combate.getEnemigo();
        ImagenesUi ui = recursos.getUi();
        double flota = Math.sin(tiempo * 2.5) * 3;
        double temblor = sacudidaEnemigo > 0 ? Math.sin(tiempo * 90) * 8 * sacudidaEnemigo / 0.4 : 0;
        int pose = poseDe(enemigo.estaMuerto(), sacudidaEnemigo, ataqueEnemigo);
        double pies = Personajes.alturaDePies(enemigo.getSprite()) + (pose == Personajes.NORMAL ? flota : 0);
        if (!Personajes.dibujar(ui, g, enemigo.getSprite(), pose, 715 + temblor, pies)) {
            // Respaldo: el dibujo de texto de siempre
            g.drawImage(recursos.getSprites().obtener(enemigo.getSprite(), 10), 650, 200, null);
        }
        TextoPixel.centrado(g, enemigo.getNombre(), 715, 152, 20, Paleta.ORO);
        Hud.barraGrande(g, 630, 168, 171, 22, enemigo.getVida(), enemigo.getVidaMaxima(), new Color(250, 28, 28),
                new Color(70, 10, 24), 14);
        if (enemigo.estaEnvenenado()) {
            TextoPixel.centrado(g, "ENVENENADO", 715, 214, 15, new Color(130, 230, 90));
        }
    }

    private void dibujarHeroe(Graphics2D g) {
        Jugador jugador = partida.getJugador();
        ImagenesUi ui = recursos.getUi();
        double flota = Math.sin(tiempo * 2.1 + 1) * 3;
        double temblor = sacudidaJugador > 0 ? Math.sin(tiempo * 90) * 8 * sacudidaJugador / 0.4 : 0;
        int pose = poseDe(jugador.estaMuerto(), sacudidaJugador, ataqueJugador);
        double pies = Personajes.SUELO + (pose == Personajes.NORMAL ? flota : 0);
        if (!Personajes.dibujar(ui, g, jugador.getClase().getSprite(), pose, 310 + temblor, pies)) {
            g.drawImage(recursos.getSprites().obtener(jugador.getClase().getSprite(), 10), 230, 220, null);
        }
        if (jugador.estaEnGuardia()) {
            TextoPixel.centrado(g, "GUARDIA", 310, 408, 16, new Color(120, 170, 255));
        }
        if (jugador.estaEnvenenado()) {
            TextoPixel.centrado(g, "ENVENENADO", 310, 426, 15, new Color(130, 230, 90));
        }
    }

    private void dibujarPanelInferior(Graphics2D g) {
        ImagenesUi ui = recursos.getUi();
        int estado = combate.getEstado();
        if (estado == Combate.ELECCION) {
            ui.dibujar(g, "GUIBatalla/ContenLogMenuBat.png", -9, 413);
            dibujarMenu(g);
        } else {
            // Un solo panel ancho: el minijuego o el mensaje del combate
            ui.dibujar(g, "GUIBatalla/ContenedorMiniJuego.png", -9, 414);
            if (estado == Combate.APUNTANDO) {
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
                TextoPixel.parrafoEscribiendo(g, texto, 58, 492, ANCHO - 120, 20, Paleta.HUESO, letras);
                if (estado != Combate.TURNO_ENEMIGO && letras > texto.length()) {
                    TextoPixel.derecha(g, "ENTER ▶", ANCHO - 50, 606, 18, Paleta.ORO);
                }
            }
        }
    }

    // Menú de dos niveles: a la izquierda el mensaje y la ayuda de la fila elegida; a la derecha los botones (menú
    // principal) o la lista de armas / hechizos
    private void dibujarMenu(Graphics2D g) {
        Jugador jugador = partida.getJugador();
        ImagenesUi ui = recursos.getUi();
        String titulo = "¿Qué hará " + jugador.getClase().getNombre() + "?";
        TextoPixel.izquierda(g, titulo, 29, 474, 22, Paleta.ORO);
        TextoPixel.parrafo(g, combate.getMensaje(), 29, 510, 500, 17, Paleta.HUESO);
        TextoPixel.parrafoResaltado(g, ayudaDeLaFila(), 29, 566, 500, 17, Paleta.HUESO, Paleta.ORO);

        int n = cantidadItems();
        for (int i = 0; i < n; i++) {
            boolean activo = filaDisponible(i);
            boolean elegido = i == seleccion;
            if (menu == PRINCIPAL) {
                String estado = elegido ? "Activo" : "Normal";
                Composite anterior = g.getComposite();
                if (!activo) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                }
                ui.dibujar(g, "GUIBatalla/" + archivoDeBoton(i) + estado + ".png", xItem(i), yItem(i));
                g.setComposite(anterior);
            } else {
                dibujarFilaDeLista(g, i, activo, elegido);
            }
        }
        if (menu != PRINCIPAL) {
            TextoPixel.derecha(g, "ESC volver", 990, 615, 14, Paleta.PERGAMINO);
        }
    }

    // Una fila de la lista de armas / hechizos: flecha dorada si está elegida, y el texto
    private void dibujarFilaDeLista(Graphics2D g, int i, boolean activo, boolean elegido) {
        ImagenesUi ui = recursos.getUi();
        int y = yItem(i);
        Color color = activo ? Paleta.HUESO : Paleta.PIEDRA;
        if (elegido) {
            color = activo ? Paleta.ORO : Paleta.PIEDRA_CLARA;
            ui.dibujarCentrado(g, "GUIBatalla/BotonSeleccion.png", LISTA_X - 16 + Math.sin(tiempo * 8) * 2, y - 9);
        }
        String texto = textoDeLaFila(i);
        boolean esVolver = texto.startsWith("◀");
        if (esVolver) {
            ui.dibujar(g, "GUIBatalla/IcoVolver_1.png", LISTA_X + 4, y - 20);
            texto = "Volver";
            TextoPixel.izquierda(g, texto, LISTA_X + 42, y, 20, color);
        } else {
            TextoPixel.izquierda(g, texto, LISTA_X + 6, y, 20, color);
        }
    }

    // Primera parte del nombre del archivo de cada botón del menú principal
    private String archivoDeBoton(int i) {
        if (i == OP_LUCHAR) {
            return "Luchar";
        }
        if (i == OP_MAGIA) {
            return "Magia";
        }
        if (i == OP_DEFENDER) {
            return "Def";
        }
        return "Correr";
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
        TextoPixel.centradoResaltado(g, minijuego.getInstruccion(), ANCHO / 2.0, 487, 22, Paleta.HUESO, Paleta.ORO);
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
        int y = 534;
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
        int y = 518;
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
        g.fillRect(x - 3, 604, ancho + 6, 16);
        g.setColor(new Color(90, 150, 220));
        g.fillRect(x, 607, (int) (ancho * resto), 10);
    }

    // Anillo: el anillo blanco se achica hacia la marca dorada (la zona crítica es el borde rojo de la marca).
    // Se dibuja al 60% del tamaño real para que entre en el panel.
    private void dibujarAnillo(Graphics2D g, MinijuegoAnillo anillo) {
        double escala = 0.6;
        int cx = ANCHO / 2;
        int cy = 556;
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

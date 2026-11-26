package juego.pantallas;

import estructuras.Grafo;
import estructuras.ListaEnlazada;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import juego.recursos.Recursos;
import juego.entidades.Enemigo;
import juego.graficos.CatalogoSprites;
import juego.graficos.DibujoMedieval;
import juego.graficos.Personajes;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.reglas.EventoNarrativo;
import juego.reglas.MapaMazmorra;
import juego.reglas.NodoMapa;
import juego.reglas.Partida;
import juego.reglas.Registro;
import juego.reglas.TipoNodo;
import motor.Entrada;
import motor.Escena;

// Pantalla del mapa: dibuja el GRAFO. El jugador elige a cuál de los lugares conectados avanzar.
public class EscenaMapa extends Escena {

    // Constantes
    private static final int ANCHO = 1024;
    private static final int RADIO_NODO = 34;
    private static final double ESCALA_HEROE = 0.12;

    // Atributos
    private final Recursos recursos;
    private final Partida partida;
    private final boolean autoguardar;
    private BufferedImage fondo;
    private ListaEnlazada<Integer> vecinos;
    private ListaEnlazada<Integer> ruta;
    private int seleccion;
    private double tiempo;
    private boolean verDiario;
    private String aviso = "";
    private double tiempoAviso;

    // Constructor
    public EscenaMapa(Recursos recursos, Partida partida, boolean autoguardar) {
        this.recursos = recursos;
        this.partida = partida;
        this.autoguardar = autoguardar;
    }

    @Override
    public void alEntrar() {
        fondo = recursos.getFondoMapa(partida.getMapa());
        vecinos = partida.getAdyacentesOrdenados();
        ruta = partida.getRuta();
        if (autoguardar) {
            guardar("Aventura guardada automáticamente.");
        }
    }

    private void guardar(String textoExito) {
        if (recursos.getArchivos().guardar(partida)) {
            avisar(textoExito);
        } else {
            avisar("No se pudo guardar la partida.");
        }
    }

    private void avisar(String texto) {
        aviso = texto;
        tiempoAviso = 3;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (tiempoAviso > 0) {
            tiempoAviso -= dt;
        }
        Entrada entrada = getEntrada();

        if (entrada.fuePresionada(KeyEvent.VK_H)) {
            verDiario = !verDiario;
        }
        if (entrada.fuePresionada(KeyEvent.VK_G)) {
            guardar("Partida guardada.");
        }
        if (entrada.fuePresionada(KeyEvent.VK_O) && !verDiario) {
            recursos.getArchivos().guardar(partida);
            getContexto().irA(new EscenaOpciones(recursos, this));
            return;
        }
        if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
            if (verDiario) {
                verDiario = false;
            } else {
                recursos.getArchivos().guardar(partida);
                getContexto().irA(new EscenaMenu(recursos));
            }
            return;
        }
        if (verDiario || vecinos.estaVacia()) {
            return;
        }

        // Elegir el destino
        if (entrada.fuePresionada(KeyEvent.VK_UP) || entrada.fuePresionada(KeyEvent.VK_LEFT)
                || entrada.fuePresionada(KeyEvent.VK_W) || entrada.fuePresionada(KeyEvent.VK_A)) {
            seleccion = (seleccion + vecinos.tamanio() - 1) % vecinos.tamanio();
        }
        if (entrada.fuePresionada(KeyEvent.VK_DOWN) || entrada.fuePresionada(KeyEvent.VK_RIGHT)
                || entrada.fuePresionada(KeyEvent.VK_S) || entrada.fuePresionada(KeyEvent.VK_D)) {
            seleccion = (seleccion + 1) % vecinos.tamanio();
        }
        int bajoMouse = vecinoBajoElMouse(entrada);
        if (bajoMouse >= 0) {
            seleccion = bajoMouse;
            if (entrada.huboClick()) {
                avanzar();
                return;
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            avanzar();
        }
    }

    // Índice (en la lista de vecinos) del lugar sobre el que está el mouse, o -1
    private int vecinoBajoElMouse(Entrada entrada) {
        for (int i = 0; i < vecinos.tamanio(); i++) {
            NodoMapa nodo = partida.getMapa().getNodo(vecinos.obtener(i));
            double distancia = Math.hypot(entrada.getMouseX() - nodo.getX(), entrada.getMouseY() - nodo.getY());
            if (distancia <= RADIO_NODO + 4) {
                return i;
            }
        }
        return -1;
    }

    // Se mueve el héroe por la arista elegida y se abre la escena del lugar
    private void avanzar() {
        int destino = vecinos.obtener(seleccion);
        partida.moverA(destino);
        NodoMapa nodo = partida.getMapa().getNodo(destino);
        TipoNodo tipo = nodo.getTipo();
        if (tipo == TipoNodo.COMBATE || tipo == TipoNodo.JEFE) {
            Enemigo enemigo = partida.crearEnemigoDeNodo(nodo);
            getContexto().irA(new EscenaCombate(recursos, partida, enemigo, tipo == TipoNodo.JEFE));
        } else if (tipo == TipoNodo.EVENTO) {
            EventoNarrativo evento = partida.crearEventoDeNodo(nodo);
            getContexto().irA(new EscenaEvento(recursos, partida, evento, false));
        } else {
            getContexto().irA(new EscenaEvento(recursos, partida, partida.crearEventoCofre(), true));
        }
    }

    // Dibujo
    @Override
    public void dibujar(Graphics2D g) {
        g.drawImage(fondo, 0, 0, null);
        dibujarSenderos(g);
        dibujarNodos(g);
        dibujarHeroe(g);
        Hud.dibujarFranja(g, recursos, partida.getJugador(), ANCHO);
        dibujarPanelInferior(g);
        if (verDiario) {
            dibujarDiario(g);
        }
    }

    private void dibujarSenderos(Graphics2D g) {
        Grafo<NodoMapa> grafo = partida.getMapa().getGrafo();
        int actual = partida.getNodoActual();
        for (int id = 0; id < grafo.cantidadVertices(); id++) {
            NodoMapa origen = grafo.obtener(id);
            ListaEnlazada<Integer> destinos = grafo.idsAdyacentes(id);
            for (int k = 0; k < destinos.tamanio(); k++) {
                NodoMapa destino = grafo.obtener(destinos.obtener(k));
                Color color = new Color(215, 195, 140, 150);
                int punto = 4;
                if (fueRecorrida(id, destino.getId())) {
                    color = Paleta.ORO;
                    punto = 6;
                } else if (id == actual) {
                    color = Paleta.HUESO;
                    punto = 6;
                }
                DibujoMedieval.sendero(g, origen.getX(), origen.getY(), destino.getX(), destino.getY(), color, punto);
            }
        }
    }

    // ¿El héroe pasó por la arista de "desde" a "hasta"? (están seguidos en la ruta)
    private boolean fueRecorrida(int desde, int hasta) {
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            if (ruta.obtener(i) == desde && ruta.obtener(i + 1) == hasta) {
                return true;
            }
        }
        return false;
    }

    private void dibujarNodos(Graphics2D g) {
        Grafo<NodoMapa> grafo = partida.getMapa().getGrafo();
        for (int id = 0; id < grafo.cantidadVertices(); id++) {
            NodoMapa nodo = grafo.obtener(id);
            int posicion = vecinos.indiceDe(id);
            dibujarNodo(g, nodo, posicion >= 0, posicion >= 0 && posicion == seleccion);
        }
    }

    private void dibujarNodo(Graphics2D g, NodoMapa nodo, boolean esVecino, boolean elegido) {
        int x = nodo.getX();
        int y = nodo.getY();
        Color relleno = colorDe(nodo.getTipo());
        g.setColor(Paleta.NEGRO);
        g.fillOval(x - RADIO_NODO - 3, y - RADIO_NODO - 3, (RADIO_NODO + 3) * 2, (RADIO_NODO + 3) * 2);
        g.setColor(relleno);
        g.fillOval(x - RADIO_NODO, y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

        // Anillo: dorado si ya se visitó, blanco pulsante si se puede ir
        if (nodo.estaVisitado()) {
            g.setColor(Paleta.ORO_OSCURO);
            g.setStroke(new BasicStroke(4));
            g.drawOval(x - RADIO_NODO, y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);
        } else if (esVecino) {
            int brillo = 170 + (int) (85 * Math.sin(tiempo * 5));
            g.setColor(new Color(brillo, brillo, brillo));
            g.setStroke(new BasicStroke(elegido ? 6 : 3));
            g.drawOval(x - RADIO_NODO - 3, y - RADIO_NODO - 3, (RADIO_NODO + 3) * 2, (RADIO_NODO + 3) * 2);
        }

        // Ícono del lugar (más tenue si ya se visitó)
        String sprite = spriteDe(nodo.getTipo());
        Composite anterior = g.getComposite();
        if (nodo.estaVisitado() && nodo.getTipo() != TipoNodo.INICIO) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }
        BufferedImage icono = recursos.getSprites().obtener(sprite, 3);
        g.drawImage(icono, x - icono.getWidth() / 2, y - icono.getHeight() / 2, null);
        g.setComposite(anterior);

        if (elegido) {
            DibujoMedieval.flecha(g, x - RADIO_NODO - 34 + Math.sin(tiempo * 8) * 3, y, 11);
            TextoPixel.centrado(g, nodo.getTipo().nombre(), x, y + RADIO_NODO + 24, 16, Paleta.ORO);
        }
    }

    // El héroe se ve con su propio sprite (pose normal) achicado, parado sobre el lugar actual: una sombra en el
    // suelo, un saltito suave y un halo dorado que late debajo. Si falta la imagen se usa el sprite chico de siempre.
    private void dibujarHeroe(Graphics2D g) {
        NodoMapa nodo = partida.getNodoActualCompleto();
        double pulso = (Math.sin(tiempo * 3) + 1) / 2;
        double salto = pulso * 5;
        double pies = nodo.getY() - 16;

        // Halo y sombra sobre el lugar
        Composite anterior = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.25 + 0.2 * pulso)));
        g.setColor(Paleta.ORO);
        g.fillOval((int) (nodo.getX() - 30), (int) (nodo.getY() - 30), 60, 60);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(Paleta.NEGRO);
        double achica = 1 - pulso * 0.15;
        g.fillOval((int) (nodo.getX() - 20 * achica), (int) (pies - 5), (int) (40 * achica), 10);
        g.setComposite(anterior);

        String sprite = partida.getJugador().getClase().getSprite();
        String ruta = Personajes.ruta(sprite, Personajes.NORMAL);
        if (ruta != null && recursos.getUi().dibujarPersonaje(g, ruta, nodo.getX(), pies - salto, ESCALA_HEROE, false)) {
            return;
        }
        BufferedImage heroe = recursos.getSprites().obtener(sprite, 3);
        DibujoMedieval.dibujarConSombra(g, heroe, nodo.getX(), nodo.getY() - RADIO_NODO - 22 - salto);
    }

    private void dibujarPanelInferior(Graphics2D g) {
        recursos.getUi().dibujar(g, "GUIBatalla/ContenedorMiniMap.png", -9, 512);
        String indicacion = "Piso " + partida.getNodoActualCompleto().getPiso() + " de " + (MapaMazmorra.PISOS - 1)
                + ". Elige tu camino.";
        double x = 28;
        if (vecinos.estaVacia()) {
            indicacion = "Estás frente a la última puerta.";
            TextoPixel.izquierda(g, indicacion, x, 567, 22, Paleta.ORO);
        } else {
            TextoPixel.izquierda(g, indicacion, x, 567, 22, Paleta.ORO);
            x = x + TextoPixel.anchoDe(g, indicacion, 22) + 12;
            NodoMapa destino = partida.getMapa().getNodo(vecinos.obtener(seleccion));
            TextoPixel.izquierda(g, "Destino: " + pista(destino.getTipo()), x, 567, 22, Paleta.HUESO);
        }
        // Teclas: la tecla en dorado y lo que hace en blanco
        double xt = 28;
        xt = tecla(g, xt, "↑↓", "elegir");
        xt = tecla(g, xt, "ENTER", "avanzar");
        xt = tecla(g, xt, "H", "diario");
        xt = tecla(g, xt, "G", "guardar");
        xt = tecla(g, xt, "O", "opciones");
        tecla(g, xt, "ESC", "menú");
        if (tiempoAviso > 0) {
            TextoPixel.derecha(g, aviso, ANCHO - 28, 608, 17, Paleta.ORO);
        }
    }

    // Escribe "TECLA acción" (la tecla en dorado) desde x y devuelve dónde empieza la siguiente indicación
    private double tecla(Graphics2D g, double x, String tecla, String accion) {
        TextoPixel.izquierda(g, tecla, x, 608, 17, Paleta.ORO);
        double despues = x + TextoPixel.anchoDe(g, tecla, 17) + 6;
        TextoPixel.izquierda(g, accion, despues, 608, 17, Paleta.HUESO);
        return despues + TextoPixel.anchoDe(g, accion, 17) + 26;
    }

    private void dibujarDiario(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, ANCHO, 640);
        recursos.getUi().dibujar(g, "MenuEventoAlea/ContenedorAlea.png", 167, 150);
        TextoPixel.tituloCentrado(g, "Diario de la aventura", ANCHO / 2.0, 200, 30, Paleta.MADERA_OSCURA, false);
        // Las últimas entradas de la PILA (la más reciente primero)
        ListaEnlazada<Registro> ultimos = partida.getUltimosRegistros(7);
        double y = 232;
        for (int i = 0; i < ultimos.tamanio(); i++) {
            Registro r = ultimos.obtener(i);
            y = TextoPixel.parrafo(g, "• " + r.getTexto(), 205, y, 610, 15, Paleta.MADERA_OSCURA, false) + 1;
        }
        TextoPixel.centrado(g, "H o ESC para cerrar", ANCHO / 2.0, 408, 14, Paleta.MADERA, false);
    }

    // Auxiliares
    private Color colorDe(TipoNodo tipo) {
        if (tipo == TipoNodo.COMBATE) {
            return new Color(96, 40, 44);
        }
        if (tipo == TipoNodo.EVENTO) {
            return new Color(60, 56, 110);
        }
        if (tipo == TipoNodo.COFRE) {
            return new Color(104, 76, 36);
        }
        if (tipo == TipoNodo.JEFE) {
            return new Color(80, 20, 60);
        }
        return new Color(50, 70, 52);
    }

    private String spriteDe(TipoNodo tipo) {
        if (tipo == TipoNodo.COMBATE) {
            return CatalogoSprites.CALAVERA;
        }
        if (tipo == TipoNodo.EVENTO) {
            return CatalogoSprites.INTERROGACION;
        }
        if (tipo == TipoNodo.COFRE) {
            return CatalogoSprites.COFRE;
        }
        if (tipo == TipoNodo.JEFE) {
            return CatalogoSprites.JEFE;
        }
        return CatalogoSprites.INICIO;
    }

    // Pista del peligro de cada tipo de lugar
    private String pista(TipoNodo tipo) {
        if (tipo == TipoNodo.COMBATE) {
            return "combate seguro";
        }
        if (tipo == TipoNodo.EVENTO) {
            return "algo inesperado (¿puede ser una pelea?)";
        }
        if (tipo == TipoNodo.COFRE) {
            return "un cofre con tesoro";
        }
        if (tipo == TipoNodo.JEFE) {
            return "el Rey Oscuro te espera";
        }
        return "";
    }

    // Para las pruebas
    public int getSeleccion() {
        return seleccion;
    }

    public boolean estaMostrandoDiario() {
        return verDiario;
    }
}

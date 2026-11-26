package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import juego.entidades.Clase;
import juego.entidades.Jugador;
import juego.graficos.ImagenesUi;
import juego.graficos.Paleta;
import juego.graficos.TextoPixel;
import juego.recursos.Recursos;

// Franja superior con el estado del héroe: vida, maná, energía, oro, atributos, retrato y nombre de la clase.
// La comparten el mapa, los eventos y el combate. Todas las piezas son imágenes de src/ui (GUIBatalla).
public final class Hud {

    // Rutas de las imágenes
    private static final String MARCO = "GUIBatalla/ContenedorMiniMap.png";
    private static final String ICO_CORAZON = "GUIBatalla/IcoCorazon.png";
    private static final String ICO_MANA = "GUIBatalla/IcoMana.png";
    private static final String ICO_ENERGIA = "GUIBatalla/IcoEnergia.png";
    private static final String ICO_ORO = "GUIBatalla/IcoOro.png";
    private static final String ATRIBUTOS = "GUIBatalla/IcoStatsContenedor.png";

    // Colores de las barras
    private static final Color BORDE_BARRA = new Color(244, 200, 56);
    private static final Color VIDA = new Color(250, 28, 28);
    private static final Color VIDA_FONDO = new Color(70, 10, 24);
    private static final Color MANA = new Color(30, 160, 250);
    private static final Color MANA_FONDO = new Color(14, 40, 90);
    private static final Color ENERGIA = new Color(90, 190, 70);
    private static final Color ENERGIA_FONDO = new Color(24, 60, 34);

    // Medidas del marco: la imagen es más alta que la franja, así que se le sacan unas filas del medio
    private static final int MARCO_X = -9;
    private static final int MARCO_Y = -7;
    private static final int FILA_DESDE = 44;
    private static final int FILA_HASTA = 61;

    // Posición de los cuatro números de atributos (centros, dentro de la imagen de atributos que empieza en 374, 20)
    private static final int ATRIBUTOS_X = 374;
    private static final int ATRIBUTOS_Y = 20;
    private static final int[] CENTROS_ATRIBUTOS = {35, 104, 174, 242};

    // Sin instancias
    private Hud() {
    }

    // Franja superior de 100 px de alto (ya no se distingue si hay o no barra de energía: siempre se muestra)
    public static void dibujarFranja(Graphics2D g, Recursos recursos, Jugador jugador, int ancho) {
        dibujarFranja(g, recursos, jugador, ancho, true);
    }

    public static void dibujarFranja(Graphics2D g, Recursos recursos, Jugador jugador, int ancho, boolean conEnergia) {
        ImagenesUi ui = recursos.getUi();
        ui.dibujarSinFilas(g, MARCO, MARCO_X, MARCO_Y, FILA_DESDE, FILA_HASTA);

        // Barras con su ícono
        ui.dibujar(g, ICO_CORAZON, 29, 18);
        barra(g, 61, 19, jugador.getVida(), jugador.getVidaMaxima(), VIDA, VIDA_FONDO);
        ui.dibujar(g, ICO_MANA, 31, 42);
        barra(g, 61, 43, jugador.getMana(), jugador.getManaMaximo(), MANA, MANA_FONDO);
        ui.dibujar(g, ICO_ENERGIA, 31, 66);
        barra(g, 61, 67, jugador.getEnergia(), jugador.getEnergiaMaxima(), ENERGIA, ENERGIA_FONDO);

        // Oro
        ui.dibujar(g, ICO_ORO, 253, 32);
        TextoPixel.izquierda(g, String.valueOf(jugador.getOro()), 304, 66, 30, Paleta.ORO);

        // Atributos: la imagen trae los rótulos y los números van encima, en los pentágonos amarillos
        ui.dibujar(g, ATRIBUTOS, ATRIBUTOS_X, ATRIBUTOS_Y);
        int[] valores = {jugador.getFuerza(), jugador.getDestreza(), jugador.getInteligencia(), jugador.getDefensa()};
        for (int i = 0; i < valores.length; i++) {
            TextoPixel.centrado(g, String.valueOf(valores[i]), ATRIBUTOS_X + CENTROS_ATRIBUTOS[i], ATRIBUTOS_Y + 62,
                    24, Paleta.NEGRO, false);
        }

        // Retrato y nombre de la clase
        Clase clase = jugador.getClase();
        ui.dibujar(g, "GUIBatalla/PerfilClase" + letra(clase) + ".png", 669, 14);
        ui.dibujar(g, "GUIBatalla/NomClase" + letra(clase) + ".png", 749, 17);
    }

    // Letra con la que terminan los nombres de archivo de cada clase (C, M, D, P)
    public static String letra(Clase clase) {
        if (clase == Clase.CABALLERO) {
            return "C";
        }
        if (clase == Clase.MAGO) {
            return "M";
        }
        if (clase == Clase.DRUIDA) {
            return "D";
        }
        return "P";
    }

    // Barra con marco dorado y el texto "valor/máximo" adentro. (x, y) es la esquina del marco; mide 164 x 19.
    public static void barra(Graphics2D g, int x, int y, int valor, int maximo, Color relleno, Color fondo) {
        barraGrande(g, x, y, 164, 19, valor, maximo, relleno, fondo, 14);
    }

    // Igual, pero con tamaño a elección (la usa también la barra del enemigo)
    public static void barraGrande(Graphics2D g, int x, int y, int ancho, int alto, int valor, int maximo,
                                   Color relleno, Color fondo, int tamanioTexto) {
        g.setColor(BORDE_BARRA);
        g.fillRect(x, y, ancho, alto);
        g.setColor(fondo);
        g.fillRect(x + 2, y + 2, ancho - 4, alto - 4);
        double proporcion = maximo <= 0 ? 0 : Math.max(0, Math.min(1, (double) valor / maximo));
        g.setColor(relleno);
        g.fillRect(x + 2, y + 2, (int) Math.round((ancho - 4) * proporcion), alto - 4);
        TextoPixel.izquierda(g, valor + "/" + maximo, x + 5, y + alto - 5, tamanioTexto, Paleta.HUESO);
    }
}

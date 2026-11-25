package juego.opciones;

// Opciones de personalización: resolución de la ventana, brillo y tamaño del texto.
// Cada opción es un número de posición (0, 1, 2...) dentro de una lista corta de valores posibles.
// El mismo objeto lo comparten la pantalla de opciones (que lo cambia) y la ventana (que lo lee en cada cuadro).
public class Opciones {

    // Cantidad de valores posibles de cada opción
    public static final int CANTIDAD_RESOLUCIONES = 3;
    public static final int CANTIDAD_BRILLOS = 4;
    public static final int CANTIDAD_TEXTOS = 3;

    // Valores por defecto
    public static final int RESOLUCION_INICIAL = 0;
    public static final int BRILLO_INICIAL = 2;
    public static final int TEXTO_INICIAL = 1;

    // Atributos
    private int resolucion = RESOLUCION_INICIAL;
    private int brillo = BRILLO_INICIAL;
    private int texto = TEXTO_INICIAL;

    // Resolución (modo de pantalla): tres modos
    //   0 = Normal (ventana de 1024 x 640, el tamaño propio del juego)
    //   1 = Grande (ventana 1,25 veces más grande: 1280 x 800)
    //   2 = Pantalla completa (ocupa toda la pantalla; el juego se agranda SIN deformarse y sobran barras negras)
    public static final int PANTALLA_NORMAL = 0;
    public static final int PANTALLA_GRANDE = 1;
    public static final int PANTALLA_COMPLETA = 2;

    public int getResolucion() {
        return resolucion;
    }

    public void setResolucion(int nueva) {
        validar(nueva, CANTIDAD_RESOLUCIONES, "resolución");
        resolucion = nueva;
    }

    public boolean esPantallaCompleta() {
        return resolucion == PANTALLA_COMPLETA;
    }

    // Cuántas veces se agranda el juego en los modos con ventana (en pantalla completa no se usa: manda el tamaño de la pantalla)
    public double getFactorVentana() {
        if (resolucion == PANTALLA_GRANDE) {
            return 1.25;
        }
        return 1.0;
    }

    public String getNombreResolucion() {
        if (resolucion == PANTALLA_NORMAL) {
            return "Normal 1024x640";
        }
        if (resolucion == PANTALLA_GRANDE) {
            return "Grande 1280x800";
        }
        return "Pantalla completa";
    }

    // Brillo: 70%, 85%, 100% o 115%
    public int getBrillo() {
        return brillo;
    }

    public void setBrillo(int nuevo) {
        validar(nuevo, CANTIDAD_BRILLOS, "brillo");
        brillo = nuevo;
    }

    public int getBrilloPorcentaje() {
        if (brillo == 0) {
            return 70;
        }
        if (brillo == 1) {
            return 85;
        }
        if (brillo == 2) {
            return 100;
        }
        return 115;
    }

    // Tamaño del texto: pequeño, normal o grande
    public int getTexto() {
        return texto;
    }

    public void setTexto(int nuevo) {
        validar(nuevo, CANTIDAD_TEXTOS, "texto");
        texto = nuevo;
    }

    public double getEscalaTexto() {
        if (texto == 0) {
            return 0.9;
        }
        if (texto == 1) {
            return 1.0;
        }
        return 1.15;
    }

    public String getNombreTexto() {
        if (texto == 0) {
            return "Pequeño";
        }
        if (texto == 1) {
            return "Normal";
        }
        return "Grande";
    }

    // Cambios relativos (+1 o -1): se detienen en los extremos, no dan la vuelta
    public void cambiarResolucion(int delta) {
        resolucion = limitar(resolucion + delta, CANTIDAD_RESOLUCIONES);
    }

    public void cambiarBrillo(int delta) {
        brillo = limitar(brillo + delta, CANTIDAD_BRILLOS);
    }

    public void cambiarTexto(int delta) {
        texto = limitar(texto + delta, CANTIDAD_TEXTOS);
    }

    // Auxiliares
    private static void validar(int valor, int cantidad, String nombre) {
        if (valor < 0 || valor >= cantidad) {
            throw new IllegalArgumentException(nombre + " fuera de rango (0 a " + (cantidad - 1) + "): " + valor);
        }
    }

    private static int limitar(int valor, int cantidad) {
        return Math.max(0, Math.min(cantidad - 1, valor));
    }
}

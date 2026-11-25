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

    // Resolución: tres tamaños de ventana con la misma proporción 16:10 que el juego
    public int getResolucion() {
        return resolucion;
    }

    public void setResolucion(int nueva) {
        validar(nueva, CANTIDAD_RESOLUCIONES, "resolución");
        resolucion = nueva;
    }

    public int getAncho() {
        if (resolucion == 0) {
            return 1024;
        }
        if (resolucion == 1) {
            return 1280;
        }
        return 1536;
    }

    public int getAlto() {
        if (resolucion == 0) {
            return 640;
        }
        if (resolucion == 1) {
            return 800;
        }
        return 960;
    }

    public String getNombreResolucion() {
        return getAncho() + " x " + getAlto();
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

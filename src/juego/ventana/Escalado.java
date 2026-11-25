package juego.ventana;

// Convierte coordenadas entre la pantalla real (el tamaño del panel donde se dibuja) y el tamaño "lógico" del juego
// (siempre 1024 x 640). El juego se dibuja en 1024 x 640 y se agranda por un MISMO factor en ancho y alto, así que
// nunca se deforma: si la pantalla no tiene la proporción 16:10 del juego, sobran franjas negras a los costados
// (o arriba y abajo) y el juego queda centrado. El mouse hace el camino inverso.
// No usa Swing: son solo cuentas, para poder probarlas.
// Es "final": nadie puede heredarla, así que el constructor puede usar sus propios métodos sin riesgo.
public final class Escalado {

    // Atributos
    private final int anchoLogico;
    private final int altoLogico;
    private int anchoReal;
    private int altoReal;
    private double factor = 1.0;
    private int desplazamientoX = 0;
    private int desplazamientoY = 0;

    // Constructor: el tamaño real empieza igual al lógico
    public Escalado(int anchoLogico, int altoLogico) {
        if (anchoLogico <= 0 || altoLogico <= 0) {
            throw new IllegalArgumentException("el tamaño lógico debe ser positivo");
        }
        this.anchoLogico = anchoLogico;
        this.altoLogico = altoLogico;
        setTamanioReal(anchoLogico, altoLogico);
    }

    // Se llama con el tamaño ACTUAL del panel. Calcula el factor (el menor de los dos que entran) y las franjas.
    public void setTamanioReal(int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("el tamaño real debe ser positivo");
        }
        anchoReal = ancho;
        altoReal = alto;
        factor = Math.min((double) ancho / anchoLogico, (double) alto / altoLogico);
        desplazamientoX = (ancho - getAnchoDibujado()) / 2;
        desplazamientoY = (alto - getAltoDibujado()) / 2;
    }

    // Cuánto se agranda el dibujo (igual en los dos ejes)
    public double getFactor() {
        return factor;
    }

    // Tamaño en píxeles reales del rectángulo donde se dibuja el juego
    public int getAnchoDibujado() {
        return (int) Math.round(anchoLogico * factor);
    }

    public int getAltoDibujado() {
        return (int) Math.round(altoLogico * factor);
    }

    // Franja negra de la izquierda y de arriba (la de la derecha y la de abajo miden lo mismo)
    public int getDesplazamientoX() {
        return desplazamientoX;
    }

    public int getDesplazamientoY() {
        return desplazamientoY;
    }

    // De píxel de la pantalla a coordenada del juego (fuera del juego da valores negativos o mayores al tamaño lógico)
    public int aLogicoX(int xReal) {
        return (int) Math.round((xReal - desplazamientoX) / factor);
    }

    public int aLogicoY(int yReal) {
        return (int) Math.round((yReal - desplazamientoY) / factor);
    }

    // Factor más grande que ENTRA en el espacio disponible, sin pasar del deseado.
    // Sirve para que una ventana no sea más grande que la pantalla.
    public double limitarFactor(int anchoDisponible, int altoDisponible, double deseado) {
        double entraEnAncho = (double) anchoDisponible / anchoLogico;
        double entraEnAlto = (double) altoDisponible / altoLogico;
        return Math.min(deseado, Math.min(entraEnAncho, entraEnAlto));
    }
}

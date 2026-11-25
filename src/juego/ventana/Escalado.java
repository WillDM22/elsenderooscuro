package juego.ventana;

// Convierte coordenadas entre la pantalla real (el tamaño de la ventana) y el tamaño "lógico" del juego
// (siempre 1024 x 640). El juego se dibuja en 1024 x 640 y la ventana lo agranda; el mouse hace el camino inverso.
public class Escalado {

    // Atributos
    private final int anchoLogico;
    private final int altoLogico;
    private int anchoReal;
    private int altoReal;

    // Constructor: el tamaño real empieza igual al lógico
    public Escalado(int anchoLogico, int altoLogico) {
        if (anchoLogico <= 0 || altoLogico <= 0) {
            throw new IllegalArgumentException("el tamaño lógico debe ser positivo");
        }
        this.anchoLogico = anchoLogico;
        this.altoLogico = altoLogico;
        this.anchoReal = anchoLogico;
        this.altoReal = altoLogico;
    }

    public void setTamanioReal(int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("el tamaño real debe ser positivo");
        }
        anchoReal = ancho;
        altoReal = alto;
    }

    // Cuánto se agranda el dibujo en cada eje
    public double getFactorX() {
        return (double) anchoReal / anchoLogico;
    }

    public double getFactorY() {
        return (double) altoReal / altoLogico;
    }

    // De píxel de la ventana a coordenada del juego
    public int aLogicoX(int xReal) {
        return (int) Math.round(xReal / getFactorX());
    }

    public int aLogicoY(int yReal) {
        return (int) Math.round(yReal / getFactorY());
    }
}

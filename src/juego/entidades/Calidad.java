package juego.entidades;

// Resultado de un golpe medido por la barra de precisión.
// Son constantes enteras simples: 0 = fallo, 1 = normal, 2 = crítico.
public final class Calidad {

    // Constantes
    public static final int FALLO = 0;
    public static final int NORMAL = 1;
    public static final int CRITICO = 2;

    // Sin instancias
    private Calidad() {
    }

    public static String nombre(int calidad) {
        if (calidad == CRITICO) {
            return "CRÍTICO";
        }
        if (calidad == NORMAL) {
            return "BIEN";
        }
        return "FALLO";
    }
}

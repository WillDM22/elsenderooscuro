package juego.reglas;

// Utilidad: cuánto se endurecen los minijuegos a medida que el héroe avanza.
// El "nivel" es la cantidad de enemigos derrotados en la aventura: cada victoria hace los minijuegos
// un poco más rápidos y con zonas de acierto más chicas (con un límite, para que siga siendo posible).
public final class Dificultad {

    // Constantes
    public static final int NIVEL_MAXIMO = 12;

    // Sin instancias
    private Dificultad() {
    }

    // Nivel dentro del rango válido (0 a NIVEL_MAXIMO)
    public static int limitarNivel(int nivel) {
        return Math.max(0, Math.min(NIVEL_MAXIMO, nivel));
    }

    // Multiplicador de velocidad: 1.0 en el nivel 0, 1.72 en el nivel máximo
    public static double factorVelocidad(int nivel) {
        return 1.0 + 0.06 * limitarNivel(nivel);
    }

    // Multiplicador del tamaño de las zonas de acierto: 1.0 en el nivel 0, 0.64 en el nivel máximo
    public static double factorZona(int nivel) {
        return 1.0 - 0.03 * limitarNivel(nivel);
    }
}

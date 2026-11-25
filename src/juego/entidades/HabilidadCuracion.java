package juego.entidades;

// Curación: cuesta maná, no usa la barra y recupera vida según la Inteligencia.
public class HabilidadCuracion extends Habilidad {

    // Constantes
    public static final String CODIGO = "CURACION";

    // Constructor
    public HabilidadCuracion() {
        super(CODIGO, "Luz sanadora", 4);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        int curado = jugador.curar(jugador.getPotenciaCuracion());
        return "Una luz tibia te envuelve: recuperas " + curado + " de vida.";
    }

    @Override
    public boolean usaBarra() {
        return false;
    }

    @Override
    public String getDescripcion() {
        return "Recupera vida según tu Inteligencia. No usa la barra.";
    }
}

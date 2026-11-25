package juego.entidades;

// Escape expeditivo: cuesta maná y permite huir de un combate normal (no de un jefe).
// El Combate es quien mira esEscape() y termina la pelea con la huida.
public class HabilidadEscape extends Habilidad {

    // Constantes
    public static final String CODIGO = "ESCAPE";

    // Constructor
    public HabilidadEscape() {
        super(CODIGO, "Paso sombrío", 3);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        return "Te fundes con las sombras y escapas del combate.";
    }

    @Override
    public boolean usaBarra() {
        return false;
    }

    @Override
    public boolean esEscape() {
        return true;
    }

    @Override
    public String getDescripcion() {
        return "Huyes de un combate normal. No funciona contra jefes.";
    }
}

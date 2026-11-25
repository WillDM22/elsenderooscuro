package juego.entidades;

// Goblin: pequeño y ladrón. Cada puñalada además le roba oro al héroe.
public class Goblin extends Enemigo {

    // Constantes
    private static final int ROBO = 3;

    // Constructor
    public Goblin() {
        super("Goblin", "goblin", 12, 4, 8);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        int robado = Math.min(jugador.getOro(), ROBO);
        jugador.cambiarOro(-robado);
        if (robado > 0) {
            return "El Goblin te apuñala: -" + real + " de vida, y te roba " + robado + " de oro.";
        }
        return "El Goblin te apuñala: -" + real + " de vida. ¡Revisa tus bolsillos vacíos!";
    }
}

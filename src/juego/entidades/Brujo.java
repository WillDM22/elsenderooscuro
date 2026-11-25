package juego.entidades;

// Brujo: pega fuerte y cada tercera acción se cura en lugar de atacar.
public class Brujo extends Enemigo {

    // Constantes
    private static final int CURACION = 5;

    // Constructor
    public Brujo() {
        super("Brujo", "brujo", 16, 6, 9);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        if (numeroDeAccion % 3 == 0) {
            curar(CURACION);
            return "El Brujo murmura un conjuro y se cura " + CURACION + " de vida.";
        }
        int real = jugador.recibirDanio(getDanio());
        return "El Brujo lanza una esfera oscura: -" + real + " de vida.";
    }
}

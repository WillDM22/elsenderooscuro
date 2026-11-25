package juego.entidades;

// Espectro: su toque helado le devuelve la mitad del daño que causa en forma de vida.
public class Espectro extends Enemigo {

    // Constructor
    public Espectro() {
        super("Espectro", "espectro", 12, 5, 9);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        int antes = getVida();
        curar(real / 2);
        int recuperado = getVida() - antes;
        if (recuperado > 0) {
            return "El Espectro te toca con frío de tumba: -" + real + " de vida. Absorbe " + recuperado + ".";
        }
        return "El Espectro te toca con frío de tumba: -" + real + " de vida.";
    }
}

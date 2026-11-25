package juego.entidades;

// Lobo: frágil pero rápido, actúa dos veces por ronda (su golpe es débil).
public class Lobo extends Enemigo {

    // Constructor
    public Lobo() {
        super("Lobo", "lobo", 14, 3, 5);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        return "El Lobo te muerde: -" + real + " de vida.";
    }

    @Override
    public int getTurnosPorRonda() {
        return 2;
    }
}

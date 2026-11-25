package juego.entidades;

// Araña: su mordida es débil pero cada dos acciones te envenena unos turnos.
public class Arana extends Enemigo {

    // Constantes
    private static final int TURNOS_VENENO = 3;
    private static final int DANIO_VENENO = 2;

    // Constructor
    public Arana() {
        super("Araña", "arana", 13, 3, 6);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        if (numeroDeAccion % 2 == 1) {
            jugador.envenenar(TURNOS_VENENO, DANIO_VENENO);
            return "La Araña te muerde: -" + real + " de vida. ¡Su veneno te quemará " + TURNOS_VENENO + " turnos!";
        }
        return "La Araña te muerde: -" + real + " de vida.";
    }
}

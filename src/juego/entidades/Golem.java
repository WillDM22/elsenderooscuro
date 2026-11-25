package juego.entidades;

// Golem de piedra: muy resistente y lento. Un turno se prepara y al siguiente golpea con el doble de fuerza.
public class Golem extends Enemigo {

    // Constructor
    public Golem() {
        super("Golem", "golem", 30, 6, 12);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        if (numeroDeAccion % 2 == 1) {
            return "El Golem alza sus puños de roca. ¡Va a golpear!";
        }
        int real = jugador.recibirDanio(getDanio() * 2);
        return "El Golem descarga sus puños de piedra: -" + real + " de vida.";
    }
}

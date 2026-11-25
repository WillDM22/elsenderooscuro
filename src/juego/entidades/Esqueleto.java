package juego.entidades;

// Esqueleto: resistente y de golpe parejo.
public class Esqueleto extends Enemigo {

    // Constructor
    public Esqueleto() {
        super("Esqueleto", "esqueleto", 18, 5, 6);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        int real = jugador.recibirDanio(getDanio());
        return "El Esqueleto te golpea con su espada oxidada: -" + real + " de vida.";
    }
}

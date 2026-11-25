package juego.entidades;

// Jefe final. Cada cuarta acción descarga un golpe devastador de daño doble.
public class ReyOscuro extends Enemigo {

    // Constructor
    public ReyOscuro() {
        super("Rey Oscuro", "rey", 60, 8, 50);
    }

    @Override
    protected String accionar(Jugador jugador, int numeroDeAccion) {
        if (numeroDeAccion % 4 == 0) {
            int real = jugador.recibirDanio(getDanio() * 2);
            return "¡El Rey Oscuro descarga un golpe devastador! -" + real + " de vida.";
        }
        int real = jugador.recibirDanio(getDanio());
        return "El Rey Oscuro te hiere con su hoja negra: -" + real + " de vida.";
    }
}

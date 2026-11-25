package juego.entidades;

// Enemigo de combate. Es abstracto: cada tipo decide cómo ataca en atacar().
public abstract class Enemigo {

    // Atributos
    private final String nombre;
    private final String sprite;
    private int vidaMaxima;
    private int vida;
    private int danio;
    private final int recompensaOro;
    private int acciones;

    // Constructor
    protected Enemigo(String nombre, String sprite, int vidaMaxima, int danio, int recompensaOro) {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("el enemigo necesita un nombre");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("la vida debe ser positiva: " + vidaMaxima);
        }
        if (danio < 0 || recompensaOro < 0) {
            throw new IllegalArgumentException("daño y recompensa no pueden ser negativos");
        }
        this.nombre = nombre;
        this.sprite = sprite;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.danio = danio;
        this.recompensaOro = recompensaOro;
    }

    // Ataque propio de cada enemigo. Devuelve el texto que se muestra en pantalla.
    protected abstract String accionar(Jugador jugador, int numeroDeAccion);

    // Turno del enemigo: cuenta la acción y delega en la subclase
    public final String atacar(Jugador jugador) {
        acciones++;
        return accionar(jugador, acciones);
    }

    // Los enemigos de pisos más altos son más duros
    public void reforzar(int piso) {
        if (piso < 0) {
            throw new IllegalArgumentException("el piso no puede ser negativo: " + piso);
        }
        vidaMaxima = vidaMaxima + 3 * piso;
        vida = vidaMaxima;
        danio = danio + piso / 2;
    }

    // Devuelve el daño real aplicado
    public int recibirDanio(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("el daño no puede ser negativo: " + cantidad);
        }
        int real = Math.min(vida, cantidad);
        vida = vida - real;
        return real;
    }

    protected void curar(int cantidad) {
        vida = Math.min(vidaMaxima, vida + cantidad);
    }

    // Cuántas veces actúa el enemigo por ronda (el lobo es rápido y actúa dos veces)
    public int getTurnosPorRonda() {
        return 1;
    }

    public boolean estaMuerto() {
        return vida <= 0;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getSprite() {
        return sprite;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getDanio() {
        return danio;
    }

    public int getRecompensaOro() {
        return recompensaOro;
    }
}

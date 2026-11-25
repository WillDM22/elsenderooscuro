package juego.entidades;

// Puños: golpe barato en energía pero débil (1 + Fuerza). La barra es un poco más fácil.
public class HabilidadPunos extends Habilidad {

    // Constantes
    public static final String CODIGO = "PUNOS";
    private static final int ENERGIA = 2;
    private static final int MANA_RECUPERADO = 1;

    // Constructor
    public HabilidadPunos() {
        super(CODIGO, "Puños", 0, ENERGIA);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Tu puño golpea el aire.";
        }
        int danio = 1 + jugador.getFuerza();
        if (calidad == Calidad.CRITICO) {
            danio = danio * 2;
        }
        int real = enemigo.recibirDanio(danio);
        jugador.cambiarMana(MANA_RECUPERADO);
        return textoCalidad(calidad) + "Tus puños golpean a " + enemigo.getNombre() + " por " + real
                + " de daño. (+" + MANA_RECUPERADO + " maná)";
    }

    @Override
    public double getDificultadBarra() {
        return 1.15;
    }

    @Override
    public String getDescripcion() {
        return "Golpe débil (1 + Fuerza) pero casi no cansa. Más fácil de acertar.";
    }
}

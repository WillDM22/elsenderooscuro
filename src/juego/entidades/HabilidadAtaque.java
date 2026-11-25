package juego.entidades;

// Ataque con arma: no cuesta maná, escala con Fuerza y devuelve un poco de maná al acertar.
public class HabilidadAtaque extends Habilidad {

    // Constantes
    public static final String CODIGO = "ATAQUE";
    private static final int MANA_RECUPERADO = 2;

    // Constructor
    public HabilidadAtaque() {
        super(CODIGO, "Ataque con arma", 0);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Tu espada pasa de largo.";
        }
        int danio = jugador.getDanioFisico();
        if (calidad == Calidad.CRITICO) {
            danio = danio * 2;
        }
        int real = enemigo.recibirDanio(danio);
        jugador.cambiarMana(MANA_RECUPERADO);
        return textoCalidad(calidad) + "Golpeas a " + enemigo.getNombre() + " por " + real
                + " de daño. (+" + MANA_RECUPERADO + " maná)";
    }

    @Override
    public String getDescripcion() {
        return "Golpe con tu arma. Daño según Fuerza. Recupera 2 de maná.";
    }
}

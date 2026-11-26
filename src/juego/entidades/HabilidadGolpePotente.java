package juego.entidades;

// Hechizo ofensivo: gran daño según Inteligencia, pero la barra es más exigente (zona crítica pequeña).
public class HabilidadGolpePotente extends Habilidad {

    // Constantes
    public static final String CODIGO = "GOLPE";

    // Constructor
    public HabilidadGolpePotente() {
        super(CODIGO, "Golpe Arcano", 5);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "El hechizo se disipa en el aire.";
        }
        int danio = jugador.getPotenciaMagica();
        if (calidad == Calidad.CRITICO) {
            danio = danio * 5 / 2;
        }
        int real = enemigo.recibirDanio(danio);
        return textoCalidad(calidad) + "Un rayo arcano hiere a " + enemigo.getNombre() + " por " + real + " de daño.";
    }

    @Override
    public double getDificultadBarra() {
        return 0.6;
    }

    @Override
    public String getDescripcion() {
        return "Gran daño mágico (Inteligencia). La barra es más difícil.";
    }
}

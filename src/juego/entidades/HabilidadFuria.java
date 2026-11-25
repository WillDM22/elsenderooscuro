package juego.entidades;

// Hechizo del caballero: un tajo con el doble de peso. Daño físico (Fuerza), barra algo más difícil.
public class HabilidadFuria extends Habilidad {

    // Constantes
    public static final String CODIGO = "FURIA";

    // Constructor
    public HabilidadFuria() {
        super(CODIGO, "Tajo heroico", 4);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Tu tajo corta solo el aire.";
        }
        int danio = jugador.getDanioFisico() * 3 / 2;
        if (calidad == Calidad.CRITICO) {
            danio = danio * 2;
        }
        int real = enemigo.recibirDanio(danio);
        return textoCalidad(calidad) + "Un tajo heroico abre a " + enemigo.getNombre() + " por " + real + " de daño.";
    }

    @Override
    public double getDificultadBarra() {
        return 0.8;
    }

    @Override
    public String getDescripcion() {
        return "Tajo de gran daño físico (Fuerza). Barra algo difícil.";
    }
}

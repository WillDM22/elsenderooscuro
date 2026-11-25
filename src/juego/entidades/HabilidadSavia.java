package juego.entidades;

// Hechizo del druida: daño mágico moderado y se cura la mitad del daño causado.
public class HabilidadSavia extends Habilidad {

    // Constantes
    public static final String CODIGO = "SAVIA";

    // Constructor
    public HabilidadSavia() {
        super(CODIGO, "Savia vital", 4);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Las raíces no logran atrapar a " + enemigo.getNombre() + ".";
        }
        int danio = jugador.getPotenciaMagica() * 2 / 3;
        if (calidad == Calidad.CRITICO) {
            danio = danio * 2;
        }
        int real = enemigo.recibirDanio(danio);
        int curado = jugador.curar(Math.max(1, real / 2));
        return textoCalidad(calidad) + "Raíces vivas drenan a " + enemigo.getNombre() + " por " + real
                + " de daño y te curan " + curado + ".";
    }

    @Override
    public double getDificultadBarra() {
        return 0.9;
    }

    @Override
    public String getDescripcion() {
        return "Daño mágico (Inteligencia) y te curas la mitad de lo que causas.";
    }
}

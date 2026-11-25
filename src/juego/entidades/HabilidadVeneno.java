package juego.entidades;

// Hechizo del ladrón: un corte pequeño con una daga envenenada. El veneno sigue quitando vida unos turnos.
public class HabilidadVeneno extends Habilidad {

    // Constantes
    public static final String CODIGO = "VENENO";
    private static final int TURNOS = 3;

    // Constructor
    public HabilidadVeneno() {
        super(CODIGO, "Daga envenenada", 3);
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "La daga no llega a rozar a " + enemigo.getNombre() + ".";
        }
        int danio = Math.max(1, jugador.getDanioFisico() * 3 / 4);
        int turnos = TURNOS;
        if (calidad == Calidad.CRITICO) {
            turnos = TURNOS + 1;
        }
        int danioVeneno = 2 + jugador.getDestreza() / 2;
        int real = enemigo.recibirDanio(danio);
        enemigo.envenenar(turnos, danioVeneno);
        return textoCalidad(calidad) + "Cortas a " + enemigo.getNombre() + " por " + real + " y lo envenenas ("
                + danioVeneno + " por turno, " + turnos + " turnos).";
    }

    @Override
    public double getDificultadBarra() {
        return 0.9;
    }

    @Override
    public String getDescripcion() {
        return "Corte leve y veneno que dura varios turnos (más Destreza, más veneno).";
    }
}

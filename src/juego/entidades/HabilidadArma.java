package juego.entidades;

// Arma de la clase del héroe (espada, bastón, dagas...). Cuesta ENERGÍA, no maná.
// El daño sale de la Fuerza: danioFisico * porcentaje / 100. Cada clase tiene su propio porcentaje,
// su multiplicador de crítico y cuánto maná recupera al acertar.
public class HabilidadArma extends Habilidad {

    // Constantes
    public static final String CODIGO = "ARMA";

    // Atributos
    private final int porcentaje;
    private final int multiplicadorCritico;
    private final int manaRecuperado;

    // Constructor
    public HabilidadArma(String nombre, int costoEnergia, int porcentaje, int multiplicadorCritico, int manaRecuperado) {
        super(CODIGO, nombre, 0, costoEnergia);
        if (porcentaje < 1 || porcentaje > 300) {
            throw new IllegalArgumentException("porcentaje de daño fuera de rango: " + porcentaje);
        }
        if (multiplicadorCritico < 1 || multiplicadorCritico > 5) {
            throw new IllegalArgumentException("multiplicador de crítico fuera de rango: " + multiplicadorCritico);
        }
        if (manaRecuperado < 0) {
            throw new IllegalArgumentException("el maná recuperado no puede ser negativo");
        }
        this.porcentaje = porcentaje;
        this.multiplicadorCritico = multiplicadorCritico;
        this.manaRecuperado = manaRecuperado;
    }

    @Override
    public String aplicar(Jugador jugador, Enemigo enemigo, int calidad) {
        if (calidad == Calidad.FALLO) {
            return textoCalidad(calidad) + "Tu golpe con " + getNombre() + " pasa de largo.";
        }
        int danio = Math.max(1, jugador.getDanioFisico() * porcentaje / 100);
        if (calidad == Calidad.CRITICO) {
            danio = danio * multiplicadorCritico;
        }
        int real = enemigo.recibirDanio(danio);
        jugador.cambiarMana(manaRecuperado);
        return textoCalidad(calidad) + getNombre() + " golpea a " + enemigo.getNombre() + " por " + real
                + " de daño. (+" + manaRecuperado + " maná)";
    }

    // Cuánto daño haría un golpe normal (lo usa la pantalla para mostrarlo)
    public int getDanioNormal(Jugador jugador) {
        return Math.max(1, jugador.getDanioFisico() * porcentaje / 100);
    }

    public int getManaRecuperado() {
        return manaRecuperado;
    }

    public int getMultiplicadorCritico() {
        return multiplicadorCritico;
    }

    @Override
    public String getDescripcion() {
        return "Golpe con tu arma (Fuerza). Crítico x" + multiplicadorCritico + ". Recupera " + manaRecuperado + " de maná.";
    }
}

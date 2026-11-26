package juego.reglas;

import juego.entidades.Calidad;
import juego.entidades.Jugador;

// Minijuego del anillo: un círculo se va achicando hacia una marca fija. Hay que apretar ESPACIO
// justo cuando el anillo coincide con la marca. Cuanto menor es la distancia (en píxeles), mejor:
//   dentro de la tolerancia crítica -> CRITICO, dentro de la normal -> NORMAL, si no -> FALLO.
// Si el anillo pasa de largo la marca sin apretar, es un FALLO.
public class MinijuegoAnillo extends Minijuego {

    // Constantes
    public static final double RADIO_INICIAL = 130;
    public static final double RADIO_OBJETIVO = 40;
    private static final double DURACION_BASE = 1.9;

    // Atributos
    private final double duracion;
    private final double toleranciaCritica;
    private final double toleranciaNormal;
    private double tiempo;

    // Constructor principal
    public MinijuegoAnillo(Jugador jugador, double dificultad, int nivel) {
        this(jugador.getBonusZonaCritica(), jugador.getFactorVelocidadBarra(), dificultad, nivel);
    }

    // Constructor con números sueltos (útil para las pruebas)
    public MinijuegoAnillo(int bonusCritico, double factorVelocidad, double dificultad, int nivel) {
        if (!(dificultad > 0 && dificultad <= 1.5)) {
            throw new IllegalArgumentException("dificultad fuera de rango: " + dificultad);
        }
        if (!(factorVelocidad > 0)) {
            throw new IllegalArgumentException("el factor de velocidad debe ser positivo: " + factorVelocidad);
        }
        double zona = Dificultad.factorZona(nivel);
        this.duracion = DURACION_BASE / (factorVelocidad * (2.0 - dificultad) * Dificultad.factorVelocidad(nivel));
        this.toleranciaCritica = (5 + bonusCritico / 4.0) * dificultad * zona;
        this.toleranciaNormal = 24 * (0.5 + 0.5 * dificultad) * (1.0 + zona) / 2.0;
    }

    @Override
    public void actualizar(double dt) {
        if (estaTerminado()) {
            return;
        }
        tiempo = tiempo + dt;
        // Si el anillo ya pasó de largo la marca, se perdió la oportunidad
        if (getRadio() < RADIO_OBJETIVO - toleranciaNormal) {
            terminar(Calidad.FALLO);
        }
    }

    @Override
    public void pulsar() {
        if (estaTerminado()) {
            return;
        }
        terminar(calidadConRadio(getRadio()));
    }

    // Calidad que daría un golpe cuando el anillo mide ese radio
    public int calidadConRadio(double radio) {
        double error = Math.abs(radio - RADIO_OBJETIVO);
        if (error <= toleranciaCritica) {
            return Calidad.CRITICO;
        }
        if (error <= toleranciaNormal) {
            return Calidad.NORMAL;
        }
        return Calidad.FALLO;
    }

    // Radio actual del anillo: baja en línea recta desde RADIO_INICIAL hasta 0
    public double getRadio() {
        return Math.max(0, RADIO_INICIAL * (1.0 - tiempo / duracion));
    }

    @Override
    public int getTipo() {
        return TIPO_ANILLO;
    }

    @Override
    public String getInstruccion() {
        return "¡ESPACIO o CLICK IZQ cuando el anillo toque la marca!";
    }

    public double getDuracion() {
        return duracion;
    }

    public double getToleranciaCritica() {
        return toleranciaCritica;
    }

    public double getToleranciaNormal() {
        return toleranciaNormal;
    }
}

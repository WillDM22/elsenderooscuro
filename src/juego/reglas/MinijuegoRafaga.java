package juego.reglas;

import juego.entidades.Calidad;
import juego.entidades.Jugador;

// Minijuego de ráfaga: hay que machacar ESPACIO todo lo posible antes de que se acabe el tiempo.
//   golpes >= objetivo crítico -> CRITICO (termina enseguida)
//   golpes >= objetivo normal  -> NORMAL
//   menos                       -> FALLO
// Los objetivos suben con el nivel de la aventura y bajan un poco con la Destreza.
public class MinijuegoRafaga extends Minijuego {

    // Constantes
    public static final double DURACION = 2.5;

    // Atributos
    private final int objetivoNormal;
    private final int objetivoCritico;
    private int golpes;
    private double tiempo;

    // Constructor principal
    public MinijuegoRafaga(Jugador jugador, double dificultad, int nivel) {
        this(jugador.getDestreza(), dificultad, nivel);
    }

    // Constructor con números sueltos (útil para las pruebas)
    public MinijuegoRafaga(int destreza, double dificultad, int nivel) {
        if (!(dificultad > 0 && dificultad <= 1.5)) {
            throw new IllegalArgumentException("dificultad fuera de rango: " + dificultad);
        }
        int n = Dificultad.limitarNivel(nivel);
        int normal = (int) Math.round((6 + n / 2 - destreza / 4) / dificultad);
        int critico = (int) Math.round((13 + n - destreza / 3) / dificultad);
        this.objetivoNormal = Math.max(3, normal);
        this.objetivoCritico = Math.min(26, Math.max(this.objetivoNormal + 2, critico));
    }

    @Override
    public void actualizar(double dt) {
        if (estaTerminado()) {
            return;
        }
        tiempo = tiempo + dt;
        if (tiempo >= DURACION) {
            terminar(calidadConGolpes(golpes));
        }
    }

    @Override
    public void pulsar() {
        if (estaTerminado()) {
            return;
        }
        golpes++;
        if (golpes >= objetivoCritico) {
            terminar(Calidad.CRITICO);
        }
    }

    // Calidad que daría esa cantidad de golpes al acabarse el tiempo
    public int calidadConGolpes(int cantidad) {
        if (cantidad >= objetivoCritico) {
            return Calidad.CRITICO;
        }
        if (cantidad >= objetivoNormal) {
            return Calidad.NORMAL;
        }
        return Calidad.FALLO;
    }

    @Override
    public int getTipo() {
        return TIPO_RAFAGA;
    }

    @Override
    public String getInstruccion() {
        return "¡Machaca ESPACIO o CLICK IZQ lo más rápido que puedas!";
    }

    public int getGolpes() {
        return golpes;
    }

    public int getObjetivoNormal() {
        return objetivoNormal;
    }

    public int getObjetivoCritico() {
        return objetivoCritico;
    }

    public double getTiempoRestante() {
        return Math.max(0, DURACION - tiempo);
    }
}

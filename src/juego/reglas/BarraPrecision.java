package juego.reglas;

import juego.entidades.Calidad;
import juego.entidades.Jugador;

// Minijuego de precisión (estilo Undertale): un cursor va y viene sobre una barra.
// Al detenerlo, cuanto más cerca del centro, mejor el golpe:
//   zona crítica (centro) -> CRITICO, zona normal -> NORMAL, afuera -> FALLO.
// La Destreza del jugador agranda la zona crítica y vuelve más lento al cursor.
public class BarraPrecision {

    // Constantes
    public static final double ANCHO = 600;
    private static final double ANCHO_NORMAL_BASE = 420;
    private static final double ANCHO_CRITICO_BASE = 40;
    private static final double VELOCIDAD_BASE = 560;

    // Atributos
    private final double anchoCritico;
    private final double anchoNormal;
    private final double velocidad;
    private double posicion;
    private int direccion = 1;
    private boolean detenida;
    private int resultado = Calidad.FALLO;

    // Constructor principal: recibe el jugador y la dificultad de la habilidad (1.0 normal, menos = más difícil)
    public BarraPrecision(Jugador jugador, double dificultad) {
        this(jugador.getBonusZonaCritica(), jugador.getFactorVelocidadBarra(), dificultad);
    }

    // Constructor con números sueltos (útil para las pruebas)
    public BarraPrecision(int bonusCritico, double factorVelocidad, double dificultad) {
        if (!(dificultad > 0 && dificultad <= 1.5)) {
            throw new IllegalArgumentException("dificultad fuera de rango: " + dificultad);
        }
        if (!(factorVelocidad > 0)) {
            throw new IllegalArgumentException("el factor de velocidad debe ser positivo: " + factorVelocidad);
        }
        this.anchoCritico = (ANCHO_CRITICO_BASE + bonusCritico) * dificultad;
        this.anchoNormal = ANCHO_NORMAL_BASE * (0.5 + 0.5 * dificultad);
        this.velocidad = VELOCIDAD_BASE * factorVelocidad * (2.0 - dificultad);
    }

    // Avance del cursor: rebota en los extremos
    public void actualizar(double dt) {
        if (detenida) {
            return;
        }
        posicion = posicion + direccion * velocidad * dt;
        if (posicion >= ANCHO) {
            posicion = ANCHO;
            direccion = -1;
        } else if (posicion <= 0) {
            posicion = 0;
            direccion = 1;
        }
    }

    // El jugador apretó ESPACIO: se frena el cursor y se mide el resultado
    public int detener() {
        detenida = true;
        resultado = calidadEn(posicion);
        return resultado;
    }

    // Calidad que daría un golpe frenado en esa posición
    public int calidadEn(double x) {
        double distancia = Math.abs(x - ANCHO / 2);
        if (distancia <= anchoCritico / 2) {
            return Calidad.CRITICO;
        }
        if (distancia <= anchoNormal / 2) {
            return Calidad.NORMAL;
        }
        return Calidad.FALLO;
    }

    // Consultas
    public double getPosicion() {
        return posicion;
    }

    public void setPosicion(double nueva) {
        if (nueva < 0 || nueva > ANCHO) {
            throw new IllegalArgumentException("posición fuera de la barra: " + nueva);
        }
        posicion = nueva;
    }

    public boolean estaDetenida() {
        return detenida;
    }

    public int getResultado() {
        return resultado;
    }

    public double getAnchoCritico() {
        return anchoCritico;
    }

    public double getAnchoNormal() {
        return anchoNormal;
    }

    public double getVelocidad() {
        return velocidad;
    }
}

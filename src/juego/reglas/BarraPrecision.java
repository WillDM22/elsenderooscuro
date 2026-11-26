package juego.reglas;

import juego.entidades.Calidad;
import juego.entidades.Jugador;

// Minijuego de precisión (estilo Undertale): un cursor va y viene sobre una barra.
// Al detenerlo, cuanto más cerca del centro, mejor el golpe:
//   zona crítica (centro) -> CRITICO, zona normal -> NORMAL, afuera -> FALLO.
// La Destreza del jugador agranda la zona crítica y vuelve más lento al cursor.
// El "nivel" de dificultad (enemigos derrotados) lo acelera y achica las zonas.
public class BarraPrecision extends Minijuego {

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

    // Constructor principal: recibe el jugador, la dificultad de la habilidad (1.0 normal, menos = más difícil)
    // y el nivel de la aventura
    public BarraPrecision(Jugador jugador, double dificultad, int nivel) {
        this(jugador.getBonusZonaCritica(), jugador.getFactorVelocidadBarra(), dificultad, nivel);
    }

    // Sin nivel (nivel 0)
    public BarraPrecision(Jugador jugador, double dificultad) {
        this(jugador, dificultad, 0);
    }

    // Constructor con números sueltos y sin nivel (útil para las pruebas)
    public BarraPrecision(int bonusCritico, double factorVelocidad, double dificultad) {
        this(bonusCritico, factorVelocidad, dificultad, 0);
    }

    // Constructor completo
    public BarraPrecision(int bonusCritico, double factorVelocidad, double dificultad, int nivel) {
        if (!(dificultad > 0 && dificultad <= 1.5)) {
            throw new IllegalArgumentException("dificultad fuera de rango: " + dificultad);
        }
        if (!(factorVelocidad > 0)) {
            throw new IllegalArgumentException("el factor de velocidad debe ser positivo: " + factorVelocidad);
        }
        double zona = Dificultad.factorZona(nivel);
        this.anchoCritico = (ANCHO_CRITICO_BASE + bonusCritico) * dificultad * zona;
        this.anchoNormal = ANCHO_NORMAL_BASE * (0.5 + 0.5 * dificultad) * (1.0 + zona) / 2.0;
        this.velocidad = VELOCIDAD_BASE * factorVelocidad * (2.0 - dificultad) * Dificultad.factorVelocidad(nivel);
    }

    // Avance del cursor: rebota en los extremos
    @Override
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
        terminar(calidadEn(posicion));
        return getResultado();
    }

    @Override
    public void pulsar() {
        detener();
    }

    @Override
    public int getTipo() {
        return TIPO_BARRA;
    }

    @Override
    public String getInstruccion() {
        return "¡Frena la barra con ESPACIO o con CLICK IZQ!";
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

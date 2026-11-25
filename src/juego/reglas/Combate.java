package juego.reglas;

import estructuras.ColaEnlazada;
import juego.entidades.Calidad;
import juego.entidades.Enemigo;
import juego.entidades.Habilidad;
import juego.entidades.Jugador;

// Lógica de un combate por turnos. No dibuja nada: la escena solo le pide cosas y muestra el estado.
// Los TURNOS están en una COLA (FIFO): se mira quién está al frente, actúa, y se lo saca de la cola.
// Cuando la cola se vacía comienza una ronda nueva: el jugador y después el enemigo (el lobo, dos veces).
public class Combate {

    // Estados del combate
    public static final int ELECCION = 0;
    public static final int APUNTANDO = 1;
    public static final int RESULTADO = 2;
    public static final int TURNO_ENEMIGO = 3;
    public static final int VICTORIA = 4;
    public static final int DERROTA = 5;
    public static final int HUIDA = 6;

    // Quién actúa (los valores que viajan por la cola)
    public static final int JUGADOR = 1;
    public static final int ENEMIGO = 2;

    // Atributos
    private final Jugador jugador;
    private final Enemigo enemigo;
    private final boolean esJefe;
    private final ColaEnlazada<Integer> turnos = new ColaEnlazada<>();
    private int estado;
    private BarraPrecision barra;
    private Habilidad habilidadActual;
    private String mensaje = "";
    private int rondas;

    // Constructor
    public Combate(Jugador jugador, Enemigo enemigo, boolean esJefe) {
        if (jugador == null || enemigo == null) {
            throw new IllegalArgumentException("el combate necesita jugador y enemigo");
        }
        this.jugador = jugador;
        this.enemigo = enemigo;
        this.esJefe = esJefe;
        this.mensaje = "¡" + enemigo.getNombre() + " aparece!";
        prepararSiguienteTurno();
    }

    // Acciones del jugador
    // Intenta usar la habilidad número "indice" de su repertorio. Devuelve false si no se puede.
    public boolean elegirHabilidad(int indice) {
        if (estado != ELECCION) {
            return false;
        }
        if (indice < 0 || indice >= jugador.cantidadHabilidades()) {
            return false;
        }
        Habilidad habilidad = jugador.getHabilidad(indice);
        if (!jugador.puedePagar(habilidad.getCostoMana())) {
            mensaje = "No tienes maná suficiente para " + habilidad.getNombre() + ".";
            return false;
        }
        if (habilidad.esEscape() && esJefe) {
            mensaje = "¡No puedes huir de un jefe!";
            return false;
        }
        jugador.gastarMana(habilidad.getCostoMana());
        habilidadActual = habilidad;
        if (habilidad.usaBarra()) {
            barra = new BarraPrecision(jugador, habilidad.getDificultadBarra());
            estado = APUNTANDO;
            mensaje = "¡Frena la barra con ESPACIO!";
        } else {
            resolver(Calidad.NORMAL);
        }
        return true;
    }

    // Avance de la barra mientras se apunta
    public void actualizar(double dt) {
        if (estado == APUNTANDO) {
            barra.actualizar(dt);
        }
    }

    // El jugador frena la barra: se mide la calidad y se resuelve la habilidad
    public void detenerBarra() {
        if (estado != APUNTANDO) {
            return;
        }
        int calidad = barra.detener();
        resolver(calidad);
    }

    // Aplica la habilidad y decide cómo sigue el combate
    private void resolver(int calidad) {
        mensaje = habilidadActual.aplicar(jugador, enemigo, calidad);
        turnos.desencolar();
        if (habilidadActual.esEscape()) {
            estado = HUIDA;
        } else {
            estado = RESULTADO;
        }
    }

    // El jugador toca "continuar" después de leer un mensaje
    public void continuar() {
        if (estado == RESULTADO) {
            if (enemigo.estaMuerto()) {
                jugador.cambiarOro(enemigo.getRecompensaOro());
                mensaje = "¡Derrotaste a " + enemigo.getNombre() + "! +" + enemigo.getRecompensaOro() + " de oro.";
                estado = VICTORIA;
            } else if (jugador.estaMuerto()) {
                mensaje = "Caes herido en el sendero...";
                estado = DERROTA;
            } else {
                prepararSiguienteTurno();
            }
        } else if (estado == TURNO_ENEMIGO) {
            ejecutarTurnoEnemigo();
        }
    }

    // El enemigo actúa (se llama desde continuar)
    public void ejecutarTurnoEnemigo() {
        if (estado != TURNO_ENEMIGO) {
            return;
        }
        mensaje = enemigo.atacar(jugador);
        turnos.desencolar();
        estado = RESULTADO;
    }

    // Mira al frente de la cola para saber quién sigue (y arma la ronda nueva si está vacía)
    private void prepararSiguienteTurno() {
        if (turnos.estaVacia()) {
            rondas++;
            turnos.encolar(JUGADOR);
            for (int i = 0; i < enemigo.getTurnosPorRonda(); i++) {
                turnos.encolar(ENEMIGO);
            }
        }
        if (turnos.frente() == JUGADOR) {
            estado = ELECCION;
        } else {
            estado = TURNO_ENEMIGO;
        }
    }

    // Orden de los próximos turnos para mostrarlos en pantalla.
    // Para mirar una cola sin perder datos se desencola todo y se vuelve a encolar en el mismo orden.
    public ColaEnlazada<Integer> getOrdenDeTurnos() {
        ColaEnlazada<Integer> copia = new ColaEnlazada<>();
        int cantidad = turnos.tamanio();
        for (int i = 0; i < cantidad; i++) {
            int quien = turnos.desencolar();
            copia.encolar(quien);
            turnos.encolar(quien);
        }
        return copia;
    }

    // Consultas
    public int getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public BarraPrecision getBarra() {
        return barra;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Enemigo getEnemigo() {
        return enemigo;
    }

    public boolean esJefe() {
        return esJefe;
    }

    public int getRondas() {
        return rondas;
    }

    public boolean haTerminado() {
        return estado == VICTORIA || estado == DERROTA || estado == HUIDA;
    }
}

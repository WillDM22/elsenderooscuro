package juego.reglas;

import estructuras.ColaEnlazada;
import java.util.Random;
import juego.entidades.Calidad;
import juego.entidades.Enemigo;
import juego.entidades.Habilidad;
import juego.entidades.Jugador;

// Lógica de un combate por turnos. No dibuja nada: la escena solo le pide cosas y muestra el estado.
// Los TURNOS están en una COLA (FIFO): se mira quién está al frente, actúa, y se lo saca de la cola.
// Cuando la cola se vacía comienza una ronda nueva: el jugador y después el enemigo (el lobo, dos veces).
//
// Acciones del jugador en su turno (estilo Pokémon):
//   LUCHAR  -> arma o puños (cuestan ENERGÍA) con un minijuego al azar
//   MAGIA   -> un hechizo de su repertorio (cuesta MANÁ) con un minijuego al azar
//   DEFENDER-> guardia (golpes a la mitad) y recupera energía
//   HUIR    -> intento de escapar (no sirve contra jefes)
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

    // Reglas de energía y huida
    public static final int ENERGIA_DEFENSA = 4;
    private static final int HUIDA_BASE = 40;
    private static final int HUIDA_POR_DESTREZA = 5;
    private static final int HUIDA_MAXIMA = 90;

    // Atributos
    private final Jugador jugador;
    private final Enemigo enemigo;
    private final boolean esJefe;
    private final int nivel;
    private final Random azar;
    private final ColaEnlazada<Integer> turnos = new ColaEnlazada<>();
    private int estado;
    private Minijuego minijuego;
    private BarraPrecision barra;
    private MinijuegoRafaga rafaga;
    private MinijuegoAnillo anillo;
    private Habilidad habilidadActual;
    private String mensaje = "";
    private int rondas;

    // Constructor sencillo: nivel 0 y azar propio
    public Combate(Jugador jugador, Enemigo enemigo, boolean esJefe) {
        this(jugador, enemigo, esJefe, 0, new Random());
    }

    // Constructor completo: el nivel endurece los minijuegos; el azar elige cuál sale (se puede fijar en pruebas)
    public Combate(Jugador jugador, Enemigo enemigo, boolean esJefe, int nivel, Random azar) {
        if (jugador == null || enemigo == null || azar == null) {
            throw new IllegalArgumentException("el combate necesita jugador, enemigo y azar");
        }
        this.jugador = jugador;
        this.enemigo = enemigo;
        this.esJefe = esJefe;
        this.nivel = Dificultad.limitarNivel(nivel);
        this.azar = azar;
        this.mensaje = "¡" + enemigo.getNombre() + " aparece!";
        jugador.prepararCombate();
        prepararSiguienteTurno();
    }

    // Acciones del jugador
    // Hechizo número "indice" de su repertorio. Devuelve false si no se puede.
    public boolean elegirHabilidad(int indice) {
        if (indice < 0 || indice >= jugador.cantidadHabilidades()) {
            return false;
        }
        return iniciarAccion(jugador.getHabilidad(indice));
    }

    public boolean elegirArma() {
        return iniciarAccion(jugador.getArma());
    }

    public boolean elegirPunos() {
        return iniciarAccion(jugador.getPunos());
    }

    // Revisa que se pueda pagar la acción, la paga y la empieza
    private boolean iniciarAccion(Habilidad habilidad) {
        if (estado != ELECCION) {
            return false;
        }
        if (!jugador.puedePagar(habilidad.getCostoMana())) {
            mensaje = "No tienes maná suficiente para " + habilidad.getNombre() + ".";
            return false;
        }
        if (!jugador.puedePagarEnergia(habilidad.getCostoEnergia())) {
            mensaje = "No tienes energía suficiente para " + habilidad.getNombre() + ".";
            return false;
        }
        if (habilidad.esEscape() && esJefe) {
            mensaje = "¡No puedes huir de un jefe!";
            return false;
        }
        jugador.gastarMana(habilidad.getCostoMana());
        jugador.gastarEnergia(habilidad.getCostoEnergia());
        habilidadActual = habilidad;
        if (habilidad.usaBarra()) {
            crearMinijuego(habilidad.getDificultadBarra());
            estado = APUNTANDO;
            mensaje = minijuego.getInstruccion();
        } else {
            resolver(Calidad.NORMAL);
        }
        return true;
    }

    // Elige al azar uno de los tres minijuegos: barra (50%), ráfaga (25%) o anillo (25%)
    private void crearMinijuego(double dificultad) {
        barra = null;
        rafaga = null;
        anillo = null;
        int tirada = azar.nextInt(100);
        if (tirada < 50) {
            barra = new BarraPrecision(jugador, dificultad, nivel);
            minijuego = barra;
        } else if (tirada < 75) {
            rafaga = new MinijuegoRafaga(jugador, dificultad, nivel);
            minijuego = rafaga;
        } else {
            anillo = new MinijuegoAnillo(jugador, dificultad, nivel);
            minijuego = anillo;
        }
    }

    // Defender: la guardia reduce a la mitad los golpes de esta ronda y se recupera energía
    public boolean defender() {
        if (estado != ELECCION) {
            return false;
        }
        jugador.setGuardia(true);
        jugador.recuperarEnergia(ENERGIA_DEFENSA);
        mensaje = "Te cubres con tu guardia: recibirás la mitad de daño esta ronda. (+" + ENERGIA_DEFENSA + " energía)";
        habilidadActual = null;
        terminarTurnoDelJugador();
        return true;
    }

    // Huir: probabilidad según la Destreza. En un jefe no se puede. Si falla, se pierde el turno.
    public boolean huir() {
        if (estado != ELECCION) {
            return false;
        }
        if (esJefe) {
            mensaje = "¡No puedes huir de un jefe!";
            return false;
        }
        if (azar.nextInt(100) < getProbabilidadDeHuir()) {
            mensaje = "Logras escabullirte entre los árboles.";
            turnos.desencolar();
            estado = HUIDA;
        } else {
            mensaje = enemigo.getNombre() + " te corta el paso: no logras huir.";
            habilidadActual = null;
            terminarTurnoDelJugador();
        }
        return true;
    }

    public int getProbabilidadDeHuir() {
        return Math.min(HUIDA_MAXIMA, HUIDA_BASE + HUIDA_POR_DESTREZA * jugador.getDestreza());
    }

    // Avance del minijuego mientras se apunta (el ráfaga y el anillo pueden terminar solos por tiempo)
    public void actualizar(double dt) {
        if (estado == APUNTANDO) {
            minijuego.actualizar(dt);
            if (minijuego.estaTerminado()) {
                resolver(minijuego.getResultado());
            }
        }
    }

    // El jugador apretó ESPACIO durante el minijuego
    public void pulsar() {
        if (estado != APUNTANDO) {
            return;
        }
        minijuego.pulsar();
        if (minijuego.estaTerminado()) {
            resolver(minijuego.getResultado());
        }
    }

    // Aplica la habilidad y decide cómo sigue el combate
    private void resolver(int calidad) {
        mensaje = habilidadActual.aplicar(jugador, enemigo, calidad);
        if (habilidadActual.esEscape()) {
            turnos.desencolar();
            estado = HUIDA;
        } else {
            terminarTurnoDelJugador();
        }
    }

    // Saca al jugador de la cola, le cobra el veneno (si lo tiene) y muestra el resultado
    private void terminarTurnoDelJugador() {
        turnos.desencolar();
        if (jugador.estaEnvenenado() && !enemigo.estaMuerto()) {
            int danio = jugador.aplicarVeneno();
            mensaje = mensaje + " El veneno te quita " + danio + " de vida.";
        }
        estado = RESULTADO;
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
        if (enemigo.estaEnvenenado() && !jugador.estaMuerto()) {
            int danio = enemigo.aplicarVeneno();
            mensaje = mensaje + " El veneno le quita " + danio + " de vida.";
        }
        estado = RESULTADO;
    }

    // Mira al frente de la cola para saber quién sigue (y arma la ronda nueva si está vacía)
    private void prepararSiguienteTurno() {
        if (turnos.estaVacia()) {
            rondas++;
            if (rondas > 1) {
                // Empieza una ronda nueva: se acaba la guardia y se recupera un poco de energía
                jugador.setGuardia(false);
                jugador.recuperarEnergia(Jugador.ENERGIA_POR_RONDA);
            }
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

    // Minijuego en curso (null si no se está apuntando)
    public Minijuego getMinijuego() {
        return minijuego;
    }

    // Los tres getters siguientes devuelven el minijuego solo si es de ese tipo (si no, null)
    public BarraPrecision getBarra() {
        return barra;
    }

    public MinijuegoRafaga getRafaga() {
        return rafaga;
    }

    public MinijuegoAnillo getAnillo() {
        return anillo;
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

    public int getNivel() {
        return nivel;
    }

    public boolean haTerminado() {
        return estado == VICTORIA || estado == DERROTA || estado == HUIDA;
    }
}

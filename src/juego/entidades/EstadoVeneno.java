package juego.entidades;

// Veneno: quita vida al terminar cada turno durante algunos turnos.
// Lo usan el Jugador (picadura de araña) y el Enemigo (daga envenenada) por composición:
// cada uno TIENE un EstadoVeneno, en lugar de heredar de una clase común.
public class EstadoVeneno {

    // Atributos
    private int turnos;
    private int danio;

    // Aplica (o renueva) el veneno. Si ya había uno, queda el que dura más y el que pega más fuerte.
    public void aplicar(int nuevosTurnos, int nuevoDanio) {
        if (nuevosTurnos <= 0 || nuevoDanio <= 0) {
            throw new IllegalArgumentException("el veneno necesita turnos y daño positivos");
        }
        if (estaActivo()) {
            danio = Math.max(danio, nuevoDanio);
            turnos = Math.max(turnos, nuevosTurnos);
        } else {
            danio = nuevoDanio;
            turnos = nuevosTurnos;
        }
    }

    // Un turno de veneno: devuelve el daño que corresponde (0 si no hay veneno)
    public int pasarTurno() {
        if (!estaActivo()) {
            return 0;
        }
        int resultado = danio;
        turnos--;
        if (turnos == 0) {
            danio = 0;
        }
        return resultado;
    }

    public boolean estaActivo() {
        return turnos > 0;
    }

    public void limpiar() {
        turnos = 0;
        danio = 0;
    }

    public int getTurnos() {
        return turnos;
    }

    public int getDanio() {
        return danio;
    }
}

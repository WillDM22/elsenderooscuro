package juego.reglas;

import juego.entidades.Calidad;

// Minijuego de acción del combate. Es abstracto: hay tres, y el combate elige uno al azar cada vez.
//   BarraPrecision   -> frenar un cursor en el centro de una barra
//   MinijuegoRafaga  -> machacar ESPACIO antes de que se acabe el tiempo
//   MinijuegoAnillo  -> frenar un anillo que se achica justo sobre una marca
// Todos terminan con una calidad (Calidad.FALLO, NORMAL o CRITICO) que usa la habilidad.
public abstract class Minijuego {

    // Tipos (la pantalla los usa para saber cómo dibujar)
    public static final int TIPO_BARRA = 0;
    public static final int TIPO_RAFAGA = 1;
    public static final int TIPO_ANILLO = 2;

    // Atributos
    private boolean terminado;
    private int resultado = Calidad.FALLO;

    // El tiempo avanza (se llama en cada cuadro)
    public abstract void actualizar(double dt);

    // El jugador apretó ESPACIO
    public abstract void pulsar();

    public abstract int getTipo();

    // Texto de ayuda que se muestra al jugador
    public abstract String getInstruccion();

    // Cierra el minijuego con una calidad. Solo vale la primera vez.
    protected void terminar(int calidad) {
        if (!terminado) {
            terminado = true;
            resultado = calidad;
        }
    }

    public boolean estaTerminado() {
        return terminado;
    }

    public int getResultado() {
        return resultado;
    }
}

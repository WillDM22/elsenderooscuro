package juego.entidades;

// Habilidad que el jugador puede usar en combate.
// Es abstracta: cada habilidad concreta decide qué efecto produce en aplicar().
public abstract class Habilidad {

    // Atributos
    private final String codigo;
    private final String nombre;
    private final int costoMana;

    // Constructor
    protected Habilidad(String codigo, String nombre, int costoMana) {
        if (codigo == null || codigo.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un código");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un nombre");
        }
        if (costoMana < 0) {
            throw new IllegalArgumentException("el costo de maná no puede ser negativo: " + costoMana);
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.costoMana = costoMana;
    }

    // Efecto de la habilidad. "calidad" viene de la barra (Calidad.FALLO/NORMAL/CRITICO).
    // Devuelve el texto que se muestra en el combate.
    public abstract String aplicar(Jugador jugador, Enemigo enemigo, int calidad);

    public abstract String getDescripcion();

    // Valores por defecto que algunas subclases cambian
    public boolean usaBarra() {
        return true;
    }

    public double getDificultadBarra() {
        return 1.0;
    }

    public boolean esEscape() {
        return false;
    }

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCostoMana() {
        return costoMana;
    }

    // Texto que explica la calidad de un golpe
    protected String textoCalidad(int calidad) {
        if (calidad == Calidad.CRITICO) {
            return "¡CRÍTICO! ";
        }
        if (calidad == Calidad.FALLO) {
            return "Fallaste. ";
        }
        return "";
    }
}

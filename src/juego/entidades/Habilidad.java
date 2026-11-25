package juego.entidades;

// Acción que el jugador puede usar en combate: un arma, los puños o un hechizo.
// Es abstracta: cada acción concreta decide qué efecto produce en aplicar().
// Cuesta maná (hechizos), energía (armas y puños) o nada.
public abstract class Habilidad {

    // Atributos
    private final String codigo;
    private final String nombre;
    private final int costoMana;
    private final int costoEnergia;

    // Constructor para hechizos: solo cuestan maná
    protected Habilidad(String codigo, String nombre, int costoMana) {
        this(codigo, nombre, costoMana, 0);
    }

    // Constructor completo
    protected Habilidad(String codigo, String nombre, int costoMana, int costoEnergia) {
        if (codigo == null || codigo.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un código");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("la habilidad necesita un nombre");
        }
        if (costoMana < 0 || costoEnergia < 0) {
            throw new IllegalArgumentException("los costos no pueden ser negativos");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.costoMana = costoMana;
        this.costoEnergia = costoEnergia;
    }

    // Efecto de la habilidad. "calidad" viene del minijuego (Calidad.FALLO/NORMAL/CRITICO).
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

    public int getCostoEnergia() {
        return costoEnergia;
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

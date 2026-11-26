package juego.entidades;

// Clase del héroe (se elige al empezar). Define atributos iniciales, arma y primer hechizo.
// Es un enum: una lista cerrada de 4 valores, cada uno con sus datos.
public enum Clase {

    CABALLERO("Caballero", "caballero", 5, 3, 1, "Espada", 7, 90, 2, 1,
            HabilidadFuria.CODIGO, "Resistente y fuerte. Su espada pega duro pero cansa."),
    MAGO("Mago", "mago", 1, 3, 5, "Bastón", 3, 70, 2, 4,
            HabilidadGolpePotente.CODIGO, "Frágil, con mucho maná. Su bastón recarga el maná."),
    DRUIDA("Druida", "druida", 3, 3, 3, "Bastón de roble", 4, 100, 2, 2,
            HabilidadSavia.CODIGO, "Equilibrado: pega y se cura con la magia de la naturaleza."),
    LADRON("Pícaro", "ladron", 2, 5, 2, "Dagas", 3, 150, 3, 2,
            HabilidadVeneno.CODIGO, "Ágil: barra más lenta, críticos x3 y veneno.");

    // Atributos de cada clase
    private final String nombre;
    private final String sprite;
    private final int fuerza;
    private final int destreza;
    private final int inteligencia;
    private final String nombreArma;
    private final int energiaArma;
    private final int porcentajeArma;
    private final int criticoArma;
    private final int manaArma;
    private final String hechizoInicial;
    private final String descripcion;

    // Constructor (solo lo usa el propio enum)
    Clase(String nombre, String sprite, int fuerza, int destreza, int inteligencia, String nombreArma,
          int energiaArma, int porcentajeArma, int criticoArma, int manaArma, String hechizoInicial,
          String descripcion) {
        this.nombre = nombre;
        this.sprite = sprite;
        this.fuerza = fuerza;
        this.destreza = destreza;
        this.inteligencia = inteligencia;
        this.nombreArma = nombreArma;
        this.energiaArma = energiaArma;
        this.porcentajeArma = porcentajeArma;
        this.criticoArma = criticoArma;
        this.manaArma = manaArma;
        this.hechizoInicial = hechizoInicial;
        this.descripcion = descripcion;
    }

    // Cada héroe recibe su propia arma (un objeto nuevo)
    public HabilidadArma crearArma() {
        return new HabilidadArma(nombreArma, energiaArma, porcentajeArma, criticoArma, manaArma);
    }

    // Convierte el texto guardado en el archivo ("MAGO") en la clase. Devuelve null si no existe.
    public static Clase porCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        try {
            return Clase.valueOf(codigo);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Las clases en el orden en que se muestran en la pantalla de elección (0..3)
    public static final int CANTIDAD = 4;

    public static Clase porOrden(int orden) {
        if (orden == 0) {
            return CABALLERO;
        }
        if (orden == 1) {
            return MAGO;
        }
        if (orden == 2) {
            return DRUIDA;
        }
        if (orden == 3) {
            return LADRON;
        }
        throw new IllegalArgumentException("orden de clase inválido: " + orden);
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getSprite() {
        return sprite;
    }

    public int getFuerza() {
        return fuerza;
    }

    public int getDestreza() {
        return destreza;
    }

    public int getInteligencia() {
        return inteligencia;
    }

    public String getNombreArma() {
        return nombreArma;
    }

    public String getHechizoInicial() {
        return hechizoInicial;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

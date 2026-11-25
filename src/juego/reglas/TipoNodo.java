package juego.reglas;

// Tipos de lugar que hay en el mapa del bosque.
public enum TipoNodo {
    INICIO("Inicio"),
    COMBATE("Combate"),
    COFRE("Cofre"),
    EVENTO("Evento"),
    JEFE("Jefe final");

    // Atributo
    private final String nombre;

    // Constructor del enum
    TipoNodo(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }
}

package juego.reglas;

// Un lugar del mapa: tipo, piso (columna) y posición en pantalla.
public class NodoMapa {

    // Atributos
    private final int id;
    private final TipoNodo tipo;
    private final int piso;
    private final int x;
    private final int y;
    private boolean visitado;

    // Constructor
    public NodoMapa(int id, TipoNodo tipo, int piso, int x, int y) {
        if (tipo == null) {
            throw new IllegalArgumentException("el nodo necesita un tipo");
        }
        if (piso < 0) {
            throw new IllegalArgumentException("el piso no puede ser negativo: " + piso);
        }
        this.id = id;
        this.tipo = tipo;
        this.piso = piso;
        this.x = x;
        this.y = y;
    }

    // Getters y marca de visita
    public int getId() {
        return id;
    }

    public TipoNodo getTipo() {
        return tipo;
    }

    public int getPiso() {
        return piso;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean estaVisitado() {
        return visitado;
    }

    public void setVisitado(boolean visitado) {
        this.visitado = visitado;
    }
}

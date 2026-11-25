package juego.entidades;

// Utilidad: fabrica de habilidades a partir de su código.
// Las habilidades se desbloquean siempre en el mismo orden (el de este catálogo).
public final class CatalogoHabilidades {

    // Constantes
    public static final int CANTIDAD = 4;

    // Sin instancias
    private CatalogoHabilidades() {
    }

    // Código de la habilidad que ocupa esa posición del catálogo (0..3)
    public static String codigoPorOrden(int orden) {
        if (orden == 0) {
            return HabilidadAtaque.CODIGO;
        }
        if (orden == 1) {
            return HabilidadCuracion.CODIGO;
        }
        if (orden == 2) {
            return HabilidadGolpePotente.CODIGO;
        }
        if (orden == 3) {
            return HabilidadEscape.CODIGO;
        }
        throw new IllegalArgumentException("orden inválido: " + orden);
    }

    public static boolean esValido(String codigo) {
        for (int i = 0; i < CANTIDAD; i++) {
            if (codigoPorOrden(i).equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    public static Habilidad crear(String codigo) {
        if (HabilidadAtaque.CODIGO.equals(codigo)) {
            return new HabilidadAtaque();
        }
        if (HabilidadCuracion.CODIGO.equals(codigo)) {
            return new HabilidadCuracion();
        }
        if (HabilidadGolpePotente.CODIGO.equals(codigo)) {
            return new HabilidadGolpePotente();
        }
        if (HabilidadEscape.CODIGO.equals(codigo)) {
            return new HabilidadEscape();
        }
        throw new IllegalArgumentException("habilidad desconocida: " + codigo);
    }
}

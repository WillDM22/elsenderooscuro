package juego.entidades;

// Utilidad: fábrica de HECHIZOS a partir de su código.
// Los hechizos se aprenden siempre en el mismo orden (el de este catálogo), saltando los que ya se saben.
// Las armas y los puños no están acá: no cuentan en el límite de 4 hechizos.
public final class CatalogoHabilidades {

    // Constantes
    public static final int CANTIDAD = 6;

    // Sin instancias
    private CatalogoHabilidades() {
    }

    // Código del hechizo que ocupa esa posición del catálogo (0..5)
    public static String codigoPorOrden(int orden) {
        if (orden == 0) {
            return HabilidadCuracion.CODIGO;
        }
        if (orden == 1) {
            return HabilidadGolpePotente.CODIGO;
        }
        if (orden == 2) {
            return HabilidadEscape.CODIGO;
        }
        if (orden == 3) {
            return HabilidadFuria.CODIGO;
        }
        if (orden == 4) {
            return HabilidadSavia.CODIGO;
        }
        if (orden == 5) {
            return HabilidadVeneno.CODIGO;
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
        if (HabilidadCuracion.CODIGO.equals(codigo)) {
            return new HabilidadCuracion();
        }
        if (HabilidadGolpePotente.CODIGO.equals(codigo)) {
            return new HabilidadGolpePotente();
        }
        if (HabilidadEscape.CODIGO.equals(codigo)) {
            return new HabilidadEscape();
        }
        if (HabilidadFuria.CODIGO.equals(codigo)) {
            return new HabilidadFuria();
        }
        if (HabilidadSavia.CODIGO.equals(codigo)) {
            return new HabilidadSavia();
        }
        if (HabilidadVeneno.CODIGO.equals(codigo)) {
            return new HabilidadVeneno();
        }
        throw new IllegalArgumentException("habilidad desconocida: " + codigo);
    }
}

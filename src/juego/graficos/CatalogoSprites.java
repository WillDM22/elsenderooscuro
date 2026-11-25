package juego.graficos;

// Catálogo de sprites 16x16 dibujados con texto (el "respaldo" del juego: nunca depende de archivos).
// Cada letra es un color de la Paleta y '.' es transparente. Las filas se separan con '/'.
// Si en la carpeta assets/ existe un .png con el mismo nombre, GestorSprites usa el .png en su lugar.
public final class CatalogoSprites {

    // Nombres de los sprites disponibles

    public static final String JUGADOR = "jugador";
    public static final String ESQUELETO = "esqueleto";
    public static final String LOBO = "lobo";
    public static final String BRUJO = "brujo";
    public static final String REY = "rey";
    public static final String CALAVERA = "calavera";
    public static final String INTERROGACION = "interrogacion";
    public static final String COFRE = "cofre";
    public static final String JEFE = "jefe";
    public static final String INICIO = "inicio";
    public static final String ALTAR = "altar";
    public static final String ARBOL = "arbol";
    public static final String ANTORCHA = "antorcha";
    public static final String CORAZON = "corazon";
    public static final String POCION = "pocion";

    // Cantidad total de sprites
    public static final int CANTIDAD = 15;

    // jugador
    private static final String DIBUJO_JUGADOR =
            "..........KLK.../" +
            "......KKKK.KLK../" +
            ".....KSSSSKKLK../" +
            "....KSLLLLSKYK../" +
            "....KSKKKKSKYK../" +
            "....KSSSSSSKBK../" +
            ".....KFFFFKKBK../" +
            "...KKKRRRRKFBK../" +
            "..KFKRRYYRRKFK../" +
            "..KFKRRYYRRKKK../" +
            "...KKRRRRRRK..../" +
            "....KBBBBBBK..../" +
            "....KRRKKRRK..../" +
            "....KNNK.KNNK.../" +
            "...KNNNK.KNNNK../" +
            "...KKKKK.KKKKK..";

    // esqueleto
    private static final String DIBUJO_ESQUELETO =
            "................/" +
            ".....KKKKKK...../" +
            "....KWWWWWWK..../" +
            "...KWWWWWWWWK.../" +
            "...KWKKWWKKWK.../" +
            "...KWKEWWEKWK.../" +
            "...KWWWKKWWWK.../" +
            "....KWWWWWWK..../" +
            ".....KWKWKWK..../" +
            "..KK.KWWWWK.KK../" +
            ".KWWKKWKKWKKWWK./" +
            "..KK.KWWWWK.KK../" +
            ".....KWKKWK...../" +
            ".....KWK.KWK..../" +
            "....KWWK.KWWK.../" +
            "....KKKK.KKKK...";

    // lobo
    private static final String DIBUJO_LOBO =
            "................/" +
            "..KK........KK../" +
            ".KSSK......KSSK./" +
            ".KSLSKKKKKKSLSK./" +
            "..KSSSSSSSSSSK../" +
            "..KSSKESSKESSK../" +
            "..KSSSSSSSSSSK../" +
            "..KSSSKKKKSSSK../" +
            "...KSSKWKWKSK.../" +
            "....KSSKKKSSK.../" +
            "...KSSSSSSSSK.../" +
            "..KSLSSSSSSLSK../" +
            "..KSSSSKKSSSSK../" +
            "..KSK.KSSK.KSK../" +
            "..KSK.KSSK.KSK../" +
            "..KKK.KKKK.KKK..";

    // brujo
    private static final String DIBUJO_BRUJO =
            ".......KK......./" +
            "......KppK....../" +
            ".....KpPPpK...../" +
            "....KpPPPPpK..../" +
            "...KKKKKKKKKK.../" +
            "....KpKFFKpK..../" +
            "....KpFKKFpK..../" +
            "....KpFFFFpK..../" +
            ".....KpWWpK...../" +
            "..KKKKpppPKKKK../" +
            ".KFKpPPPYPPPpKFK/" +
            ".KOKpPPPPPPPpKOK/" +
            "..KKpPPPPPPPpKK./" +
            "...KpPPPPPPPpK../" +
            "...KpppppppppK../" +
            "...KKKKKKKKKKK..";

    // rey
    private static final String DIBUJO_REY =
            "..Y..Y.YY.Y..Y../" +
            "..YY.YYYYYY.YY../" +
            "..YYYYYYYYYYYY../" +
            "..KYYyyyyyyYYK../" +
            "...KKKKKKKKKK.../" +
            "..KsSSSSSSSSsK../" +
            "..KsSKKSSKKSsK../" +
            "..KsSKEKKEKSsK../" +
            "..KsSSSKKSSSsK../" +
            "...KsSSWWSSsK.../" +
            ".KKKKsSSSSsKKKK./" +
            "KrRKrrRRRRrrKRrK/" +
            "KrRKrRRYYRRrKRrK/" +
            "KssKrRRYYRRrKssK/" +
            ".KKKrRRRRRRrKKK./" +
            "...KKKKKKKKKK...";

    // calavera
    private static final String DIBUJO_CALAVERA =
            "................/" +
            "....KKKKKKKK..../" +
            "...KWWWWWWWWK.../" +
            "..KWWWWWWWWWWK../" +
            "..KWWWWWWWWWWK../" +
            "..KWKKKWWKKKWK../" +
            "..KWKKKWWKKKWK../" +
            "..KWKKKWWKKKWK../" +
            "..KWWWWKKWWWWK../" +
            "...KWWWWWWWWK.../" +
            "....KWWWWWWK..../" +
            "....KWKWKWKWK.../" +
            "....KWKWKWKWK.../" +
            ".....KKKKKKK..../" +
            "................/" +
            "................";

    // interrogacion
    private static final String DIBUJO_INTERROGACION =
            "................/" +
            ".....KKKKKK...../" +
            "....KYYYYYYK..../" +
            "...KYYKKKKYYK.../" +
            "...KYKK..KKYK.../" +
            ".......KKYYK..../" +
            "......KYYYK...../" +
            ".....KYYYK....../" +
            ".....KYYK......./" +
            ".....KYYK......./" +
            ".....KKKK......./" +
            ".....KYYK......./" +
            ".....KYYK......./" +
            ".....KKKK......./" +
            "................/" +
            "................";

    // cofre
    private static final String DIBUJO_COFRE =
            "................/" +
            "................/" +
            "....KKKKKKKK..../" +
            "..KKBBBBBBBBKK../" +
            ".KBbbbbbbbbbbBK./" +
            ".KBbbbbbbbbbbBK./" +
            ".KYYYYYYYYYYYYK./" +
            ".KBBBBBYYBBBBBK./" +
            ".KBbbbbYKYbbbBK./" +
            ".KBbbbbYYYbbbBK./" +
            ".KBbbbbbbbbbbBK./" +
            ".KBbbbbbbbbbbBK./" +
            ".KNBBBBBBBBBBNK./" +
            "..KKKKKKKKKKKK../" +
            "................/" +
            "................";

    // jefe
    private static final String DIBUJO_JEFE =
            "................/" +
            "..Y...Y..Y...Y../" +
            "..YY.YYY.YYY.Y../" +
            "..YYYYYYYYYYYY../" +
            "..KYyYyYyYyYYK../" +
            "...KKKKKKKKKK.../" +
            "..KWWWWWWWWWWK../" +
            "..KWKKKWWKKKWK../" +
            "..KWKRKWWKRKWK../" +
            "..KWKKKWWKKKWK../" +
            "..KWWWWKKWWWWK../" +
            "...KWWWWWWWWK.../" +
            "....KWKWKWKWK.../" +
            "....KKKKKKKKK.../" +
            "................/" +
            "................";

    // inicio
    private static final String DIBUJO_INICIO =
            "................/" +
            "......O........./" +
            ".....OOO......../" +
            "....OOYOO......./" +
            "....OYYYO......./" +
            ".....OYO......../" +
            "...K.KKK.K....../" +
            "...KKBBBKK....../" +
            "....KBNBK......./" +
            "..KKBBKKBBKK..../" +
            ".KBBKK..KKBBK.../" +
            ".KKK......KKK.../" +
            "................/" +
            "................/" +
            "................/" +
            "................";

    // altar
    private static final String DIBUJO_ALTAR =
            "................/" +
            "................/" +
            "......KKKK....../" +
            ".....KYYYYK...../" +
            "......KKKK....../" +
            "....KKLLLLKK..../" +
            "...KLLLLLLLLK.../" +
            "..KKSSSSSSSSKK../" +
            "...KSsSSSSsSK.../" +
            "...KSSSSSSSSK.../" +
            "...KSsSSSSsSK.../" +
            "...KSSSSSSSSK.../" +
            "..KKSSSSSSSSKK../" +
            ".KLLLLLLLLLLLLK./" +
            ".KKKKKKKKKKKKKK./" +
            "................";

    // arbol
    private static final String DIBUJO_ARBOL =
            "................/" +
            ".....KKKKK....../" +
            "...KKGGGGGKK..../" +
            "..KGGgGGGGGGK.../" +
            ".KGGGGGGGgGGGK../" +
            ".KGgGGGGGGGGGK../" +
            "KGGGGGgGGGGgGGK./" +
            "KGGGGGGGGGGGGGK./" +
            ".KGGgGGGGgGGGK../" +
            "..KKGGGGGGGKK.../" +
            "....KKKBBKK...../" +
            ".....KBBBK....../" +
            ".....KBbBK....../" +
            ".....KBBBK....../" +
            "....KBBBBBK...../" +
            "....KKKKKKK.....";

    // antorcha
    private static final String DIBUJO_ANTORCHA =
            "................/" +
            ".......O......../" +
            "......OOO......./" +
            ".....OOYOO....../" +
            ".....OYYYO....../" +
            "......OYO......./" +
            ".......KK......./" +
            ".......KBK....../" +
            ".......KBK....../" +
            ".......KbK....../" +
            ".......KBK....../" +
            ".......KBK....../" +
            ".......KBK....../" +
            ".......KNK....../" +
            ".......KKK....../" +
            "................";

    // corazon
    private static final String DIBUJO_CORAZON =
            "................/" +
            "................/" +
            "..KKK....KKK..../" +
            ".KRRRK..KRRRK.../" +
            "KRRWRRKKRRRRRK../" +
            "KRRRRRRRRRRRRK../" +
            "KRRRRRRRRRRRRK../" +
            ".KRRRRRRRRRRK.../" +
            "..KRRRRRRRRK..../" +
            "...KRRRRRRK...../" +
            "....KRRRRK....../" +
            ".....KRRK......./" +
            "......KK......../" +
            "................/" +
            "................/" +
            "................";

    // pocion
    private static final String DIBUJO_POCION =
            "................/" +
            "......KKKK....../" +
            "......KBBK....../" +
            ".....KKKKKK...../" +
            "......KUUK....../" +
            ".....KUUUUK...../" +
            "....KUUWUUUK..../" +
            "...KUUWUUUUUK.../" +
            "...KUUUUUUUUK.../" +
            "...KUUUUUUUUK.../" +
            "...KuUUUUUUuK.../" +
            "....KuuuuuuK..../" +
            ".....KKKKKK...../" +
            "................/" +
            "................/" +
            "................";

    // Sin instancias
    private CatalogoSprites() {
    }

    // Nombre del sprite número "orden" (0 a CANTIDAD-1)
    public static String nombrePorOrden(int orden) {
        if (orden == 0) {
            return JUGADOR;
        }
        if (orden == 1) {
            return ESQUELETO;
        }
        if (orden == 2) {
            return LOBO;
        }
        if (orden == 3) {
            return BRUJO;
        }
        if (orden == 4) {
            return REY;
        }
        if (orden == 5) {
            return CALAVERA;
        }
        if (orden == 6) {
            return INTERROGACION;
        }
        if (orden == 7) {
            return COFRE;
        }
        if (orden == 8) {
            return JEFE;
        }
        if (orden == 9) {
            return INICIO;
        }
        if (orden == 10) {
            return ALTAR;
        }
        if (orden == 11) {
            return ARBOL;
        }
        if (orden == 12) {
            return ANTORCHA;
        }
        if (orden == 13) {
            return CORAZON;
        }
        if (orden == 14) {
            return POCION;
        }
        throw new IllegalArgumentException("orden inválido: " + orden);
    }

    public static boolean existe(String nombre) {
        for (int i = 0; i < CANTIDAD; i++) {
            if (nombrePorOrden(i).equals(nombre)) {
                return true;
            }
        }
        return false;
    }

    // Dibujo hecho con texto del sprite pedido
    public static Sprite proceduralDe(String nombre) {
        if (JUGADOR.equals(nombre)) {
            return new Sprite(DIBUJO_JUGADOR);
        }
        if (ESQUELETO.equals(nombre)) {
            return new Sprite(DIBUJO_ESQUELETO);
        }
        if (LOBO.equals(nombre)) {
            return new Sprite(DIBUJO_LOBO);
        }
        if (BRUJO.equals(nombre)) {
            return new Sprite(DIBUJO_BRUJO);
        }
        if (REY.equals(nombre)) {
            return new Sprite(DIBUJO_REY);
        }
        if (CALAVERA.equals(nombre)) {
            return new Sprite(DIBUJO_CALAVERA);
        }
        if (INTERROGACION.equals(nombre)) {
            return new Sprite(DIBUJO_INTERROGACION);
        }
        if (COFRE.equals(nombre)) {
            return new Sprite(DIBUJO_COFRE);
        }
        if (JEFE.equals(nombre)) {
            return new Sprite(DIBUJO_JEFE);
        }
        if (INICIO.equals(nombre)) {
            return new Sprite(DIBUJO_INICIO);
        }
        if (ALTAR.equals(nombre)) {
            return new Sprite(DIBUJO_ALTAR);
        }
        if (ARBOL.equals(nombre)) {
            return new Sprite(DIBUJO_ARBOL);
        }
        if (ANTORCHA.equals(nombre)) {
            return new Sprite(DIBUJO_ANTORCHA);
        }
        if (CORAZON.equals(nombre)) {
            return new Sprite(DIBUJO_CORAZON);
        }
        if (POCION.equals(nombre)) {
            return new Sprite(DIBUJO_POCION);
        }
        throw new IllegalArgumentException("no existe el sprite: " + nombre);
    }
}

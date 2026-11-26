package juego.graficos;

// Ilustración que acompaña a cada evento (la imagen que se ve a la izquierda del pergamino).
// Los archivos están en src/ui/MenuEventoAlea/Ilustraciones. Un evento nuevo que se agregue a eventos.txt y no
// figure en esta tabla no se rompe: devuelve null y la pantalla usa el dibujo de siempre.
public final class Ilustraciones {

    // Cada fila: título del evento (como está en datos/eventos.txt) y nombre del archivo
    private static final String[][] TABLA = {
        {"Altares gemelos", "altares"},
        {"El mercader errante", "mercader"},
        {"La fuente de aguas negras", "fuente"},
        {"El ermitaño de la celda", "ermitano"},
        {"Emboscada en el pasillo", "emboscada"},
        {"El herrero abandonado", "herrero"},
        {"Biblioteca en ruinas", "biblioteca"},
        {"El puente del troll", "puente"},
        {"El pasillo se bifurca en la niebla", "niebla"},
        {"El campamento abandonado", "campamento"},
        {"El espejo roto", "espejo"},
        {"El túnel de telarañas", "telaranas"},
        {"El ladrón de la encrucijada", "ladron"},
        {"La estatua que respira", "estatua"}
    };

    private static final String CARPETA = "MenuEventoAlea/Ilustraciones/";

    // Sin instancias
    private Ilustraciones() {
    }

    // Ruta de la ilustración (dentro de src/ui) o null si ese evento no tiene una
    public static String ruta(String titulo, boolean esCofre) {
        if (esCofre) {
            return CARPETA + "cofre.png";
        }
        for (int i = 0; i < TABLA.length; i++) {
            if (TABLA[i][0].equalsIgnoreCase(titulo)) {
                return CARPETA + TABLA[i][1] + ".png";
            }
        }
        return null;
    }
}

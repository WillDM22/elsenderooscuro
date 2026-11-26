package juego.graficos;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

// Las dos tipografías del juego (carpeta src/ui/Fuentes):
//   - CUERPO: "Alkhemikal", la letra de todos los textos corrientes y de los números.
//   - TITULO: "gothic-pixel-font", la letra gótica de títulos y opciones. No tiene tildes ni números.
// Se buscan igual que las imágenes: primero en el classpath y luego como archivo. Si una fuente falta o está dañada
// se usa una fuente del sistema, así que el juego siempre se puede leer.
public final class Fuentes {

    // Fuente base (tamaño 1 punto: se agranda con deriveFont cada vez que se usa)
    private static final Font CUERPO = cargar("Fuentes/Alkhemikal.ttf", Font.SANS_SERIF);
    private static final Font TITULO = cargar("Fuentes/gothic-pixel-font.ttf", Font.SERIF);
    // Fuente de último recurso para símbolos que ninguna de las dos tiene (flechas, corazones, etc.)
    private static final Font RESPALDO = new Font(Font.SANS_SERIF, Font.BOLD, 1);

    // Sin instancias
    private Fuentes() {
    }

    public static Font cuerpo(float tamanio) {
        return CUERPO.deriveFont(tamanio);
    }

    public static Font titulo(float tamanio) {
        return TITULO.deriveFont(tamanio);
    }

    public static Font respaldo(float tamanio) {
        return RESPALDO.deriveFont(tamanio);
    }

    // Lee un .ttf; si no se puede, devuelve una fuente del sistema
    private static Font cargar(String ruta, String familiaDeRespaldo) {
        try {
            InputStream flujo = Fuentes.class.getResourceAsStream("/ui/" + ruta);
            if (flujo != null) {
                try {
                    return Font.createFont(Font.TRUETYPE_FONT, flujo);
                } finally {
                    flujo.close();
                }
            }
            File archivo = new File("src/ui/" + ruta);
            if (archivo.isFile()) {
                return Font.createFont(Font.TRUETYPE_FONT, archivo);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("ADVERTENCIA: no se pudo leer la fuente ui/" + ruta + ": " + e.getMessage());
        }
        System.err.println("ADVERTENCIA: falta la fuente ui/" + ruta + ". Se usa una fuente del sistema.");
        return new Font(familiaDeRespaldo, Font.BOLD, 1);
    }
}

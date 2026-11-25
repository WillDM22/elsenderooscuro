package juego.graficos;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// Herramienta aparte (no forma parte del juego): guarda todos los sprites hechos con texto como archivos
// .png de 16x16 en la carpeta assets/. Sirve para abrirlos en un editor de pixel-art (Piskel, Aseprite, GIMP),
// retocarlos y que el juego use la versión nueva automáticamente (GestorSprites prefiere el .png).
//
// Uso:  java -cp out juego.graficos.ExportadorSprites [carpeta]
public class ExportadorSprites {

    // Punto de entrada
    public static void main(String[] args) {
        String carpeta = args.length > 0 ? args[0] : "assets";
        new File(carpeta).mkdirs();
        for (int i = 0; i < CatalogoSprites.CANTIDAD; i++) {
            String nombre = CatalogoSprites.nombrePorOrden(i);
            File destino = new File(carpeta, nombre + ".png");
            try {
                ImageIO.write(CatalogoSprites.proceduralDe(nombre).crearImagen(1), "png", destino);
                System.out.println("Guardado " + destino.getPath());
            } catch (IOException e) {
                System.out.println("No se pudo guardar " + destino.getPath() + ": " + e.getMessage());
            }
        }
    }
}

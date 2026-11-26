package juego;

import juego.pantallas.EscenaMenu;
import juego.recursos.Recursos;
import juego.ventana.VentanaJuego;

import java.io.File;

public class Main {

    // Punto de entrada: abre la ventana con el menú principal.
    // Las carpetas "datos" (eventos.txt, guardado.txt y opciones.txt) y "assets" (imágenes opcionales)
    // están en la raíz del proyecto. Cuando el juego se empaqueta como .exe, el lanzador define la propiedad
    // "solis.base" con la carpeta de la aplicación, y de ahí se leen "datos" y "assets".
    public static void main(String[] args) {
        String base = System.getProperty("solis.base");
        String prefijo = (base == null || base.isEmpty()) ? "" : base + File.separator;
        Recursos recursos = new Recursos(prefijo + "datos", prefijo + "assets");
        VentanaJuego.abrir("Solis Umbra", recursos.getOpciones(), new EscenaMenu(recursos));
    }
}

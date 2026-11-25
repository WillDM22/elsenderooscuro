package juego;

import juego.pantallas.EscenaMenu;
import juego.recursos.Recursos;
import motor.Ventana;

public class Main {

    // Punto de entrada: abre la ventana con el menú principal.
    // Las carpetas "datos" (eventos.txt y guardado.txt) y "assets" (imágenes opcionales) están en la raíz del proyecto.
    public static void main(String[] args) {
        Recursos recursos = new Recursos("datos", "assets");
        Ventana.abrir("El Sendero Oscuro", 1024, 640, new EscenaMenu(recursos));
    }
}

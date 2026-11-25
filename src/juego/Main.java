package juego;

import juego.pantallas.EscenaMenu;
import juego.recursos.Recursos;
import juego.ventana.VentanaJuego;

public class Main {

    // Punto de entrada: abre la ventana con el menú principal.
    // Las carpetas "datos" (eventos.txt, guardado.txt y opciones.txt) y "assets" (imágenes opcionales)
    // están en la raíz del proyecto.
    public static void main(String[] args) {
        Recursos recursos = new Recursos("datos", "assets");
        VentanaJuego.abrir("El Sendero Oscuro", recursos.getOpciones(), new EscenaMenu(recursos));
    }
}

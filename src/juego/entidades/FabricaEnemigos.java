package juego.entidades;

import java.util.Random;

// Utilidad: crea enemigos. El piso del mapa los vuelve más duros.
public final class FabricaEnemigos {

    // Sin instancias
    private FabricaEnemigos() {
    }

    // Enemigo al azar: Esqueleto o Lobo; desde el piso 3 también puede salir un Brujo
    public static Enemigo crearAleatorio(int piso, Random azar) {
        int tipos = 2;
        if (piso >= 3) {
            tipos = 3;
        }
        int elegido = azar.nextInt(tipos);
        Enemigo enemigo;
        if (elegido == 0) {
            enemigo = new Esqueleto();
        } else if (elegido == 1) {
            enemigo = new Lobo();
        } else {
            enemigo = new Brujo();
        }
        enemigo.reforzar(piso);
        return enemigo;
    }

    // Enemigo por nombre (lo usan las emboscadas escritas en eventos.txt)
    public static Enemigo crearPorNombre(String nombre, int piso) {
        Enemigo enemigo;
        if ("ESQUELETO".equals(nombre)) {
            enemigo = new Esqueleto();
        } else if ("LOBO".equals(nombre)) {
            enemigo = new Lobo();
        } else if ("BRUJO".equals(nombre)) {
            enemigo = new Brujo();
        } else {
            throw new IllegalArgumentException("enemigo desconocido: " + nombre);
        }
        enemigo.reforzar(piso);
        return enemigo;
    }

    public static Enemigo crearJefe() {
        return new ReyOscuro();
    }
}

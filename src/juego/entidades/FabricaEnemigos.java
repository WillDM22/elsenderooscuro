package juego.entidades;

import java.util.Random;

// Utilidad: crea enemigos. El piso del mapa decide cuáles pueden aparecer y los vuelve más duros.
public final class FabricaEnemigos {

    // Sin instancias
    private FabricaEnemigos() {
    }

    // Cuántos tipos pueden aparecer en ese piso:
    //   piso 1: Esqueleto, Lobo, Goblin | piso 2: + Araña | piso 3: + Brujo, Espectro | piso 4 o más: + Golem
    public static int tiposDisponibles(int piso) {
        if (piso <= 1) {
            return 3;
        }
        if (piso == 2) {
            return 4;
        }
        if (piso == 3) {
            return 6;
        }
        return 7;
    }

    // Enemigo al azar entre los tipos disponibles del piso
    public static Enemigo crearAleatorio(int piso, Random azar) {
        int elegido = azar.nextInt(tiposDisponibles(piso));
        Enemigo enemigo;
        if (elegido == 0) {
            enemigo = new Esqueleto();
        } else if (elegido == 1) {
            enemigo = new Lobo();
        } else if (elegido == 2) {
            enemigo = new Goblin();
        } else if (elegido == 3) {
            enemigo = new Arana();
        } else if (elegido == 4) {
            enemigo = new Brujo();
        } else if (elegido == 5) {
            enemigo = new Espectro();
        } else {
            enemigo = new Golem();
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
        } else if ("GOBLIN".equals(nombre)) {
            enemigo = new Goblin();
        } else if ("ARANA".equals(nombre)) {
            enemigo = new Arana();
        } else if ("GOLEM".equals(nombre)) {
            enemigo = new Golem();
        } else if ("ESPECTRO".equals(nombre)) {
            enemigo = new Espectro();
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

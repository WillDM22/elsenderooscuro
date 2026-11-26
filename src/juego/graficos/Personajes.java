package juego.graficos;

import java.awt.Graphics2D;

// Los héroes y enemigos del combate: cada uno tiene cuatro imágenes (normal, ataque, dañado y muerto) en la
// carpeta src/ui/SpriteSheets. Esta clase sabe qué archivo corresponde a cada personaje y pose, y los dibuja
// todos con la misma escala y apoyados en el suelo.
public final class Personajes {

    // Poses
    public static final int NORMAL = 0;
    public static final int ATAQUE = 1;
    public static final int DANIADO = 2;
    public static final int MUERTO = 3;

    // Todas las imágenes se achican con el mismo factor, así los personajes mantienen su tamaño relativo
    public static final double ESCALA = 0.265;

    // Nombre de los archivos de cada personaje, en el orden de las poses (los nombres respetan los archivos tal
    // como están en la carpeta, incluso los que tienen alguna letra distinta, como "EsqueltoNormal").
    private static final String[][] ARCHIVOS = {
        {"caballero", "CaballeroNormal", "CaballeroAtaque", "CaballeroDaniado", "CaballeroMuerto"},
        {"mago", "MagoNormal", "MagoAtaque", "MagoDaniado", "MagoMuerto"},
        {"druida", "DruidaNormal", "DruidaAtaque", "DruidaDaniado", "DruidaMuerto"},
        {"ladron", "PicaroNormal", "PicaroAtaque", "PicaroDaniado", "PicaroMuerto"},
        {"esqueleto", "EsqueltoNormal", "EsqueletoAtaque", "EsqueletoDaniado", "EsqueletoMuerto"},
        {"lobo", "LoboNormal", "LoboAtaquel", "LoboDaniado", "LoboMuerto"},
        {"brujo", "BrujoNormal", "BrujoAtaque", "BrujoDaniado", "BrujoMuerto"},
        {"goblin", "GoblinNormal", "GoblinAtaque", "GoblinDaniado", "GoblinMuerto"},
        {"arana", "AraniaNormal", "AraniaAtaque", "AraniaDaniada", "AraniaMuerta"},
        {"golem", "GolemNormal", "GolemAtaque", "GolemDaniado", "GolemMuerto+"},
        {"espectro", "EspectroNormal", "EspectroAtaque", "EspectroDaniado", "EspectroMuerto"},
        {"rey", "ReyOscuroNormal", "ReyOscuroAtaque", "ReyOscuroDaniado", "ReyOscuroMuerto"}
    };

    // Altura del suelo en el combate (donde apoyan los pies) y altura a la que flotan los fantasmas
    public static final double SUELO = 384;
    private static final double ALTURA_FANTASMA = 352;
    private static final double ALTURA_BRUJO = 372;

    // Sin instancias
    private Personajes() {
    }

    // Ruta de la imagen dentro de src/ui, o null si el personaje no existe
    public static String ruta(String sprite, int pose) {
        for (int i = 0; i < ARCHIVOS.length; i++) {
            if (ARCHIVOS[i][0].equals(sprite)) {
                return "SpriteSheets/" + ARCHIVOS[i][pose + 1] + ".png";
            }
        }
        return null;
    }

    // ¿Es un personaje que flota? Se dibuja más arriba que los que caminan.
    public static double alturaDePies(String sprite) {
        if (sprite.equals("espectro")) {
            return ALTURA_FANTASMA;
        }
        if (sprite.equals("brujo")) {
            return ALTURA_BRUJO;
        }
        return SUELO;
    }

    // Dibuja al personaje centrado en cx con los pies en "ypies". Devuelve false si falta la imagen.
    public static boolean dibujar(ImagenesUi ui, Graphics2D g, String sprite, int pose, double cx, double ypies) {
        String ruta = ruta(sprite, pose);
        if (ruta == null) {
            return false;
        }
        return ui.dibujarPersonaje(g, ruta, cx, ypies, ESCALA, false);
    }
}

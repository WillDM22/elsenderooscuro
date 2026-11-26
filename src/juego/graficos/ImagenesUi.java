package juego.graficos;

import estructuras.ListaEnlazada;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

// Cargador de las imágenes de la interfaz (carpeta src/ui: menús, HUD, fondos, personajes).
//   - Busca la imagen primero dentro del classpath ("/ui/...", sirve al empaquetar en un .jar) y, si no está,
//     como archivo "src/ui/..." relativo a la carpeta desde donde se ejecuta el juego.
//   - Si una imagen no existe o está dañada devuelve null y el que la pidió usa su dibujo de respaldo,
//     así el juego nunca se cae por una imagen faltante.
//   - Cada imagen se lee una sola vez (caché en una ListaEnlazada). También se guardan las versiones
//     achicadas, porque achicar una imagen grande en cada cuadro sería muy lento.
//
// Las imágenes de la UI vienen con un margen transparente (de unos 36 px) alrededor. Por eso casi todos los
// métodos "dibujar" ubican la parte VISIBLE de la imagen (su "caja"), no la esquina del archivo.
public class ImagenesUi {

    // Una entrada del caché: la imagen original, la caja de lo visible y las versiones achicadas
    private static class Guardada {
        private final String ruta;
        private final BufferedImage imagen;
        private final int cajaX;
        private final int cajaY;
        private final int cajaAncho;
        private final int cajaAlto;
        private final ListaEnlazada<Achicada> achicadas = new ListaEnlazada<>();
        private final ListaEnlazada<Achicada> recortadas = new ListaEnlazada<>();

        private Guardada(String ruta, BufferedImage imagen) {
            this.ruta = ruta;
            this.imagen = imagen;
            int minX = imagen.getWidth();
            int minY = imagen.getHeight();
            int maxX = -1;
            int maxY = -1;
            for (int y = 0; y < imagen.getHeight(); y++) {
                for (int x = 0; x < imagen.getWidth(); x++) {
                    if ((imagen.getRGB(x, y) >>> 24) > 20) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            if (maxX < 0) {
                minX = 0;
                minY = 0;
                maxX = imagen.getWidth() - 1;
                maxY = imagen.getHeight() - 1;
            }
            this.cajaX = minX;
            this.cajaY = minY;
            this.cajaAncho = maxX - minX + 1;
            this.cajaAlto = maxY - minY + 1;
        }
    }

    // Una versión achicada de una imagen (para no repetir el trabajo)
    private static class Achicada {
        private final int ancho;
        private final int alto;
        private final BufferedImage imagen;

        private Achicada(int ancho, int alto, BufferedImage imagen) {
            this.ancho = ancho;
            this.alto = alto;
            this.imagen = imagen;
        }
    }

    // Rutas dentro de src/ui (sin la barra inicial). Las constantes evitan errores de tipeo en las pantallas.
    public static final String ICONO = "ico.png";
    public static final String PUNTERO = "Puntero/mouse normal.png";

    // Atributos
    private final ListaEnlazada<Guardada> cache = new ListaEnlazada<>();
    private final ListaEnlazada<String> faltantes = new ListaEnlazada<>();

    // ¿Existe la imagen? (la carga si hace falta)
    public boolean existe(String ruta) {
        return buscar(ruta) != null;
    }

    // Imagen original tal como está en el archivo (con su margen transparente). Null si no existe.
    public BufferedImage obtener(String ruta) {
        Guardada g = buscar(ruta);
        return g == null ? null : g.imagen;
    }

    // Copia de la parte visible de la imagen (sin el margen transparente). Null si no existe.
    public BufferedImage recortada(String ruta) {
        Guardada g = buscar(ruta);
        if (g == null) {
            return null;
        }
        BufferedImage copia = new BufferedImage(g.cajaAncho, g.cajaAlto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copia.createGraphics();
        g2.drawImage(g.imagen, -g.cajaX, -g.cajaY, null);
        g2.dispose();
        return copia;
    }

    // Ancho y alto de la parte visible de la imagen
    public int anchoVisible(String ruta) {
        Guardada g = buscar(ruta);
        return g == null ? 0 : g.cajaAncho;
    }

    public int altoVisible(String ruta) {
        Guardada g = buscar(ruta);
        return g == null ? 0 : g.cajaAlto;
    }

    // Dibuja la imagen de modo que la esquina superior izquierda de su parte VISIBLE quede en (x, y).
    // Devuelve false si la imagen no existe (para que quien llamó dibuje otra cosa).
    public boolean dibujar(Graphics2D g, String ruta, double x, double y) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return false;
        }
        g.drawImage(e.imagen, (int) Math.round(x - e.cajaX), (int) Math.round(y - e.cajaY), null);
        return true;
    }

    // Igual, pero centrando la parte visible en (cx, cy)
    public boolean dibujarCentrado(Graphics2D g, String ruta, double cx, double cy) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return false;
        }
        return dibujar(g, ruta, cx - e.cajaAncho / 2.0, cy - e.cajaAlto / 2.0);
    }

    // Dibuja la parte visible, pero salteando las filas "desde".."hasta" (se usa para acortar un marco sin deformarlo:
    // las filas del medio solo son relleno, así que sacarlas no se nota). Las filas se cuentan desde arriba de lo visible.
    public boolean dibujarSinFilas(Graphics2D g, String ruta, double x, double y, int desde, int hasta) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return false;
        }
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        int ancho = e.cajaAncho;
        // Parte de arriba
        g.drawImage(e.imagen, xi, yi, xi + ancho, yi + desde,
                e.cajaX, e.cajaY, e.cajaX + ancho, e.cajaY + desde, null);
        // Parte de abajo, pegada a la de arriba
        int restante = e.cajaAlto - hasta;
        g.drawImage(e.imagen, xi, yi + desde, xi + ancho, yi + desde + restante,
                e.cajaX, e.cajaY + hasta, e.cajaX + ancho, e.cajaY + hasta + restante, null);
        return true;
    }

    // Dibuja la imagen COMPLETA (sin recortar el margen) estirada a ocupar ancho x alto desde (x, y).
    // Es lo que se usa para los fondos, que se estiran a toda la ventana.
    public boolean dibujarEstirada(Graphics2D g, String ruta, int x, int y, int ancho, int alto) {
        BufferedImage imagen = achicada(ruta, ancho, alto);
        if (imagen == null) {
            return false;
        }
        g.drawImage(imagen, x, y, null);
        return true;
    }

    // Para las capturas de pantalla que traen un borde de ventana: toma lo visible, le saca "borde" píxeles por cada
    // lado y lo estira a ocupar ancho x alto desde (x, y).
    public boolean dibujarVisibleEstirada(Graphics2D g, String ruta, int x, int y, int ancho, int alto, int borde) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return false;
        }
        BufferedImage hecha = null;
        for (int i = 0; i < e.recortadas.tamanio(); i++) {
            Achicada a = e.recortadas.obtener(i);
            if (a.ancho == ancho && a.alto == alto) {
                hecha = a.imagen;
            }
        }
        if (hecha == null) {
            BufferedImage recorte = e.imagen.getSubimage(e.cajaX + borde, e.cajaY + borde,
                    e.cajaAncho - 2 * borde, e.cajaAlto - 2 * borde);
            hecha = escalar(recorte, ancho, alto);
            e.recortadas.agregar(new Achicada(ancho, alto, hecha));
        }
        g.drawImage(hecha, x, y, null);
        return true;
    }

    // Dibuja un personaje (o enemigo) con los pies en (cx, ypies), achicado por "escala" y, si espejado, dado vuelta.
    // Devuelve false si la imagen no existe.
    public boolean dibujarPersonaje(Graphics2D g, String ruta, double cx, double ypies, double escala, boolean espejado) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return false;
        }
        int ancho = Math.max(1, (int) Math.round(e.cajaAncho * escala));
        int alto = Math.max(1, (int) Math.round(e.cajaAlto * escala));
        BufferedImage recorte = achicadaVisible(e, ancho, alto);
        int x = (int) Math.round(cx - ancho / 2.0);
        int y = (int) Math.round(ypies - alto);
        if (espejado) {
            g.drawImage(recorte, x + ancho, y, -ancho, alto, null);
        } else {
            g.drawImage(recorte, x, y, null);
        }
        return true;
    }

    // Tamaño (ancho, alto) que tendría el personaje dibujado con esa escala
    public int anchoPersonaje(String ruta, double escala) {
        Guardada e = buscar(ruta);
        return e == null ? 0 : (int) Math.round(e.cajaAncho * escala);
    }

    public int altoPersonaje(String ruta, double escala) {
        Guardada e = buscar(ruta);
        return e == null ? 0 : (int) Math.round(e.cajaAlto * escala);
    }

    // Búsqueda y carga
    private Guardada buscar(String ruta) {
        for (int i = 0; i < cache.tamanio(); i++) {
            Guardada g = cache.obtener(i);
            if (g.ruta.equals(ruta)) {
                return g;
            }
        }
        if (faltantes.indiceDe(ruta) >= 0) {
            return null;
        }
        BufferedImage imagen = leer(ruta);
        if (imagen == null) {
            faltantes.agregar(ruta);
            System.err.println("ADVERTENCIA: no se pudo leer la imagen ui/" + ruta + ". Se usa un dibujo de respaldo.");
            return null;
        }
        Guardada nueva = new Guardada(ruta, imagen);
        cache.agregar(nueva);
        return nueva;
    }

    private BufferedImage leer(String ruta) {
        try {
            URL recurso = ImagenesUi.class.getResource("/ui/" + ruta);
            if (recurso != null) {
                return ImagenesUi.leerConvertida(ImageIO.read(recurso));
            }
            File archivo = new File("src/ui/" + ruta);
            if (archivo.isFile()) {
                return ImagenesUi.leerConvertida(ImageIO.read(archivo));
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    // Pasa la imagen a ARGB (más rápido de dibujar y con transparencia asegurada)
    private static BufferedImage leerConvertida(BufferedImage original) {
        if (original == null) {
            return null;
        }
        if (original.getType() == BufferedImage.TYPE_INT_ARGB) {
            return original;
        }
        BufferedImage copia = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copia.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copia;
    }

    // Versiones achicadas
    private BufferedImage achicada(String ruta, int ancho, int alto) {
        Guardada e = buscar(ruta);
        if (e == null) {
            return null;
        }
        for (int i = 0; i < e.achicadas.tamanio(); i++) {
            Achicada a = e.achicadas.obtener(i);
            if (a.ancho == ancho && a.alto == alto && a.imagen != null) {
                return a.imagen;
            }
        }
        BufferedImage nueva = escalar(e.imagen, ancho, alto);
        e.achicadas.agregar(new Achicada(ancho, alto, nueva));
        return nueva;
    }

    // Igual, pero de la parte visible nada más (la que se usa para los personajes)
    private BufferedImage achicadaVisible(Guardada e, int ancho, int alto) {
        // El tamaño pedido se guarda con signo negativo en el ancho para distinguir "visible" de "completa"
        for (int i = 0; i < e.achicadas.tamanio(); i++) {
            Achicada a = e.achicadas.obtener(i);
            if (a.ancho == -ancho && a.alto == alto) {
                return a.imagen;
            }
        }
        BufferedImage visible = e.imagen.getSubimage(e.cajaX, e.cajaY, e.cajaAncho, e.cajaAlto);
        BufferedImage nueva = escalar(visible, ancho, alto);
        e.achicadas.agregar(new Achicada(-ancho, alto, nueva));
        return nueva;
    }

    // Cambia el tamaño con buena calidad: si hay que achicar mucho se hace de a mitades (si no, salen dientes de sierra)
    private static BufferedImage escalar(BufferedImage origen, int ancho, int alto) {
        BufferedImage actual = origen;
        int anchoActual = origen.getWidth();
        int altoActual = origen.getHeight();
        while (anchoActual / 2 >= ancho && altoActual / 2 >= alto) {
            anchoActual = anchoActual / 2;
            altoActual = altoActual / 2;
            actual = pasoDeEscala(actual, anchoActual, altoActual);
        }
        if (anchoActual != ancho || altoActual != alto) {
            actual = pasoDeEscala(actual, ancho, alto);
        }
        return actual;
    }

    private static BufferedImage pasoDeEscala(BufferedImage origen, int ancho, int alto) {
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origen, 0, 0, ancho, alto, null);
        g.dispose();
        return destino;
    }
}

package juego.graficos;

import estructuras.Grafo;
import estructuras.ListaEnlazada;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import juego.reglas.MapaMazmorra;
import juego.reglas.NodoMapa;

// Ejemplo de renderizado medieval con Graphics2D: fondos de bosque, senderos punteados,
// marcos de pergamino, cuadros de diálogo oscuros con borde dorado y flecha de selección.
// Todo se dibuja con rectángulos, líneas y los sprites de texto: no hace falta ningún archivo externo.
public final class DibujoMedieval {

    // Sin instancias
    private DibujoMedieval() {
    }

    // Fondos
    // Bosque oscuro: degradado verde, "ruido" de pasto, árboles en filas y bordes sombreados.
    // Se dibuja una sola vez y se guarda en una imagen (dibujarla después es muy barato).
    public static BufferedImage crearFondoBosque(GestorSprites sprites, int ancho, int alto, long semilla) {
        BufferedImage fondo = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fondo.createGraphics();
        Random azar = new Random(semilla);

        g.setPaint(new GradientPaint(0, 0, new Color(14, 34, 28), 0, alto, new Color(34, 78, 44)));
        g.fillRect(0, 0, ancho, alto);

        // Pasto: cuadraditos de verdes parecidos
        for (int i = 0; i < ancho * alto / 260; i++) {
            int x = azar.nextInt(ancho);
            int y = azar.nextInt(alto);
            int brillo = azar.nextInt(30);
            g.setColor(new Color(30 + brillo / 2, 70 + brillo, 38 + brillo / 2, 140));
            g.fillRect(x, y, 4, 4);
        }

        // Árboles: filas desde el fondo (arriba) hacia el frente (abajo)
        for (int y = 30; y < alto + 60; y += 78) {
            for (int x = -30; x < ancho + 30; x += 96) {
                int px = x + azar.nextInt(50);
                int py = y + azar.nextInt(30);
                g.drawImage(sprites.obtener(CatalogoSprites.ARBOL, 6), px, py - 96, null);
            }
        }

        sombrearBordes(g, ancho, alto);
        g.dispose();
        return fondo;
    }

    // Mapa: el mismo bosque pero con claros de tierra donde están los lugares y un sendero ancho entre ellos
    public static BufferedImage crearFondoMapa(GestorSprites sprites, MapaMazmorra mapa) {
        BufferedImage fondo = crearFondoBosque(sprites, MapaMazmorra.ANCHO, MapaMazmorra.ALTO, mapa.getSemilla());
        Graphics2D g = fondo.createGraphics();
        dibujarSenderosYClaros(g, mapa);
        sombrearBordes(g, MapaMazmorra.ANCHO, MapaMazmorra.ALTO);
        g.dispose();
        return fondo;
    }

    // Senderos anchos de tierra entre los lugares y un claro de tierra alrededor de cada uno.
    // Se dibujan sobre el fondo que esté puesto (el bosque o el muro del calabozo).
    public static void dibujarSenderosYClaros(Graphics2D g, MapaMazmorra mapa) {
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Grafo<NodoMapa> grafo = mapa.getGrafo();

        // Senderos de tierra
        g.setStroke(new BasicStroke(34, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(110, 78, 40, 170));
        for (int id = 0; id < grafo.cantidadVertices(); id++) {
            NodoMapa origen = grafo.obtener(id);
            ListaEnlazada<NodoMapa> vecinos = grafo.adyacentes(id);
            for (int k = 0; k < vecinos.tamanio(); k++) {
                NodoMapa destino = vecinos.obtener(k);
                g.draw(curva(origen.getX(), origen.getY(), destino.getX(), destino.getY()));
            }
        }
        // Claros alrededor de cada lugar
        for (int id = 0; id < grafo.cantidadVertices(); id++) {
            NodoMapa nodo = grafo.obtener(id);
            g.setColor(new Color(112, 80, 42, 215));
            g.fill(new Ellipse2D.Double(nodo.getX() - 46, nodo.getY() - 46, 92, 92));
            g.setColor(new Color(60, 100, 50, 110));
            g.setStroke(new BasicStroke(3));
            g.draw(new Ellipse2D.Double(nodo.getX() - 48, nodo.getY() - 48, 96, 96));
        }
    }

    // Franja de bosque (para el borde de las pantallas de menú, fin, etc.)
    public static void dibujarFranjaBosque(Graphics2D g, GestorSprites sprites, int ancho, int y, long semilla) {
        Random azar = new Random(semilla);
        for (int x = -20; x < ancho; x += 70) {
            g.drawImage(sprites.obtener(CatalogoSprites.ARBOL, 5), x + azar.nextInt(20), y - 60 + azar.nextInt(24), null);
        }
    }

    // Marcos y cuadros
    // Cuadro de diálogo oscuro con doble borde (estilo RPG clásico)
    public static void cuadroDialogo(Graphics2D g, int x, int y, int ancho, int alto) {
        g.setColor(new Color(8, 6, 14, 235));
        g.fillRect(x, y, ancho, alto);
        g.setColor(Paleta.ORO_OSCURO);
        g.fillRect(x, y, ancho, 4);
        g.fillRect(x, y + alto - 4, ancho, 4);
        g.fillRect(x, y, 4, alto);
        g.fillRect(x + ancho - 4, y, 4, alto);
        g.setColor(Paleta.PIEDRA_CLARA);
        g.drawRect(x + 7, y + 7, ancho - 15, alto - 15);
        // Esquinas doradas
        g.setColor(Paleta.ORO);
        g.fillRect(x - 2, y - 2, 12, 12);
        g.fillRect(x + ancho - 10, y - 2, 12, 12);
        g.fillRect(x - 2, y + alto - 10, 12, 12);
        g.fillRect(x + ancho - 10, y + alto - 10, 12, 12);
    }

    // Pergamino: papel beige con bordes arrugados y marco de madera
    public static void marcoPergamino(Graphics2D g, int x, int y, int ancho, int alto) {
        g.setColor(Paleta.MADERA_OSCURA);
        g.fillRect(x - 8, y - 8, ancho + 16, alto + 16);
        g.setColor(Paleta.MADERA);
        g.fillRect(x - 5, y - 5, ancho + 10, alto + 10);
        g.setPaint(new GradientPaint(x, y, Paleta.PERGAMINO, x, y + alto, Paleta.PERGAMINO_OSCURO));
        g.fillRect(x, y, ancho, alto);
        g.setColor(new Color(150, 110, 60, 90));
        g.drawRect(x + 6, y + 6, ancho - 13, alto - 13);
        // Rodillos arriba y abajo
        g.setColor(Paleta.MADERA_OSCURA);
        g.fillRect(x - 12, y - 12, ancho + 24, 10);
        g.fillRect(x - 12, y + alto + 2, ancho + 24, 10);
        g.setColor(Paleta.ORO_OSCURO);
        g.fillRect(x - 12, y - 12, 8, 10);
        g.fillRect(x + ancho + 4, y - 12, 8, 10);
        g.fillRect(x - 12, y + alto + 2, 8, 10);
        g.fillRect(x + ancho + 4, y + alto + 2, 8, 10);
    }

    // Flecha de selección apuntando a la derecha: un triángulo dorado con borde negro
    public static void flecha(Graphics2D g, double x, double y, double tamanio) {
        Path2D.Double triangulo = new Path2D.Double();
        triangulo.moveTo(x, y - tamanio);
        triangulo.lineTo(x + tamanio * 1.4, y);
        triangulo.lineTo(x, y + tamanio);
        triangulo.closePath();
        g.setColor(Paleta.NEGRO);
        g.setStroke(new BasicStroke(4));
        g.draw(triangulo);
        g.setColor(Paleta.ORO);
        g.fill(triangulo);
    }

    // Barra de vida o de maná con borde
    public static void barra(Graphics2D g, int x, int y, int ancho, int alto, double valor, double maximo,
                             Color relleno, Color fondo) {
        g.setColor(Paleta.NEGRO);
        g.fillRect(x - 3, y - 3, ancho + 6, alto + 6);
        g.setColor(fondo);
        g.fillRect(x, y, ancho, alto);
        double proporcion = maximo <= 0 ? 0 : Math.max(0, Math.min(1, valor / maximo));
        int lleno = (int) Math.round(ancho * proporcion);
        g.setColor(relleno);
        g.fillRect(x, y, lleno, alto);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x, y, lleno, Math.max(2, alto / 3));
    }

    // Sendero punteado entre dos lugares: cuadraditos a lo largo de una curva suave
    public static void sendero(Graphics2D g, double x1, double y1, double x2, double y2, Color color, int punto) {
        QuadCurve2D.Double curva = curva(x1, y1, x2, y2);
        double largo = Math.hypot(x2 - x1, y2 - y1);
        int pasos = (int) Math.max(4, largo / (punto * 2.6));
        g.setColor(color);
        for (int i = 1; i < pasos; i++) {
            double t = (double) i / pasos;
            double px = (1 - t) * (1 - t) * curva.x1 + 2 * (1 - t) * t * curva.ctrlx + t * t * curva.x2;
            double py = (1 - t) * (1 - t) * curva.y1 + 2 * (1 - t) * t * curva.ctrly + t * t * curva.y2;
            g.fillRect((int) Math.round(px - punto / 2.0), (int) Math.round(py - punto / 2.0), punto, punto);
        }
    }

    // Curva suave entre dos puntos (la curvatura depende de las coordenadas, así es siempre la misma)
    public static QuadCurve2D.Double curva(double x1, double y1, double x2, double y2) {
        double desvio = ((int) (x1 * 7 + y2 * 13) % 31) - 15;
        double mx = (x1 + x2) / 2;
        double my = (y1 + y2) / 2 + desvio;
        return new QuadCurve2D.Double(x1, y1, mx, my, x2, y2);
    }

    // Texto o sprite con una sombra suave por debajo (para que los íconos "floten" sobre el mapa)
    public static void dibujarConSombra(Graphics2D g, BufferedImage imagen, double cx, double cy) {
        int x = (int) Math.round(cx - imagen.getWidth() / 2.0);
        int y = (int) Math.round(cy - imagen.getHeight() / 2.0);
        java.awt.Composite anterior = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(Paleta.NEGRO);
        g.fillOval(x + 4, y + imagen.getHeight() - 10, imagen.getWidth() - 8, 14);
        g.setComposite(anterior);
        g.drawImage(imagen, x, y, null);
    }

    // Oscurece los bordes de la imagen (efecto "viñeta") con capas transparentes
    private static void sombrearBordes(Graphics2D g, int ancho, int alto) {
        for (int i = 0; i < 36; i++) {
            int alfa = (36 - i) * 3;
            g.setColor(new Color(0, 0, 0, Math.min(255, alfa)));
            g.fillRect(0, i * 2, ancho, 2);
            g.fillRect(0, alto - (i + 1) * 2, ancho, 2);
            g.fillRect(i * 3, 0, 3, alto);
            g.fillRect(ancho - (i + 1) * 3, 0, 3, alto);
        }
    }
}

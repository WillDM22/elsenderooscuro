package juego.ventana;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import juego.graficos.ImagenesUi;
import juego.opciones.Opciones;
import motor.Entrada;
import motor.Escena;
import motor.Motor;

// Ventana propia del juego. Hace lo mismo que la Ventana del kit (reloj + Motor + dibujo), pero además:
//   - tiene tres modos de pantalla (Opciones): Normal, Grande y Pantalla completa,
//   - el juego se dibuja siempre en 1024 x 640 y se agranda con el MISMO factor en ancho y alto: nunca se estira;
//     si sobra lugar (pantalla completa en un monitor que no es 16:10) quedan franjas negras y el juego va centrado,
//   - una ventana nunca es más grande que la pantalla (se achica el factor hasta que entre),
//   - aplica el brillo (una capa oscura o clara por encima del juego),
//   - convierte las coordenadas del mouse al tamaño lógico usando el tamaño REAL del panel en ese momento.
// El Motor, las Escenas y la Entrada son los del kit; solo se reemplaza la ventana, porque la del kit tiene tamaño fijo.
public class VentanaJuego {

    // Constantes
    public static final int ANCHO_LOGICO = 1024;
    public static final int ALTO_LOGICO = 640;
    private static final int MAXIMO_PASOS = 12;

    // Atributos
    private final Motor motor;
    private final Opciones opciones;
    private final Escalado escalado = new Escalado(ANCHO_LOGICO, ALTO_LOGICO);
    private final JFrame marco;
    private final JPanel panel;
    private final Timer reloj;
    private final ImagenesUi ui = new ImagenesUi();
    private Cursor punteroPropio;
    private int resolucionAplicada;
    private long ultimo;
    private double acumulado;

    // Constructor
    public VentanaJuego(String titulo, Opciones opciones) {
        this.opciones = opciones;
        this.motor = new Motor(ANCHO_LOGICO, ALTO_LOGICO);
        this.marco = new JFrame(titulo);
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarCuadro(g);
            }
        };
        panel.setFocusable(true);
        panel.setBackground(Color.BLACK);
        panel.addKeyListener(motor.getEntrada());
        conectarMouse();

        marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marco.add(panel);
        configurarIcono();
        configurarPuntero();

        reloj = new Timer(8, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
    }

    // Ícono de la ventana y de la barra de tareas: el logo (src/ui/ico.png) sin su margen transparente
    private void configurarIcono() {
        BufferedImage logo = ui.recortada(ImagenesUi.ICONO);
        if (logo == null) {
            return;
        }
        BufferedImage icono = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icono.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(logo, 0, 0, 128, 128, null);
        g.dispose();
        marco.setIconImage(icono);
        // En algunos sistemas (macOS) el ícono de la barra de tareas se pone aparte; si no se puede, no pasa nada
        try {
            if (java.awt.Taskbar.isTaskbarSupported()
                    && java.awt.Taskbar.getTaskbar().isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                java.awt.Taskbar.getTaskbar().setIconImage(icono);
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            // Sin ícono de barra de tareas: se sigue con el de la ventana
        }
    }

    // Puntero del mouse propio (src/ui/Puntero). Los sistemas solo admiten punteros de cierto tamaño (en Windows
    // suele ser 32 x 32): si la imagen es más grande se achica manteniendo la proporción. Si algo falla, el juego
    // sigue con el puntero normal del sistema.
    private void configurarPuntero() {
        try {
            BufferedImage original = ui.obtener(ImagenesUi.PUNTERO);
            if (original == null) {
                return;
            }
            Toolkit kit = Toolkit.getDefaultToolkit();
            Dimension maximo = kit.getBestCursorSize(original.getWidth(), original.getHeight());
            if (maximo.width <= 0 || maximo.height <= 0) {
                System.err.println("ADVERTENCIA: el sistema no admite punteros personalizados. Se usa el puntero por defecto.");
                return;
            }
            double factor = Math.min(1.0, Math.min((double) maximo.width / original.getWidth(),
                    (double) maximo.height / original.getHeight()));
            int ancho = Math.max(1, (int) Math.round(original.getWidth() * factor));
            int alto = Math.max(1, (int) Math.round(original.getHeight() * factor));

            // Lienzo transparente del tamaño exacto que pide el sistema, con la imagen arriba a la izquierda
            BufferedImage lienzo = new BufferedImage(maximo.width, maximo.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = lienzo.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();

            // El punto activo (el que "hace click") es la esquina superior izquierda
            Image imagen = lienzo;
            punteroPropio = kit.createCustomCursor(imagen, new Point(0, 0), "punteroJuego");
            panel.setCursor(punteroPropio);
        } catch (Exception e) {
            System.err.println("No se pudo preparar el puntero personalizado: " + e.getMessage());
        }
    }

    // Apertura desde main
    public static void abrir(String titulo, Opciones opciones, Escena inicial) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VentanaJuego ventana = new VentanaJuego(titulo, opciones);
                ventana.iniciar(inicial);
            }
        });
    }

    // Arranque
    public void iniciar(Escena inicial) {
        motor.iniciar(inicial);
        aplicarResolucion();
        ultimo = System.nanoTime();
        reloj.start();
    }

    // El mouse llega en píxeles de la ventana: se pasa a coordenadas del juego antes de dárselo a la Entrada
    private void conectarMouse() {
        final Entrada entrada = motor.getEntrada();
        MouseAdapter adaptador = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    entrada.moverMouse(escalado.aLogicoX(e.getX()), escalado.aLogicoY(e.getY()));
                    entrada.apretarMouse();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    entrada.soltarMouse();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Swing solo avisa "mouseMoved" cuando NINGÚN botón está apretado. Si la Entrada cree que el botón sigue
                // apretado (se perdió el aviso de "soltado", por ejemplo al reabrir la ventana), acá se corrige.
                entrada.soltarMouse();
                entrada.moverMouse(escalado.aLogicoX(e.getX()), escalado.aLogicoY(e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                entrada.moverMouse(escalado.aLogicoX(e.getX()), escalado.aLogicoY(e.getY()));
            }
        };
        panel.addMouseListener(adaptador);
        panel.addMouseMotionListener(adaptador);
    }

    // Deja la ventana en el modo elegido (Normal, Grande o Pantalla completa). Se usa al abrir y cada vez que cambia la opción.
    // Para cambiar entre ventana con bordes y pantalla completa (sin bordes) Swing obliga a cerrar la ventana (dispose)
    // y volver a mostrarla; el panel y el Motor siguen siendo los mismos, así que el juego no se reinicia.
    private void aplicarResolucion() {
        resolucionAplicada = opciones.getResolucion();
        if (marco.isDisplayable()) {
            marco.dispose();
        }
        boolean completa = opciones.esPantallaCompleta();
        marco.setUndecorated(completa);
        marco.setResizable(false);
        if (completa) {
            mostrarEnPantallaCompleta();
        } else {
            mostrarEnVentana();
        }
        marco.toFront();
        panel.requestFocusInWindow();
        // Al cerrar y reabrir la ventana se pierde el aviso de "botón soltado": si no se limpia, la Entrada
        // queda creyendo que el botón sigue apretado y no registra ningún click más.
        motor.getEntrada().soltarMouse();
    }

    // Modos con ventana: tamaño deseado (1,0 o 1,25 veces el juego), pero nunca más grande que el espacio útil de la pantalla
    private void mostrarEnVentana() {
        double deseado = opciones.getFactorVentana();
        fijarTamanioPanel(deseado);
        marco.pack();
        Insets bordes = marco.getInsets();
        Rectangle util = areaUtil();
        double factor = escalado.limitarFactor(util.width - bordes.left - bordes.right,
                util.height - bordes.top - bordes.bottom, deseado);
        if (factor < deseado) {
            fijarTamanioPanel(factor);
            marco.pack();
        }
        marco.setLocation(util.x + (util.width - marco.getWidth()) / 2, util.y + (util.height - marco.getHeight()) / 2);
        marco.setVisible(true);
    }

    private void fijarTamanioPanel(double factor) {
        int ancho = (int) Math.round(ANCHO_LOGICO * factor);
        int alto = (int) Math.round(ALTO_LOGICO * factor);
        panel.setPreferredSize(new Dimension(ancho, alto));
    }

    // Pantalla completa "sin bordes": una ventana sin título ni bordes del tamaño exacto del monitor. No se usa el modo
    // exclusivo de Java (setFullScreenWindow) porque en Windows, con zoom de pantalla o varios monitores, puede dejar
    // el mouse o el foco mal. Del dibujo se encarga Escalado (franjas negras, sin estirar).
    private void mostrarEnPantallaCompleta() {
        Rectangle pantalla = dispositivo().getDefaultConfiguration().getBounds();
        panel.setPreferredSize(new Dimension(pantalla.width, pantalla.height));
        marco.pack();
        marco.setBounds(pantalla);
        marco.setVisible(true);
    }

    // Pantalla donde está (o va a estar) la ventana
    private GraphicsDevice dispositivo() {
        GraphicsConfiguration configuracion = marco.getGraphicsConfiguration();
        if (configuracion != null) {
            return configuracion.getDevice();
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    // Parte de la pantalla que no ocupan la barra de tareas ni otras barras del sistema
    private Rectangle areaUtil() {
        GraphicsConfiguration configuracion = dispositivo().getDefaultConfiguration();
        Rectangle pantalla = configuracion.getBounds();
        Insets barras = Toolkit.getDefaultToolkit().getScreenInsets(configuracion);
        return new Rectangle(pantalla.x + barras.left, pantalla.y + barras.top,
                pantalla.width - barras.left - barras.right, pantalla.height - barras.top - barras.bottom);
    }

    // Toma el tamaño REAL del panel en este momento (la ventana puede no medir exactamente lo pedido)
    private void sincronizarTamanio() {
        if (panel.getWidth() > 0 && panel.getHeight() > 0) {
            escalado.setTamanioReal(panel.getWidth(), panel.getHeight());
        }
    }

    // Dibuja el juego agrandado y centrado, con franjas negras si sobra lugar, y encima la capa del brillo
    private void dibujarCuadro(Graphics g) {
        sincronizarTamanio();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.translate(escalado.getDesplazamientoX(), escalado.getDesplazamientoY());
        g2.scale(escalado.getFactor(), escalado.getFactor());
        g2.clipRect(0, 0, ANCHO_LOGICO, ALTO_LOGICO);
        motor.dibujar(g2);
        g2.dispose();
        dibujarBrillo(g);
    }

    // Brillo menor a 100: capa negra translúcida. Mayor a 100: capa blanca muy suave. Solo sobre el juego, no sobre las franjas.
    private void dibujarBrillo(Graphics g) {
        int porcentaje = opciones.getBrilloPorcentaje();
        if (porcentaje == 100) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        if (porcentaje < 100) {
            g2.setColor(Color.BLACK);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100 - porcentaje) / 100f * 1.4f));
        } else {
            g2.setColor(Color.WHITE);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (porcentaje - 100) / 100f * 0.8f));
        }
        g2.fillRect(escalado.getDesplazamientoX(), escalado.getDesplazamientoY(),
                escalado.getAnchoDibujado(), escalado.getAltoDibujado());
        g2.dispose();
    }

    // Cuadro
    private void tick() {
        if (opciones.getResolucion() != resolucionAplicada) {
            aplicarResolucion();
        }
        sincronizarTamanio();
        long ahora = System.nanoTime();
        double delta = (ahora - ultimo) / 1e9;
        ultimo = ahora;
        acumulado += Math.min(delta, 0.25);

        int pasos = 0;
        while (acumulado >= Motor.PASO && pasos < MAXIMO_PASOS) {
            motor.paso();
            acumulado -= Motor.PASO;
            pasos++;
        }
        if (pasos == MAXIMO_PASOS) {
            acumulado = 0;
        }

        if (motor.estaTerminado()) {
            reloj.stop();
            marco.dispose();
            System.exit(0);
        }
        panel.repaint();
    }
}

package juego.ventana;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import juego.opciones.Opciones;
import motor.Entrada;
import motor.Escena;
import motor.Motor;

// Ventana propia del juego. Hace lo mismo que la Ventana del kit (reloj + Motor + dibujo), pero además:
//   - cambia de tamaño cuando cambia la resolución en Opciones (el juego se dibuja en 1024 x 640 y se agranda),
//   - aplica el brillo (una capa oscura o clara por encima de todo),
//   - convierte las coordenadas del mouse al tamaño lógico.
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
        panel.addKeyListener(motor.getEntrada());
        conectarMouse();

        marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marco.setResizable(false);
        marco.add(panel);
        aplicarResolucion();

        reloj = new Timer(8, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
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
        marco.setVisible(true);
        panel.requestFocusInWindow();
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

    // Pone la ventana al tamaño de la resolución elegida (se hace sobre la misma ventana: no se vuelve a crear)
    private void aplicarResolucion() {
        resolucionAplicada = opciones.getResolucion();
        panel.setPreferredSize(new Dimension(opciones.getAncho(), opciones.getAlto()));
        escalado.setTamanioReal(opciones.getAncho(), opciones.getAlto());
        marco.pack();
        marco.setLocationRelativeTo(null);
    }

    // Dibuja el juego agrandado y encima la capa del brillo
    private void dibujarCuadro(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int anchoPantalla = panel.getWidth();
        int altoPantalla = panel.getHeight();
        g2.scale(anchoPantalla / (double) ANCHO_LOGICO, altoPantalla / (double) ALTO_LOGICO);
        motor.dibujar(g2);
        g2.dispose();
        dibujarBrillo(g, anchoPantalla, altoPantalla);
    }

    // Brillo menor a 100: capa negra translúcida. Mayor a 100: capa blanca muy suave.
    private void dibujarBrillo(Graphics g, int ancho, int alto) {
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
        g2.fillRect(0, 0, ancho, alto);
        g2.dispose();
    }

    // Cuadro
    private void tick() {
        if (opciones.getResolucion() != resolucionAplicada) {
            aplicarResolucion();
            panel.requestFocusInWindow();
        }
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

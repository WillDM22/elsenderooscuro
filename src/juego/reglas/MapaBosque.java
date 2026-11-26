package juego.reglas;

import estructuras.Grafo;
import estructuras.ListaEnlazada;
import java.util.Random;

// Mapa del bosque: un GRAFO DIRIGIDO de nodos organizados en "pisos" (columnas) de izquierda a derecha.
//   piso 0: inicio | pisos 1 a 5: 2 o 3 lugares cada uno | piso 6: jefe final
// El mapa se genera a partir de una semilla: la misma semilla produce siempre el mismo mapa,
// por eso al guardar la partida alcanza con guardar la semilla.
public class MapaBosque {

    // Constantes
    public static final int ANCHO = 1024;
    public static final int ALTO = 640;
    public static final int PISOS = 7;
    private static final int MARGEN_X = 90;
    private static final int SEPARACION_X = 140;
    private static final int ARRIBA = 172;
    private static final int ABAJO = 492;

    // Atributos
    private final long semilla;
    private final Grafo<NodoMapa> grafo = new Grafo<>();
    private int idInicio;
    private int idJefe;

    // Constructor: genera el mapa completo
    public MapaBosque(long semilla) {
        this.semilla = semilla;
        generar();
    }

    // Generación
    private void generar() {
        Random azar = new Random(semilla);
        idInicio = grafo.agregarVertice(new NodoMapa(0, TipoNodo.INICIO, 0, MARGEN_X, (ARRIBA + ABAJO) / 2));
        ListaEnlazada<Integer> pisoAnterior = new ListaEnlazada<>();
        pisoAnterior.agregar(idInicio);

        for (int piso = 1; piso < PISOS - 1; piso++) {
            int cantidad = 2 + azar.nextInt(2);
            ListaEnlazada<Integer> pisoActual = new ListaEnlazada<>();
            for (int k = 0; k < cantidad; k++) {
                TipoNodo tipo = elegirTipo(azar, piso, k);
                int x = MARGEN_X + piso * SEPARACION_X;
                int franja = (ABAJO - ARRIBA) / cantidad;
                int y = ARRIBA + franja * k + franja / 2 + azar.nextInt(31) - 15;
                int id = grafo.cantidadVertices();
                pisoActual.agregar(grafo.agregarVertice(new NodoMapa(id, tipo, piso, x, y)));
            }
            conectarPisos(pisoAnterior, pisoActual, azar);
            pisoAnterior = pisoActual;
        }

        int idUltimo = grafo.cantidadVertices();
        NodoMapa jefe = new NodoMapa(idUltimo, TipoNodo.JEFE, PISOS - 1,
                MARGEN_X + (PISOS - 1) * SEPARACION_X, (ARRIBA + ABAJO) / 2);
        idJefe = grafo.agregarVertice(jefe);
        ListaEnlazada<Integer> pisoJefe = new ListaEnlazada<>();
        pisoJefe.agregar(idJefe);
        conectarPisos(pisoAnterior, pisoJefe, azar);
    }

    // 45% combate, 30% evento, 25% cofre. En los pisos 2 y 4 el primer lugar siempre es un cofre.
    private TipoNodo elegirTipo(Random azar, int piso, int k) {
        if (k == 0 && (piso == 2 || piso == 4)) {
            return TipoNodo.COFRE;
        }
        int tirada = azar.nextInt(100);
        if (tirada < 45) {
            return TipoNodo.COMBATE;
        }
        if (tirada < 75) {
            return TipoNodo.EVENTO;
        }
        return TipoNodo.COFRE;
    }

    // Conecta un piso con el siguiente de modo que ningún lugar quede sin salida ni sin entrada
    private void conectarPisos(ListaEnlazada<Integer> anterior, ListaEnlazada<Integer> siguiente, Random azar) {
        int n = anterior.tamanio();
        int m = siguiente.tamanio();
        // 1) cada lugar del piso anterior se conecta con el lugar "proporcional" del siguiente
        for (int i = 0; i < n; i++) {
            int j = indiceProporcional(i, n, m);
            grafo.agregarArista(anterior.obtener(i), siguiente.obtener(j));
            // 2) a veces, un segundo camino a un vecino
            if (m > 1 && azar.nextInt(100) < 40) {
                int vecino = j + 1;
                if (vecino >= m) {
                    vecino = j - 1;
                }
                grafo.agregarArista(anterior.obtener(i), siguiente.obtener(vecino));
            }
        }
        // 3) si algún lugar del siguiente piso quedó sin entrada, se lo conecta con el más cercano
        for (int j = 0; j < m; j++) {
            if (!tieneEntrada(anterior, siguiente.obtener(j))) {
                int i = indiceProporcional(j, m, n);
                grafo.agregarArista(anterior.obtener(i), siguiente.obtener(j));
            }
        }
    }

    // Posición i de n (0..n-1) llevada a una posición de 0..m-1
    private int indiceProporcional(int i, int n, int m) {
        if (n == 1 || m == 1) {
            return m == 1 ? 0 : (m - 1) / 2;
        }
        double ubicacion = (double) i / (n - 1);
        return (int) Math.round(ubicacion * (m - 1));
    }

    private boolean tieneEntrada(ListaEnlazada<Integer> anterior, int destino) {
        for (int i = 0; i < anterior.tamanio(); i++) {
            if (grafo.existeArista(anterior.obtener(i), destino)) {
                return true;
            }
        }
        return false;
    }

    // Consultas
    public Grafo<NodoMapa> getGrafo() {
        return grafo;
    }

    public NodoMapa getNodo(int id) {
        return grafo.obtener(id);
    }

    public int getIdInicio() {
        return idInicio;
    }

    public int getIdJefe() {
        return idJefe;
    }

    public long getSemilla() {
        return semilla;
    }

    public int cantidadNodos() {
        return grafo.cantidadVertices();
    }
}

package juego.archivos;

import estructuras.ListaEnlazada;
import estructuras.PilaEnlazada;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import juego.entidades.CatalogoHabilidades;
import juego.entidades.Jugador;
import juego.reglas.EfectoEvento;
import juego.reglas.EventoNarrativo;
import juego.reglas.MapaBosque;
import juego.reglas.OpcionEvento;
import juego.reglas.Partida;
import juego.reglas.Registro;
import utilidades.Tokenizador;

// Persistencia en archivos de texto plano (java.io):
//   guardado.txt -> la partida (se escribe con PrintWriter y se lee con BufferedReader)
//   eventos.txt  -> los eventos narrativos del juego (se leen con BufferedReader)
// Si un archivo falta o está dañado el juego NO se rompe: cargar() devuelve null y los eventos usan un respaldo.
public class GestorArchivos {

    // Constantes
    private static final String VERSION = "1";
    private static final String NOMBRE_GUARDADO = "guardado.txt";
    private static final String NOMBRE_EVENTOS = "eventos.txt";

    // Atributos
    private final String carpeta;

    // Constructor
    public GestorArchivos(String carpeta) {
        if (carpeta == null || carpeta.isEmpty()) {
            throw new IllegalArgumentException("la carpeta no puede estar vacía");
        }
        this.carpeta = carpeta;
    }

    // Rutas
    public String rutaGuardado() {
        return carpeta + File.separator + NOMBRE_GUARDADO;
    }

    public String rutaEventos() {
        return carpeta + File.separator + NOMBRE_EVENTOS;
    }

    // Guardado
    public boolean existeGuardado() {
        return new File(rutaGuardado()).isFile();
    }

    public boolean borrarGuardado() {
        File archivo = new File(rutaGuardado());
        return archivo.isFile() && archivo.delete();
    }

    // Escribe la partida. Devuelve false si no se pudo (por ejemplo, disco lleno o carpeta sin permiso).
    public boolean guardar(Partida partida) {
        new File(carpeta).mkdirs();
        try (PrintWriter salida = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaGuardado()), "UTF-8"))) {
            Jugador j = partida.getJugador();
            salida.println("VERSION=" + VERSION);
            salida.println("SEMILLA=" + partida.getSemilla());
            salida.println("FUERZA=" + j.getFuerza());
            salida.println("DESTREZA=" + j.getDestreza());
            salida.println("INTELIGENCIA=" + j.getInteligencia());
            salida.println("VIDA=" + j.getVida());
            salida.println("MANA=" + j.getMana());
            salida.println("ORO=" + j.getOro());
            salida.println("DERROTADOS=" + partida.getDerrotados());
            salida.println("NODO_ACTUAL=" + partida.getNodoActual());
            salida.println("HABILIDADES=" + textoHabilidades(j));
            salida.println("RUTA=" + textoRuta(partida.getRuta()));
            ListaEnlazada<Registro> diario = partida.getDiarioCompleto();
            for (int i = 0; i < diario.tamanio(); i++) {
                Registro r = diario.obtener(i);
                salida.println("REGISTRO=" + r.getIdNodo() + "|" + limpiar(r.getTexto()));
            }
            return !salida.checkError();
        } catch (IOException e) {
            return false;
        }
    }

    // Lee la partida. Devuelve null si no existe o si el archivo está dañado o fue modificado a mano.
    public Partida cargar(ListaEnlazada<EventoNarrativo> eventos) {
        ListaEnlazada<String> lineas = leerLineas(rutaGuardado());
        if (lineas == null) {
            return null;
        }
        try {
            if (!VERSION.equals(valorDe(lineas, "VERSION"))) {
                return null;
            }
            long semilla = Long.parseLong(valorDe(lineas, "SEMILLA"));
            int fuerza = Integer.parseInt(valorDe(lineas, "FUERZA"));
            int destreza = Integer.parseInt(valorDe(lineas, "DESTREZA"));
            int inteligencia = Integer.parseInt(valorDe(lineas, "INTELIGENCIA"));
            int vida = Integer.parseInt(valorDe(lineas, "VIDA"));
            int mana = Integer.parseInt(valorDe(lineas, "MANA"));
            int oro = Integer.parseInt(valorDe(lineas, "ORO"));
            int derrotados = Integer.parseInt(valorDe(lineas, "DERROTADOS"));
            int nodoActual = Integer.parseInt(valorDe(lineas, "NODO_ACTUAL"));
            if (vida <= 0) {
                return null;
            }

            Jugador jugador = new Jugador(fuerza, destreza, inteligencia, vida, mana, oro);
            if (!cargarHabilidades(jugador, valorDe(lineas, "HABILIDADES"))) {
                return null;
            }
            PilaEnlazada<Integer> ruta = leerRuta(valorDe(lineas, "RUTA"), semilla);
            if (ruta == null || ruta.tope() != nodoActual) {
                return null;
            }
            PilaEnlazada<Registro> diario = leerDiario(lineas);
            return new Partida(semilla, jugador, ruta, diario, derrotados, eventos);
        } catch (RuntimeException e) {
            // NumberFormatException (texto en lugar de número), IllegalArgumentException (valores imposibles),
            // NullPointerException (falta una clave)... todo significa "archivo dañado".
            return null;
        }
    }

    // Eventos
    // Lee eventos.txt. Si no existe o no tiene ningún evento válido, usa unos eventos de respaldo.
    public ListaEnlazada<EventoNarrativo> cargarEventos() {
        ListaEnlazada<String> lineas = leerLineas(rutaEventos());
        ListaEnlazada<EventoNarrativo> eventos = new ListaEnlazada<>();
        if (lineas != null) {
            eventos = interpretarEventos(lineas);
        }
        if (eventos.estaVacia()) {
            eventos = interpretarEventos(textoDeRespaldo());
        }
        return eventos;
    }

    // Convierte las líneas del archivo en eventos.
    //   EVENTO|titulo|relato
    //   OPCION|texto del botón|EFECTO:valor;EFECTO:valor|texto del resultado
    // Los eventos mal escritos se descartan (no rompen la carga de los demás).
    public ListaEnlazada<EventoNarrativo> interpretarEventos(ListaEnlazada<String> lineas) {
        ListaEnlazada<EventoNarrativo> resultado = new ListaEnlazada<>();
        String titulo = null;
        String relato = "";
        ListaEnlazada<OpcionEvento> opciones = new ListaEnlazada<>();
        boolean valido = true;

        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            Tokenizador t = new Tokenizador(linea, '|');
            String tipo = t.siguiente();
            if (tipo.equals("EVENTO")) {
                cerrarEvento(resultado, titulo, relato, opciones, valido);
                titulo = t.hayMas() ? t.siguiente() : "";
                relato = t.hayMas() ? t.siguiente() : "";
                opciones = new ListaEnlazada<>();
                valido = true;
            } else if (tipo.equals("OPCION") && titulo != null) {
                try {
                    String texto = t.siguiente();
                    String efectos = t.hayMas() ? t.siguiente() : "";
                    String textoResultado = t.hayMas() ? t.siguiente() : "";
                    opciones.agregar(new OpcionEvento(texto, textoResultado, interpretarEfectos(efectos)));
                } catch (RuntimeException e) {
                    valido = false;
                }
            }
        }
        cerrarEvento(resultado, titulo, relato, opciones, valido);
        return resultado;
    }

    // Agrega el evento a la lista si está completo y es válido
    private void cerrarEvento(ListaEnlazada<EventoNarrativo> destino, String titulo, String relato,
                              ListaEnlazada<OpcionEvento> opciones, boolean valido) {
        if (titulo == null || !valido) {
            return;
        }
        try {
            destino.agregar(new EventoNarrativo(titulo, relato, opciones));
        } catch (IllegalArgumentException e) {
            // evento con título vacío o con menos de 2 / más de 3 opciones: se descarta
        }
    }

    // "VIDA:-3;ORO:10" -> lista de efectos
    public ListaEnlazada<EfectoEvento> interpretarEfectos(String texto) {
        ListaEnlazada<EfectoEvento> efectos = new ListaEnlazada<>();
        Tokenizador porEfecto = new Tokenizador(texto, ';');
        while (porEfecto.hayMas()) {
            String par = porEfecto.siguiente().trim();
            if (par.isEmpty()) {
                continue;
            }
            Tokenizador partes = new Tokenizador(par, ':');
            EfectoEvento.Tipo tipo = EfectoEvento.Tipo.valueOf(partes.siguiente().trim());
            String dato = partes.siguiente().trim();
            if (tipo == EfectoEvento.Tipo.HABILIDAD || tipo == EfectoEvento.Tipo.COMBATE) {
                efectos.agregar(new EfectoEvento(tipo, 0, dato));
            } else {
                efectos.agregar(new EfectoEvento(tipo, Integer.parseInt(dato), null));
            }
        }
        return efectos;
    }

    // Lectura de archivos
    // Devuelve todas las líneas del archivo, o null si no se puede leer
    public ListaEnlazada<String> leerLineas(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.isFile()) {
            return null;
        }
        ListaEnlazada<String> lineas = new ListaEnlazada<>();
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            String linea = entrada.readLine();
            while (linea != null) {
                lineas.agregar(linea);
                linea = entrada.readLine();
            }
            return lineas;
        } catch (IOException e) {
            return null;
        }
    }

    // Auxiliares de la carga de partidas
    // Valor de la primera línea "CLAVE=valor"; null si no existe
    private String valorDe(ListaEnlazada<String> lineas, String clave) {
        String prefijo = clave + "=";
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i);
            if (linea.startsWith(prefijo)) {
                return linea.substring(prefijo.length()).trim();
            }
        }
        return null;
    }

    private boolean cargarHabilidades(Jugador jugador, String texto) {
        Tokenizador t = new Tokenizador(texto, ',');
        while (t.hayMas()) {
            String codigo = t.siguiente().trim();
            if (!CatalogoHabilidades.esValido(codigo)) {
                return false;
            }
            if (!jugador.aprenderHabilidad(CatalogoHabilidades.crear(codigo))) {
                return false;
            }
        }
        // Todo héroe sabe atacar: un guardado sin ATAQUE fue modificado a mano
        return jugador.conoceHabilidad("ATAQUE");
    }

    // Arma la pila de la ruta y comprueba que sea un camino posible del grafo (empezando en el inicio)
    private PilaEnlazada<Integer> leerRuta(String texto, long semilla) {
        MapaBosque mapa = new MapaBosque(semilla);
        PilaEnlazada<Integer> ruta = new PilaEnlazada<>();
        Tokenizador t = new Tokenizador(texto, ',');
        int anterior = -1;
        while (t.hayMas()) {
            int id = Integer.parseInt(t.siguiente().trim());
            if (anterior == -1) {
                if (id != mapa.getIdInicio()) {
                    return null;
                }
            } else if (!mapa.getGrafo().existeArista(anterior, id)) {
                return null;
            }
            ruta.apilar(id);
            anterior = id;
        }
        if (ruta.estaVacia()) {
            return null;
        }
        return ruta;
    }

    private PilaEnlazada<Registro> leerDiario(ListaEnlazada<String> lineas) {
        PilaEnlazada<Registro> diario = new PilaEnlazada<>();
        String prefijo = "REGISTRO=";
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i);
            if (linea.startsWith(prefijo)) {
                String resto = linea.substring(prefijo.length());
                int barra = resto.indexOf('|');
                if (barra > 0) {
                    int id = Integer.parseInt(resto.substring(0, barra).trim());
                    diario.apilar(new Registro(id, resto.substring(barra + 1)));
                }
            }
        }
        return diario;
    }

    // Auxiliares de la escritura
    private String textoHabilidades(Jugador jugador) {
        String texto = "";
        for (int i = 0; i < jugador.cantidadHabilidades(); i++) {
            if (i > 0) {
                texto = texto + ",";
            }
            texto = texto + jugador.getHabilidad(i).getCodigo();
        }
        return texto;
    }

    private String textoRuta(ListaEnlazada<Integer> ruta) {
        String texto = "";
        for (int i = 0; i < ruta.tamanio(); i++) {
            if (i > 0) {
                texto = texto + ",";
            }
            texto = texto + ruta.obtener(i);
        }
        return texto;
    }

    // Un texto con salto de línea rompería el formato de una línea por dato
    private String limpiar(String texto) {
        return texto.replace('\n', ' ').replace('\r', ' ');
    }

    // Eventos mínimos por si falta eventos.txt
    private ListaEnlazada<String> textoDeRespaldo() {
        ListaEnlazada<String> lineas = new ListaEnlazada<>();
        lineas.agregar("EVENTO|Un altar de piedra|Un altar cubierto de musgo ofrece una vasija con agua clara.");
        lineas.agregar("OPCION|Beber el agua|VIDA:8;MANA:3|El agua es fresca y te reanima.");
        lineas.agregar("OPCION|Dejar una moneda|ORO:-5;FUERZA:1|Sientes que el bosque te agradece.");
        lineas.agregar("OPCION|Seguir de largo||No pasa nada.");
        lineas.agregar("EVENTO|Un viajero herido|Un viajero te pide ayuda junto al camino.");
        lineas.agregar("OPCION|Vendar sus heridas|VIDA:-2;ORO:15|Te lo agradece con unas monedas.");
        lineas.agregar("OPCION|Ignorarlo||Sigues caminando.");
        return lineas;
    }
}

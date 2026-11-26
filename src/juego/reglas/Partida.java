package juego.reglas;

import estructuras.ListaEnlazada;
import estructuras.PilaEnlazada;
import java.util.Random;
import juego.entidades.Clase;
import juego.entidades.Enemigo;
import juego.entidades.FabricaEnemigos;
import juego.entidades.Jugador;

// Todo lo que define una aventura en curso: el mapa (grafo), el héroe, y dos PILAS:
//   ruta   -> ids de los nodos visitados (el tope es el lugar donde está el héroe)
//   diario -> registro de lo que fue pasando (el tope es lo más reciente)
// Con la semilla se vuelve a generar el mapa, así que al guardar no hace falta guardar el grafo.
public class Partida {

    // Atributos
    private final long semilla;
    private final MapaBosque mapa;
    private final Jugador jugador;
    private final PilaEnlazada<Integer> ruta;
    private final PilaEnlazada<Registro> diario;
    private final ListaEnlazada<EventoNarrativo> eventos;
    private int derrotados;

    // Constructor completo (lo usa también el GestorArchivos al cargar)
    public Partida(long semilla, Jugador jugador, PilaEnlazada<Integer> ruta, PilaEnlazada<Registro> diario,
                   int derrotados, ListaEnlazada<EventoNarrativo> eventos) {
        if (jugador == null || ruta == null || diario == null || eventos == null) {
            throw new IllegalArgumentException("faltan datos para crear la partida");
        }
        if (ruta.estaVacia()) {
            throw new IllegalArgumentException("la ruta debe tener al menos el nodo de inicio");
        }
        if (derrotados < 0) {
            throw new IllegalArgumentException("derrotados no puede ser negativo: " + derrotados);
        }
        this.semilla = semilla;
        this.mapa = new MapaBosque(semilla);
        this.jugador = jugador;
        this.ruta = ruta;
        this.diario = diario;
        this.derrotados = derrotados;
        this.eventos = eventos;
        marcarVisitados();
    }

    // Partida nueva: héroe nuevo, parado en el inicio
    public static Partida nueva(long semilla, ListaEnlazada<EventoNarrativo> eventos, Clase clase) {
        MapaBosque mapaTemporal = new MapaBosque(semilla);
        PilaEnlazada<Integer> ruta = new PilaEnlazada<>();
        ruta.apilar(mapaTemporal.getIdInicio());
        PilaEnlazada<Registro> diario = new PilaEnlazada<>();
        diario.apilar(new Registro(mapaTemporal.getIdInicio(), "Entras a Solis Umbra como " + clase.getNombre() + "."));
        return new Partida(semilla, new Jugador(clase), ruta, diario, 0, eventos);
    }

    // Marca en el mapa los nodos que están en la ruta.
    // Para recorrer una pila sin perder datos se pasa todo a una pila auxiliar y se devuelve.
    private void marcarVisitados() {
        PilaEnlazada<Integer> auxiliar = new PilaEnlazada<>();
        while (!ruta.estaVacia()) {
            int id = ruta.desapilar();
            mapa.getNodo(id).setVisitado(true);
            auxiliar.apilar(id);
        }
        while (!auxiliar.estaVacia()) {
            ruta.apilar(auxiliar.desapilar());
        }
    }

    // Movimiento por el grafo
    public int getNodoActual() {
        return ruta.tope();
    }

    public NodoMapa getNodoActualCompleto() {
        return mapa.getNodo(getNodoActual());
    }

    // A dónde se puede ir desde el lugar actual
    public ListaEnlazada<Integer> getAdyacentes() {
        return mapa.getGrafo().idsAdyacentes(getNodoActual());
    }

    // Los mismos destinos, ordenados de arriba hacia abajo según su posición en pantalla.
    // Así la flecha "arriba" del teclado elige el lugar de más arriba y "abajo" el de más abajo.
    // Es un ordenamiento por inserción: cada id se coloca antes del primero que está más abajo.
    public ListaEnlazada<Integer> getAdyacentesOrdenados() {
        ListaEnlazada<Integer> original = getAdyacentes();
        ListaEnlazada<Integer> ordenados = new ListaEnlazada<>();
        for (int i = 0; i < original.tamanio(); i++) {
            int id = original.obtener(i);
            int y = mapa.getNodo(id).getY();
            int posicion = 0;
            while (posicion < ordenados.tamanio() && mapa.getNodo(ordenados.obtener(posicion)).getY() <= y) {
                posicion++;
            }
            ordenados.insertar(posicion, id);
        }
        return ordenados;
    }

    public boolean puedeMoverA(int id) {
        if (id < 0 || id >= mapa.cantidadNodos()) {
            return false;
        }
        return mapa.getGrafo().existeArista(getNodoActual(), id);
    }

    public void moverA(int id) {
        if (!puedeMoverA(id)) {
            throw new IllegalArgumentException("no hay camino desde el nodo " + getNodoActual() + " al " + id);
        }
        NodoMapa destino = mapa.getNodo(id);
        destino.setVisitado(true);
        ruta.apilar(id);
        registrar(id, "Piso " + destino.getPiso() + ": llegas a " + destino.getTipo().nombre() + ".");
    }

    // Diario
    public void registrar(int idNodo, String texto) {
        diario.apilar(new Registro(idNodo, texto));
    }

    // Los últimos "cantidad" registros, del más reciente al más antiguo.
    // Se sacan de la pila y se devuelven a su lugar usando una pila auxiliar.
    public ListaEnlazada<Registro> getUltimosRegistros(int cantidad) {
        ListaEnlazada<Registro> resultado = new ListaEnlazada<>();
        PilaEnlazada<Registro> auxiliar = new PilaEnlazada<>();
        while (!diario.estaVacia() && resultado.tamanio() < cantidad) {
            Registro registro = diario.desapilar();
            resultado.agregar(registro);
            auxiliar.apilar(registro);
        }
        while (!auxiliar.estaVacia()) {
            diario.apilar(auxiliar.desapilar());
        }
        return resultado;
    }

    // Todo el diario, del más antiguo al más nuevo (así se escribe en el archivo)
    public ListaEnlazada<Registro> getDiarioCompleto() {
        ListaEnlazada<Registro> resultado = new ListaEnlazada<>();
        PilaEnlazada<Registro> auxiliar = new PilaEnlazada<>();
        while (!diario.estaVacia()) {
            auxiliar.apilar(diario.desapilar());
        }
        while (!auxiliar.estaVacia()) {
            Registro registro = auxiliar.desapilar();
            resultado.agregar(registro);
            diario.apilar(registro);
        }
        return resultado;
    }

    // Ids visitados, del primero al último
    public ListaEnlazada<Integer> getRuta() {
        ListaEnlazada<Integer> resultado = new ListaEnlazada<>();
        PilaEnlazada<Integer> auxiliar = new PilaEnlazada<>();
        while (!ruta.estaVacia()) {
            auxiliar.apilar(ruta.desapilar());
        }
        while (!auxiliar.estaVacia()) {
            int id = auxiliar.desapilar();
            resultado.agregar(id);
            ruta.apilar(id);
        }
        return resultado;
    }

    // Enemigos y eventos de cada nodo. Se calculan con una semilla propia del nodo:
    // el mismo nodo siempre tiene el mismo enemigo/evento (no se puede "re-tirar" cargando la partida).
    public Enemigo crearEnemigoDeNodo(NodoMapa nodo) {
        if (nodo.getTipo() == TipoNodo.JEFE) {
            return FabricaEnemigos.crearJefe();
        }
        Random azar = new Random(semilla * 1000 + nodo.getId());
        return FabricaEnemigos.crearAleatorio(nodo.getPiso(), azar);
    }

    public EventoNarrativo crearEventoDeNodo(NodoMapa nodo) {
        if (eventos.estaVacia()) {
            return crearEventoCofre();
        }
        Random azar = new Random(semilla * 7919 + nodo.getId());
        return eventos.obtener(azar.nextInt(eventos.tamanio()));
    }

    // El cofre es un evento armado en el código (no viene del archivo): su primera opción enseña la
    // próxima habilidad del catálogo; si ya se saben todas, da un punto de Inteligencia.
    public EventoNarrativo crearEventoCofre() {
        ListaEnlazada<OpcionEvento> opciones = new ListaEnlazada<>();

        ListaEnlazada<EfectoEvento> pergamino = new ListaEnlazada<>();
        String proxima = jugador.proximaHabilidadPorAprender();
        if (proxima != null) {
            pergamino.agregar(new EfectoEvento(EfectoEvento.Tipo.HABILIDAD, 0, proxima));
        } else {
            pergamino.agregar(new EfectoEvento(EfectoEvento.Tipo.INTELIGENCIA, 1, null));
        }
        opciones.agregar(new OpcionEvento("Tomar el pergamino sellado",
                "Rompes el sello y las runas se graban en tu mente.", pergamino));

        ListaEnlazada<EfectoEvento> monedas = new ListaEnlazada<>();
        monedas.agregar(new EfectoEvento(EfectoEvento.Tipo.ORO, 20, null));
        opciones.agregar(new OpcionEvento("Llevarte las monedas",
                "Las monedas tintinean en tu bolsa.", monedas));

        ListaEnlazada<EfectoEvento> pocion = new ListaEnlazada<>();
        pocion.agregar(new EfectoEvento(EfectoEvento.Tipo.VIDA, 12, null));
        pocion.agregar(new EfectoEvento(EfectoEvento.Tipo.MANA, 5, null));
        opciones.agregar(new OpcionEvento("Beber la poción azul",
                "Un calor agradable recorre tu cuerpo.", pocion));

        return new EventoNarrativo("Un cofre olvidado",
                "Entre las raíces de un roble hay un cofre de hierro entreabierto. Dentro ves tres cosas.", opciones);
    }

    // Progreso del héroe: cada 2 enemigos derrotados mejora un atributo, rotando Fuerza, Destreza e Inteligencia.
    // Devuelve el texto de la mejora, o null si todavía no toca.
    public String sumarDerrotado() {
        derrotados++;
        if (derrotados % 2 != 0) {
            return null;
        }
        int cual = (derrotados / 2 - 1) % 3;
        jugador.mejorarAtributo(cual, 1);
        if (cual == Jugador.FUERZA) {
            return "Te sientes más fuerte: +1 Fuerza.";
        }
        if (cual == Jugador.DESTREZA) {
            return "Tus reflejos mejoran: +1 Destreza.";
        }
        return "Tu mente se agudiza: +1 Inteligencia.";
    }

    // Getters
    public long getSemilla() {
        return semilla;
    }

    public MapaBosque getMapa() {
        return mapa;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public int getDerrotados() {
        return derrotados;
    }

    public ListaEnlazada<EventoNarrativo> getEventos() {
        return eventos;
    }

    public boolean estaEnElJefe() {
        return getNodoActual() == mapa.getIdJefe();
    }
}

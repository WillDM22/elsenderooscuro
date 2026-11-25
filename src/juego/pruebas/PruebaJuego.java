package juego.pruebas;

import estructuras.ColaEnlazada;
import estructuras.Grafo;
import estructuras.ListaEnlazada;
import estructuras.PilaEnlazada;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import juego.recursos.Recursos;
import juego.archivos.GestorArchivos;
import juego.entidades.Brujo;
import juego.entidades.Calidad;
import juego.entidades.CatalogoHabilidades;
import juego.entidades.Enemigo;
import juego.entidades.Esqueleto;
import juego.entidades.FabricaEnemigos;
import juego.entidades.Habilidad;
import juego.entidades.HabilidadAtaque;
import juego.entidades.HabilidadCuracion;
import juego.entidades.HabilidadEscape;
import juego.entidades.HabilidadGolpePotente;
import juego.entidades.Jugador;
import juego.entidades.Lobo;
import juego.entidades.ReyOscuro;
import juego.graficos.CatalogoSprites;
import juego.graficos.GestorSprites;
import juego.graficos.Sprite;
import juego.pantallas.EscenaCombate;
import juego.pantallas.EscenaEvento;
import juego.pantallas.EscenaFin;
import juego.pantallas.EscenaMapa;
import juego.pantallas.EscenaMenu;
import juego.reglas.BarraPrecision;
import juego.reglas.Combate;
import juego.reglas.EfectoEvento;
import juego.reglas.EventoNarrativo;
import juego.reglas.MapaBosque;
import juego.reglas.NodoMapa;
import juego.reglas.OpcionEvento;
import juego.reglas.Partida;
import juego.reglas.Registro;
import juego.reglas.TipoNodo;
import motor.Motor;
import utilidades.Tokenizador;

// Pruebas automáticas del juego (sin ventana). Se ejecuta con:  java -cp out juego.pruebas.PruebaJuego
// Cada comprobación imprime solo si falla; al final muestra el total.
public class PruebaJuego {

    // Contadores
    private static int total;
    private static int fallas;

    // Punto de entrada
    public static void main(String[] args) {
        pruebasGrafo();
        pruebasListaYTokenizador();
        pruebasJugador();
        pruebasHabilidades();
        pruebasBarra();
        pruebasEnemigos();
        pruebasCombate();
        pruebasMapa();
        pruebasEventos();
        pruebasPartida();
        pruebasArchivos();
        pruebasSprites();
        pruebasPantallas();
        pruebasAventuraCompleta();
        System.out.println();
        System.out.println(total - fallas + " de " + total + " comprobaciones correctas"
                + (fallas == 0 ? "  ->  OK" : "  ->  HAY " + fallas + " FALLAS"));
        if (fallas > 0) {
            System.exit(1);
        }
    }

    // Auxiliar de comprobación
    private static void comprobar(String descripcion, boolean condicion) {
        total++;
        if (!condicion) {
            fallas++;
            System.out.println("FALLA: " + descripcion);
        }
    }

    // Grafo
    private static void pruebasGrafo() {
        Grafo<String> g = new Grafo<>();
        int a = g.agregarVertice("A");
        int b = g.agregarVertice("B");
        int c = g.agregarVertice("C");
        int d = g.agregarVertice("D");
        int e = g.agregarVertice("E");
        comprobar("grafo: ids consecutivos", a == 0 && b == 1 && c == 2 && d == 3 && e == 4);
        comprobar("grafo: 5 vértices", g.cantidadVertices() == 5);
        comprobar("grafo: arista nueva devuelve true", g.agregarArista(a, b));
        g.agregarArista(a, c);
        g.agregarArista(b, d);
        g.agregarArista(c, d);
        comprobar("grafo: arista repetida devuelve false", !g.agregarArista(a, b));
        comprobar("grafo: lazo devuelve false", !g.agregarArista(a, a));
        comprobar("grafo: 4 aristas", g.cantidadAristas() == 4);
        comprobar("grafo: existe a->b", g.existeArista(a, b));
        comprobar("grafo: es dirigido (no existe b->a)", !g.existeArista(b, a));
        comprobar("grafo: grado de salida de a es 2", g.gradoSalida(a) == 2);
        comprobar("grafo: obtener devuelve el dato", g.obtener(c).equals("C"));
        comprobar("grafo: idsAdyacentes de a = 1,2",
                g.idsAdyacentes(a).tamanio() == 2 && g.idsAdyacentes(a).obtener(0) == 1 && g.idsAdyacentes(a).obtener(1) == 2);
        comprobar("grafo: adyacentes devuelve datos", g.adyacentes(a).obtener(0).equals("B"));

        ListaEnlazada<Integer> anchura = g.recorridoEnAnchura(a);
        comprobar("grafo: BFS visita 4 vértices (E es inalcanzable)", anchura.tamanio() == 4);
        comprobar("grafo: BFS orden A,B,C,D",
                anchura.obtener(0) == 0 && anchura.obtener(1) == 1 && anchura.obtener(2) == 2 && anchura.obtener(3) == 3);
        ListaEnlazada<Integer> profundidad = g.recorridoEnProfundidad(a);
        comprobar("grafo: DFS orden A,B,D,C",
                profundidad.tamanio() == 4 && profundidad.obtener(0) == 0 && profundidad.obtener(1) == 1
                        && profundidad.obtener(2) == 3 && profundidad.obtener(3) == 2);
        comprobar("grafo: hayCamino a->d", g.hayCamino(a, d));
        comprobar("grafo: no hayCamino d->a", !g.hayCamino(d, a));
        comprobar("grafo: no hayCamino a->e", !g.hayCamino(a, e));
        comprobar("grafo: BFS repetido da lo mismo", g.recorridoEnAnchura(a).tamanio() == 4);

        try {
            g.obtener(9);
            comprobar("grafo: obtener(9) debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("grafo: obtener(9) lanza IllegalArgumentException", true);
        }
        try {
            g.agregarArista(0, 9);
            comprobar("grafo: arista a vértice inexistente debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("grafo: arista a vértice inexistente lanza excepción", true);
        }
    }

    // ListaEnlazada y Tokenizador
    private static void pruebasListaYTokenizador() {
        ListaEnlazada<String> l = new ListaEnlazada<>();
        l.agregar("a");
        l.agregar("b");
        l.insertar(1, "x");
        comprobar("lista: insertar en el medio", l.obtener(1).equals("x") && l.tamanio() == 3);
        comprobar("lista: indiceDe", l.indiceDe("b") == 2 && l.indiceDe("zzz") == -1);
        comprobar("lista: eliminar el último", l.eliminar(2).equals("b") && l.tamanio() == 2);
        l.agregar("z");
        comprobar("lista: agregar tras eliminar el último enlaza bien", l.obtener(2).equals("z"));

        Tokenizador t = new Tokenizador("uno|dos||cuatro", '|');
        comprobar("tokenizador: primer trozo", t.hayMas() && t.siguiente().equals("uno"));
        comprobar("tokenizador: segundo trozo", t.siguiente().equals("dos"));
        comprobar("tokenizador: trozo vacío", t.siguiente().isEmpty());
        comprobar("tokenizador: último trozo", t.siguiente().equals("cuatro") && !t.hayMas());
        comprobar("tokenizador: texto vacío tiene 0 trozos", Tokenizador.contar("", '|') == 0);
        comprobar("tokenizador: 'a|' tiene 2 trozos", Tokenizador.contar("a|", '|') == 2);
        comprobar("tokenizador: sin separador tiene 1 trozo", Tokenizador.contar("hola", '|') == 1);
        try {
            Tokenizador vacio = new Tokenizador("a", '|');
            vacio.siguiente();
            vacio.siguiente();
            comprobar("tokenizador: pedir de más debe fallar", false);
        } catch (IllegalStateException ex) {
            comprobar("tokenizador: pedir de más lanza IllegalStateException", true);
        }
    }

    // Jugador
    private static void pruebasJugador() {
        Jugador j = new Jugador();
        comprobar("jugador: atributos iniciales 3/3/3", j.getFuerza() == 3 && j.getDestreza() == 3 && j.getInteligencia() == 3);
        comprobar("jugador: vida máxima = 20 + 5*Fuerza = 35", j.getVidaMaxima() == 35 && j.getVida() == 35);
        comprobar("jugador: maná máximo = 6 + 3*Inteligencia = 15", j.getManaMaximo() == 15 && j.getMana() == 15);
        comprobar("jugador: defensa = Destreza/2 = 1", j.getDefensa() == 1);
        comprobar("jugador: daño físico = 2 + 2*Fuerza = 8", j.getDanioFisico() == 8);
        comprobar("jugador: potencia mágica = 3 + 3*Inteligencia = 12", j.getPotenciaMagica() == 12);
        comprobar("jugador: curación = 6 + 3*Inteligencia = 15", j.getPotenciaCuracion() == 15);
        comprobar("jugador: empieza con 1 habilidad (ataque)", j.cantidadHabilidades() == 1 && j.conoceHabilidad("ATAQUE"));
        comprobar("jugador: empieza sin oro", j.getOro() == 0);

        int real = j.recibirDanio(5);
        comprobar("jugador: la defensa reduce el daño (5-1=4)", real == 4 && j.getVida() == 31);
        comprobar("jugador: el daño mínimo es 1", j.recibirDanio(0) == 1 && j.getVida() == 30);
        comprobar("jugador: curar no pasa del máximo", j.curar(100) == 5 && j.getVida() == 35);
        j.recibirDanio(1000);
        comprobar("jugador: la vida no baja de 0 y muere", j.getVida() == 0 && j.estaMuerto());

        Jugador k = new Jugador();
        k.mejorarAtributo(Jugador.FUERZA, 2);
        comprobar("jugador: +2 Fuerza sube vida máxima en 10 y la vida actual", k.getVidaMaxima() == 45 && k.getVida() == 45);
        k.mejorarAtributo(Jugador.INTELIGENCIA, 1);
        comprobar("jugador: +1 Inteligencia sube maná máximo y maná", k.getManaMaximo() == 18 && k.getMana() == 18);
        k.mejorarAtributo(Jugador.DESTREZA, 1);
        comprobar("jugador: Destreza 4 -> defensa 2", k.getDefensa() == 2 && k.getBonusZonaCritica() == 16);
        comprobar("jugador: más destreza = cursor más lento", k.getFactorVelocidadBarra() < new Jugador().getFactorVelocidadBarra());
        k.cambiarVida(-1000);
        comprobar("jugador: un evento nunca baja la vida de 1", k.getVida() == 1);
        k.cambiarOro(-50);
        comprobar("jugador: el oro no baja de 0", k.getOro() == 0);
        k.cambiarOro(30);
        comprobar("jugador: cambiarOro suma", k.getOro() == 30);
        k.cambiarMana(-1000);
        comprobar("jugador: el maná no baja de 0", k.getMana() == 0 && !k.puedePagar(1) && k.puedePagar(0));

        try {
            new Jugador(0, 3, 3, 10, 10, 0);
            comprobar("jugador: fuerza 0 debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("jugador: atributo 0 lanza IllegalArgumentException", true);
        }
        try {
            j.recibirDanio(-3);
            comprobar("jugador: daño negativo debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("jugador: daño negativo lanza excepción", true);
        }
        try {
            k.gastarMana(5);
            comprobar("jugador: gastar maná que no hay debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("jugador: gastar maná que no hay lanza excepción", true);
        }
        try {
            k.mejorarAtributo(9, 1);
            comprobar("jugador: atributo desconocido debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("jugador: atributo desconocido lanza excepción", true);
        }

        // Repertorio: máximo 4, sin repetidos, en orden del catálogo
        Jugador h = new Jugador();
        comprobar("repertorio: no repite habilidades", !h.aprenderHabilidad(new HabilidadAtaque()));
        comprobar("repertorio: próxima por aprender = CURACION", "CURACION".equals(h.proximaHabilidadPorAprender()));
        comprobar("repertorio: aprende curación", h.aprenderHabilidad(new HabilidadCuracion()));
        comprobar("repertorio: aprende golpe", h.aprenderHabilidad(new HabilidadGolpePotente()));
        comprobar("repertorio: aprende escape", h.aprenderHabilidad(new HabilidadEscape()));
        comprobar("repertorio: llega a 4", h.cantidadHabilidades() == Jugador.MAX_HABILIDADES);
        comprobar("repertorio: ya no queda nada por aprender", h.proximaHabilidadPorAprender() == null);
        comprobar("repertorio: getHabilidad(1) es la curación", h.getHabilidad(1).getCodigo().equals("CURACION"));
    }

    // Habilidades
    private static void pruebasHabilidades() {
        Jugador j = new Jugador();
        Enemigo e = new Esqueleto();
        Habilidad ataque = new HabilidadAtaque();
        String texto = ataque.aplicar(j, e, Calidad.NORMAL);
        comprobar("ataque normal hace 8 de daño", e.getVida() == 10 && texto.contains("8"));
        comprobar("ataque recupera 2 de maná", j.getMana() == 15);
        j.cambiarMana(-5);
        ataque.aplicar(j, e, Calidad.CRITICO);
        comprobar("ataque crítico hace el doble (16, tope de la vida restante)", e.getVida() == 0);
        comprobar("ataque acertado devuelve 2 de maná", j.getMana() == 12);
        Enemigo e2 = new Esqueleto();
        ataque.aplicar(j, e2, Calidad.FALLO);
        comprobar("ataque fallado no hace daño", e2.getVida() == 18);
        comprobar("ataque cuesta 0 maná y usa barra", ataque.getCostoMana() == 0 && ataque.usaBarra());

        Habilidad cura = new HabilidadCuracion();
        j.recibirDanio(20);
        int antes = j.getVida();
        cura.aplicar(j, e2, Calidad.NORMAL);
        comprobar("curación recupera 15 (6+3*3)", j.getVida() == antes + 15);
        comprobar("curación no usa barra y cuesta 4", !cura.usaBarra() && cura.getCostoMana() == 4);

        Habilidad golpe = new HabilidadGolpePotente();
        Enemigo e3 = new ReyOscuro();
        golpe.aplicar(j, e3, Calidad.NORMAL);
        comprobar("golpe potente normal hace 12", e3.getVida() == 48);
        golpe.aplicar(j, e3, Calidad.CRITICO);
        comprobar("golpe potente crítico hace 30 (12*5/2)", e3.getVida() == 18);
        comprobar("golpe potente tiene barra difícil", golpe.getDificultadBarra() < 1.0 && golpe.getCostoMana() == 5);

        Habilidad escape = new HabilidadEscape();
        comprobar("escape: esEscape y no usa barra", escape.esEscape() && !escape.usaBarra() && escape.getCostoMana() == 3);
        comprobar("las demás habilidades no son escape", !ataque.esEscape() && !cura.esEscape() && !golpe.esEscape());

        comprobar("catálogo: 4 habilidades", CatalogoHabilidades.CANTIDAD == 4);
        comprobar("catálogo: crear por código", CatalogoHabilidades.crear("GOLPE").getNombre().equals(golpe.getNombre()));
        comprobar("catálogo: código inválido", !CatalogoHabilidades.esValido("FUEGO") && CatalogoHabilidades.esValido("ESCAPE"));
        try {
            CatalogoHabilidades.crear("FUEGO");
            comprobar("catálogo: crear código inválido debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("catálogo: crear código inválido lanza excepción", true);
        }
    }

    // Barra de precisión
    private static void pruebasBarra() {
        BarraPrecision b = new BarraPrecision(new Jugador(), 1.0);
        comprobar("barra: el centro es crítico", b.calidadEn(300) == Calidad.CRITICO);
        comprobar("barra: los extremos fallan", b.calidadEn(0) == Calidad.FALLO && b.calidadEn(600) == Calidad.FALLO);
        comprobar("barra: una posición intermedia es normal", b.calidadEn(300 + 100) == Calidad.NORMAL);
        comprobar("barra: crítica (40+12=52 px) es más angosta que la normal", b.getAnchoCritico() == 52 && b.getAnchoCritico() < b.getAnchoNormal());

        Jugador agil = new Jugador();
        agil.mejorarAtributo(Jugador.DESTREZA, 5);
        BarraPrecision facil = new BarraPrecision(agil, 1.0);
        comprobar("barra: más Destreza agranda la zona crítica", facil.getAnchoCritico() > b.getAnchoCritico());
        comprobar("barra: más Destreza baja la velocidad", facil.getVelocidad() < b.getVelocidad());
        BarraPrecision dificil = new BarraPrecision(new Jugador(), 0.6);
        comprobar("barra: dificultad 0.6 achica la zona crítica", dificil.getAnchoCritico() < b.getAnchoCritico());
        comprobar("barra: dificultad 0.6 achica la zona normal", dificil.getAnchoNormal() < b.getAnchoNormal());

        BarraPrecision m = new BarraPrecision(new Jugador(), 1.0);
        m.actualizar(0.1);
        comprobar("barra: el cursor avanza", m.getPosicion() > 0);
        m.setPosicion(599);
        m.actualizar(0.5);
        comprobar("barra: llega al extremo derecho y no se pasa", m.getPosicion() == 600);
        m.actualizar(0.1);
        comprobar("barra: rebota y vuelve hacia la izquierda", m.getPosicion() < 600 && m.getPosicion() > 0);
        m.setPosicion(300);
        int r = m.detener();
        comprobar("barra: detener en el centro da CRITICO", r == Calidad.CRITICO && m.estaDetenida() && m.getResultado() == r);
        double fija = m.getPosicion();
        m.actualizar(1.0);
        comprobar("barra: detenida no se mueve", m.getPosicion() == fija);
        try {
            new BarraPrecision(0, 1.0, 0);
            comprobar("barra: dificultad 0 debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("barra: dificultad 0 lanza excepción", true);
        }
        try {
            m.setPosicion(700);
            comprobar("barra: posición fuera debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("barra: posición fuera lanza excepción", true);
        }
    }

    // Enemigos
    private static void pruebasEnemigos() {
        Esqueleto esq = new Esqueleto();
        comprobar("esqueleto: 18 de vida y 5 de daño", esq.getVida() == 18 && esq.getDanio() == 5);
        Jugador j = new Jugador();
        String t = esq.atacar(j);
        comprobar("esqueleto: el ataque hiere (5-1 de defensa)", j.getVida() == 31 && t.contains("4"));
        comprobar("recibirDanio devuelve el daño real (tope = vida restante)", esq.recibirDanio(100) == 18 && esq.estaMuerto());

        Lobo lobo = new Lobo();
        comprobar("lobo: actúa 2 veces por ronda", lobo.getTurnosPorRonda() == 2 && esq.getTurnosPorRonda() == 1);

        Brujo brujo = new Brujo();
        Jugador j2 = new Jugador();
        brujo.recibirDanio(10);
        brujo.atacar(j2);
        brujo.atacar(j2);
        int vidaAntes = brujo.getVida();
        brujo.atacar(j2);
        comprobar("brujo: cada tercera acción se cura 5", brujo.getVida() == vidaAntes + 5 && j2.getVida() == 25);

        ReyOscuro rey = new ReyOscuro();
        Jugador j3 = new Jugador();
        j3.mejorarAtributo(Jugador.FUERZA, 5);
        rey.atacar(j3);
        rey.atacar(j3);
        rey.atacar(j3);
        int antes = j3.getVida();
        rey.atacar(j3);
        comprobar("rey: cada cuarta acción pega doble (16-1)", antes - j3.getVida() == 15);
        comprobar("rey: 60 de vida y 50 de oro", rey.getVidaMaxima() == 60 && rey.getRecompensaOro() == 50);

        Esqueleto fuerte = new Esqueleto();
        fuerte.reforzar(4);
        comprobar("reforzar(4): +12 de vida y +2 de daño", fuerte.getVidaMaxima() == 30 && fuerte.getVida() == 30 && fuerte.getDanio() == 7);

        java.util.Random azar = new java.util.Random(5);
        boolean aparecioBrujoTemprano = false;
        boolean aparecioBrujoTarde = false;
        for (int i = 0; i < 80; i++) {
            if (FabricaEnemigos.crearAleatorio(1, azar).getNombre().equals("Brujo")) {
                aparecioBrujoTemprano = true;
            }
            if (FabricaEnemigos.crearAleatorio(4, azar).getNombre().equals("Brujo")) {
                aparecioBrujoTarde = true;
            }
        }
        comprobar("fábrica: no hay brujos en el piso 1", !aparecioBrujoTemprano);
        comprobar("fábrica: sí hay brujos desde el piso 3", aparecioBrujoTarde);
        comprobar("fábrica: crearPorNombre", FabricaEnemigos.crearPorNombre("LOBO", 0).getNombre().equals("Lobo"));
        comprobar("fábrica: jefe", FabricaEnemigos.crearJefe().getNombre().equals("Rey Oscuro"));
        try {
            FabricaEnemigos.crearPorNombre("DRAGON", 0);
            comprobar("fábrica: nombre desconocido debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("fábrica: nombre desconocido lanza excepción", true);
        }
        try {
            esq.recibirDanio(-1);
            comprobar("enemigo: daño negativo debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("enemigo: daño negativo lanza excepción", true);
        }
    }

    // Combate
    private static void pruebasCombate() {
        Jugador j = new Jugador();
        Combate c = new Combate(j, new Lobo(), false);
        comprobar("combate: empieza eligiendo el jugador", c.getEstado() == Combate.ELECCION);
        ColaEnlazada<Integer> orden = c.getOrdenDeTurnos();
        comprobar("combate: la cola tiene J, E, E (lobo veloz)",
                orden.tamanio() == 3 && orden.desencolar() == Combate.JUGADOR && orden.desencolar() == Combate.ENEMIGO
                        && orden.desencolar() == Combate.ENEMIGO);
        comprobar("combate: mirar la cola no la consume", c.getOrdenDeTurnos().tamanio() == 3);
        comprobar("combate: habilidad inexistente se rechaza", !c.elegirHabilidad(3));

        comprobar("combate: elegir ataque pasa a APUNTANDO", c.elegirHabilidad(0) && c.getEstado() == Combate.APUNTANDO);
        comprobar("combate: durante la barra no se puede elegir otra", !c.elegirHabilidad(0));
        c.getBarra().setPosicion(300);
        c.detenerBarra();
        comprobar("combate: tras frenar hay RESULTADO", c.getEstado() == Combate.RESULTADO);
        comprobar("combate: el crítico (16) derrotó al lobo (14 de vida)", c.getEnemigo().estaMuerto());
        comprobar("combate: el turno del jugador salió de la cola", c.getOrdenDeTurnos().tamanio() == 2);

        // Un combate más largo con un esqueleto
        Jugador j2 = new Jugador();
        Combate c2 = new Combate(j2, new Esqueleto(), false);
        c2.elegirHabilidad(0);
        c2.getBarra().setPosicion(200);
        c2.detenerBarra();
        comprobar("combate: golpe normal (8) al esqueleto", c2.getEnemigo().getVida() == 10);
        c2.continuar();
        comprobar("combate: después del resultado toca el enemigo", c2.getEstado() == Combate.TURNO_ENEMIGO);
        c2.continuar();
        comprobar("combate: el enemigo hirió al jugador", j2.getVida() == 31 && c2.getEstado() == Combate.RESULTADO);
        c2.continuar();
        comprobar("combate: nueva ronda, vuelve a elegir el jugador", c2.getEstado() == Combate.ELECCION && c2.getRondas() == 2);

        // Maná insuficiente
        Jugador j3 = new Jugador();
        j3.aprenderHabilidad(new HabilidadCuracion());
        j3.cambiarMana(-100);
        Combate c3 = new Combate(j3, new Esqueleto(), false);
        comprobar("combate: sin maná no se puede curar", !c3.elegirHabilidad(1) && c3.getEstado() == Combate.ELECCION);

        // Curación sin barra
        Jugador j4 = new Jugador();
        j4.aprenderHabilidad(new HabilidadCuracion());
        j4.recibirDanio(20);
        Combate c4 = new Combate(j4, new Esqueleto(), false);
        c4.elegirHabilidad(1);
        comprobar("combate: la curación se resuelve sin barra (16 + 15 = 31)", c4.getEstado() == Combate.RESULTADO && j4.getVida() == 31);
        comprobar("combate: la curación gastó 4 de maná", j4.getMana() == 11);

        // Escape
        Jugador j5 = new Jugador();
        j5.aprenderHabilidad(new HabilidadCuracion());
        j5.aprenderHabilidad(new HabilidadGolpePotente());
        j5.aprenderHabilidad(new HabilidadEscape());
        Combate c5 = new Combate(j5, new Esqueleto(), false);
        comprobar("combate: se puede huir de un combate normal", c5.elegirHabilidad(3) && c5.getEstado() == Combate.HUIDA);
        comprobar("combate: huir terminó el combate", c5.haTerminado());
        Combate jefe = new Combate(j5, new ReyOscuro(), true);
        comprobar("combate: no se puede huir del jefe", !jefe.elegirHabilidad(3) && jefe.getEstado() == Combate.ELECCION);
        comprobar("combate: no se gastó maná al intentar huir del jefe", j5.getMana() == 12);

        // Victoria
        Jugador j6 = new Jugador();
        Enemigo debil = new Esqueleto();
        debil.recibirDanio(17);
        Combate c6 = new Combate(j6, debil, false);
        c6.elegirHabilidad(0);
        c6.getBarra().setPosicion(300);
        c6.detenerBarra();
        c6.continuar();
        comprobar("combate: victoria cuando el enemigo cae", c6.getEstado() == Combate.VICTORIA && c6.haTerminado());
        comprobar("combate: la victoria da el oro (6)", j6.getOro() == 6);

        // Derrota
        Jugador j7 = new Jugador();
        j7.recibirDanio(34);
        Combate c7 = new Combate(j7, new Esqueleto(), false);
        c7.elegirHabilidad(0);
        c7.getBarra().setPosicion(0);
        c7.detenerBarra();
        c7.continuar();
        c7.continuar();
        c7.continuar();
        comprobar("combate: derrota cuando el jugador cae", c7.getEstado() == Combate.DERROTA && j7.estaMuerto());
        try {
            new Combate(null, new Lobo(), false);
            comprobar("combate: sin jugador debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("combate: sin jugador lanza excepción", true);
        }
    }

    // Mapa (grafo)
    private static void pruebasMapa() {
        boolean todosConectados = true;
        boolean todosConSalida = true;
        boolean todosConEntrada = true;
        boolean cofresForzados = true;
        boolean cantidades = true;
        boolean aristasAdelante = true;
        boolean jefeAlcanzable = true;
        for (long semilla = 0; semilla < 40; semilla++) {
            MapaBosque m = new MapaBosque(semilla);
            Grafo<NodoMapa> g = m.getGrafo();
            if (g.recorridoEnAnchura(m.getIdInicio()).tamanio() != g.cantidadVertices()) {
                todosConectados = false;
            }
            if (!g.hayCamino(m.getIdInicio(), m.getIdJefe())) {
                jefeAlcanzable = false;
            }
            if (g.cantidadVertices() < 12 || g.cantidadVertices() > 17) {
                cantidades = false;
            }
            for (int id = 0; id < g.cantidadVertices(); id++) {
                NodoMapa n = g.obtener(id);
                if (n.getTipo() != TipoNodo.JEFE && g.gradoSalida(id) == 0) {
                    todosConSalida = false;
                }
                ListaEnlazada<Integer> destinos = g.idsAdyacentes(id);
                for (int k = 0; k < destinos.tamanio(); k++) {
                    if (g.obtener(destinos.obtener(k)).getPiso() != n.getPiso() + 1) {
                        aristasAdelante = false;
                    }
                }
                if (n.getTipo() != TipoNodo.INICIO && !tieneEntrada(g, id)) {
                    todosConEntrada = false;
                }
            }
            if (!hayCofreEnPiso(g, 2) || !hayCofreEnPiso(g, 4)) {
                cofresForzados = false;
            }
        }
        comprobar("mapa: desde el inicio se llega a todos los nodos (40 semillas)", todosConectados);
        comprobar("mapa: el jefe es alcanzable (40 semillas)", jefeAlcanzable);
        comprobar("mapa: ningún nodo (salvo el jefe) queda sin salida", todosConSalida);
        comprobar("mapa: todos los nodos tienen entrada", todosConEntrada);
        comprobar("mapa: las aristas siempre avanzan un piso", aristasAdelante);
        comprobar("mapa: hay cofre en los pisos 2 y 4", cofresForzados);
        comprobar("mapa: entre 12 y 17 nodos", cantidades);

        MapaBosque a = new MapaBosque(77);
        MapaBosque b = new MapaBosque(77);
        boolean iguales = a.cantidadNodos() == b.cantidadNodos() && a.getGrafo().cantidadAristas() == b.getGrafo().cantidadAristas();
        for (int id = 0; id < a.cantidadNodos() && iguales; id++) {
            iguales = a.getNodo(id).getTipo() == b.getNodo(id).getTipo() && a.getNodo(id).getY() == b.getNodo(id).getY();
        }
        comprobar("mapa: la misma semilla genera el mismo mapa", iguales);

        boolean algunoDistinto = false;
        for (long s = 1; s < 20 && !algunoDistinto; s++) {
            MapaBosque otro = new MapaBosque(s);
            if (otro.cantidadNodos() != a.cantidadNodos() || otro.getGrafo().cantidadAristas() != a.getGrafo().cantidadAristas()) {
                algunoDistinto = true;
            }
        }
        comprobar("mapa: semillas distintas generan mapas distintos", algunoDistinto);
        comprobar("mapa: el inicio es del tipo INICIO y el jefe del tipo JEFE",
                a.getNodo(a.getIdInicio()).getTipo() == TipoNodo.INICIO && a.getNodo(a.getIdJefe()).getTipo() == TipoNodo.JEFE);
        comprobar("mapa: el jefe está en el piso 6", a.getNodo(a.getIdJefe()).getPiso() == MapaBosque.PISOS - 1);
    }

    private static boolean tieneEntrada(Grafo<NodoMapa> g, int destino) {
        for (int id = 0; id < g.cantidadVertices(); id++) {
            if (g.existeArista(id, destino)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hayCofreEnPiso(Grafo<NodoMapa> g, int piso) {
        for (int id = 0; id < g.cantidadVertices(); id++) {
            NodoMapa n = g.obtener(id);
            if (n.getPiso() == piso && n.getTipo() == TipoNodo.COFRE) {
                return true;
            }
        }
        return false;
    }

    // Eventos
    private static void pruebasEventos() {
        GestorArchivos gestor = new GestorArchivos("datos");
        ListaEnlazada<String> lineas = new ListaEnlazada<>();
        lineas.agregar("# comentario");
        lineas.agregar("");
        lineas.agregar("EVENTO|Altar|Un altar viejo.");
        lineas.agregar("OPCION|Rezar|VIDA:5;MANA:-2|Rezas.");
        lineas.agregar("OPCION|Irte||Te vas.");
        lineas.agregar("EVENTO|Roto|Faltan opciones.");
        lineas.agregar("OPCION|Única|VIDA:1|Solo una.");
        lineas.agregar("EVENTO|Efecto inválido|Efecto desconocido.");
        lineas.agregar("OPCION|A|FUEGO:5|x");
        lineas.agregar("OPCION|B|VIDA:1|y");
        lineas.agregar("EVENTO|Número inválido|Número mal escrito.");
        lineas.agregar("OPCION|A|VIDA:mucho|x");
        lineas.agregar("OPCION|B|VIDA:1|y");
        lineas.agregar("EVENTO|Cuatro|Demasiadas opciones.");
        lineas.agregar("OPCION|1||a");
        lineas.agregar("OPCION|2||b");
        lineas.agregar("OPCION|3||c");
        lineas.agregar("OPCION|4||d");
        lineas.agregar("EVENTO|Emboscada|Un lobo.");
        lineas.agregar("OPCION|Pelear|COMBATE:LOBO|Pelea.");
        lineas.agregar("OPCION|Huir|VIDA:-2|Huyes.");
        ListaEnlazada<EventoNarrativo> eventos = gestor.interpretarEventos(lineas);
        comprobar("eventos: solo 2 eventos son válidos (los demás se descartan)", eventos.tamanio() == 2);
        comprobar("eventos: título y relato", eventos.obtener(0).getTitulo().equals("Altar")
                && eventos.obtener(0).getRelato().equals("Un altar viejo."));
        comprobar("eventos: 2 opciones en el altar", eventos.obtener(0).cantidadOpciones() == 2);
        OpcionEvento rezar = eventos.obtener(0).getOpcion(0);
        comprobar("eventos: la opción tiene 2 efectos", rezar.cantidadEfectos() == 2);
        comprobar("eventos: efecto numérico leído", rezar.getEfecto(0).getTipo() == EfectoEvento.Tipo.VIDA && rezar.getEfecto(0).getValor() == 5);
        comprobar("eventos: efecto negativo leído", rezar.getEfecto(1).getValor() == -2);
        comprobar("eventos: opción sin efectos", eventos.obtener(0).getOpcion(1).cantidadEfectos() == 0);
        comprobar("eventos: emboscada detectada", "LOBO".equals(eventos.obtener(1).getOpcion(0).getEmboscada())
                && eventos.obtener(1).getOpcion(1).getEmboscada() == null);

        Jugador j = new Jugador();
        j.recibirDanio(10);
        j.cambiarMana(-5);
        String resumen = rezar.aplicar(j);
        comprobar("eventos: aplicar suma vida y resta maná", j.getVida() == 31 && j.getMana() == 8);
        comprobar("eventos: el resumen describe los efectos", resumen.equals("+5 vida, -2 maná"));

        ListaEnlazada<EfectoEvento> pago = gestor.interpretarEfectos("ORO:-15;VIDA:3");
        OpcionEvento comprar = new OpcionEvento("Comprar", "Compras.", pago);
        Jugador pobre = new Jugador();
        comprobar("eventos: sin oro no se puede elegir una opción que cuesta oro", !comprar.puedeElegir(pobre));
        pobre.cambiarOro(15);
        comprobar("eventos: con oro suficiente sí", comprar.puedeElegir(pobre));
        comprar.aplicar(pobre);
        comprobar("eventos: pagar descuenta el oro", pobre.getOro() == 0);

        ListaEnlazada<EfectoEvento> habilidad = gestor.interpretarEfectos("HABILIDAD:GOLPE");
        Jugador alumno = new Jugador();
        String texto = habilidad.obtener(0).aplicar(alumno);
        comprobar("eventos: HABILIDAD enseña el hechizo", alumno.conoceHabilidad("GOLPE") && texto.startsWith("aprendes"));
        comprobar("eventos: si ya la conoce no la repite", habilidad.obtener(0).aplicar(alumno).contains("ya conocías"));
        comprobar("eventos: efectos vacío = lista vacía", gestor.interpretarEfectos("").estaVacia());
        try {
            new EventoNarrativo("X", "y", new ListaEnlazada<OpcionEvento>());
            comprobar("eventos: sin opciones debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("eventos: sin opciones lanza excepción", true);
        }
        try {
            new EfectoEvento(EfectoEvento.Tipo.HABILIDAD, 0, "FUEGO");
            comprobar("eventos: habilidad inválida debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("eventos: habilidad inválida lanza excepción", true);
        }

        // El archivo real del juego
        ListaEnlazada<EventoNarrativo> reales = gestor.cargarEventos();
        comprobar("eventos.txt real: se cargan al menos 9 eventos", reales.tamanio() >= 9);
        boolean todosValidos = true;
        for (int i = 0; i < reales.tamanio(); i++) {
            EventoNarrativo e = reales.obtener(i);
            if (e.cantidadOpciones() < 2 || e.cantidadOpciones() > 3) {
                todosValidos = false;
            }
        }
        comprobar("eventos.txt real: todos tienen 2 o 3 opciones", todosValidos);
        boolean todosTienenSalida = true;
        for (int i = 0; i < reales.tamanio(); i++) {
            boolean hayGratis = false;
            for (int k = 0; k < reales.obtener(i).cantidadOpciones(); k++) {
                if (reales.obtener(i).getOpcion(k).puedeElegir(new Jugador())) {
                    hayGratis = true;
                }
            }
            if (!hayGratis) {
                todosTienenSalida = false;
            }
        }
        comprobar("eventos.txt real: un jugador sin oro siempre puede elegir alguna opción", todosTienenSalida);
        boolean ensenaCuracion = false;
        boolean ensenaGolpe = false;
        boolean ensenaEscape = false;
        for (int i = 0; i < reales.tamanio(); i++) {
            for (int k = 0; k < reales.obtener(i).cantidadOpciones(); k++) {
                OpcionEvento o = reales.obtener(i).getOpcion(k);
                for (int f = 0; f < o.cantidadEfectos(); f++) {
                    EfectoEvento ef = o.getEfecto(f);
                    if (ef.getTipo() == EfectoEvento.Tipo.HABILIDAD) {
                        ensenaCuracion = ensenaCuracion || ef.getTexto().equals("CURACION");
                        ensenaGolpe = ensenaGolpe || ef.getTexto().equals("GOLPE");
                        ensenaEscape = ensenaEscape || ef.getTexto().equals("ESCAPE");
                    }
                }
            }
        }
        comprobar("eventos.txt real: enseña curación, golpe y escape", ensenaCuracion && ensenaGolpe && ensenaEscape);

        // Sin archivo: se usan los eventos de respaldo
        GestorArchivos sinArchivos = new GestorArchivos(carpetaTemporal("vacia").getPath());
        comprobar("eventos: sin eventos.txt se usan los de respaldo", sinArchivos.cargarEventos().tamanio() >= 2);
    }

    // Partida
    private static void pruebasPartida() {
        ListaEnlazada<EventoNarrativo> eventos = new GestorArchivos("datos").cargarEventos();
        Partida p = Partida.nueva(42, eventos);
        comprobar("partida: empieza en el inicio", p.getNodoActual() == p.getMapa().getIdInicio());
        comprobar("partida: el inicio está visitado", p.getNodoActualCompleto().estaVisitado());
        comprobar("partida: la ruta tiene 1 nodo", p.getRuta().tamanio() == 1);
        ListaEnlazada<Integer> vecinos = p.getAdyacentes();
        comprobar("partida: desde el inicio hay 2 o 3 caminos", vecinos.tamanio() >= 2 && vecinos.tamanio() <= 3);
        int destino = vecinos.obtener(0);
        comprobar("partida: puede moverse a un vecino", p.puedeMoverA(destino));
        comprobar("partida: no puede saltar al jefe", !p.puedeMoverA(p.getMapa().getIdJefe()));
        comprobar("partida: id inválido no se puede", !p.puedeMoverA(-1) && !p.puedeMoverA(999));
        p.moverA(destino);
        comprobar("partida: tras moverse está en el destino", p.getNodoActual() == destino);
        comprobar("partida: el destino quedó visitado", p.getMapa().getNodo(destino).estaVisitado());
        comprobar("partida: la ruta apila el nodo (tope)", p.getRuta().tamanio() == 2 && p.getRuta().obtener(1) == destino);
        try {
            p.moverA(p.getMapa().getIdInicio());
            comprobar("partida: volver atrás debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("partida: volver atrás lanza excepción", true);
        }

        p.registrar(destino, "Segundo");
        p.registrar(destino, "Tercero");
        ListaEnlazada<Registro> ultimos = p.getUltimosRegistros(2);
        comprobar("diario: los últimos salen del más reciente al más antiguo",
                ultimos.tamanio() == 2 && ultimos.obtener(0).getTexto().equals("Tercero") && ultimos.obtener(1).getTexto().equals("Segundo"));
        comprobar("diario: mirar la pila no la destruye", p.getDiarioCompleto().tamanio() == 4);
        comprobar("diario: el completo va del más antiguo al más nuevo",
                p.getDiarioCompleto().obtener(0).getTexto().startsWith("Entras") && p.getDiarioCompleto().obtener(3).getTexto().equals("Tercero"));
        comprobar("diario: pedir más de los que hay devuelve todos", p.getUltimosRegistros(99).tamanio() == 4);

        // Enemigos y eventos por nodo: siempre iguales
        NodoMapa nodo = p.getNodoActualCompleto();
        Enemigo e1 = p.crearEnemigoDeNodo(nodo);
        Enemigo e2 = p.crearEnemigoDeNodo(nodo);
        comprobar("partida: el mismo nodo siempre da el mismo enemigo", e1.getNombre().equals(e2.getNombre()) && e1.getVidaMaxima() == e2.getVidaMaxima());
        comprobar("partida: el evento de un nodo es siempre el mismo",
                p.crearEventoDeNodo(nodo).getTitulo().equals(p.crearEventoDeNodo(nodo).getTitulo()));
        NodoMapa jefe = p.getMapa().getNodo(p.getMapa().getIdJefe());
        comprobar("partida: el nodo jefe da el Rey Oscuro", p.crearEnemigoDeNodo(jefe).getNombre().equals("Rey Oscuro"));

        // Cofre
        EventoNarrativo cofre = p.crearEventoCofre();
        comprobar("cofre: 3 opciones", cofre.cantidadOpciones() == 3);
        comprobar("cofre: la primera enseña la próxima habilidad (CURACION)",
                cofre.getOpcion(0).getEfecto(0).getTipo() == EfectoEvento.Tipo.HABILIDAD
                        && cofre.getOpcion(0).getEfecto(0).getTexto().equals("CURACION"));
        cofre.getOpcion(0).aplicar(p.getJugador());
        comprobar("cofre: el héroe aprendió curación", p.getJugador().conoceHabilidad("CURACION"));
        p.getJugador().aprenderHabilidad(new HabilidadGolpePotente());
        p.getJugador().aprenderHabilidad(new HabilidadEscape());
        comprobar("cofre: con todas las habilidades da Inteligencia",
                p.crearEventoCofre().getOpcion(0).getEfecto(0).getTipo() == EfectoEvento.Tipo.INTELIGENCIA);

        // Crecimiento
        Partida q = Partida.nueva(1, eventos);
        comprobar("crecimiento: el primer derrotado no mejora nada", q.sumarDerrotado() == null);
        String m1 = q.sumarDerrotado();
        comprobar("crecimiento: el segundo da +1 Fuerza", m1 != null && m1.contains("Fuerza") && q.getJugador().getFuerza() == 4);
        q.sumarDerrotado();
        String m2 = q.sumarDerrotado();
        comprobar("crecimiento: el cuarto da +1 Destreza", m2 != null && m2.contains("Destreza") && q.getJugador().getDestreza() == 4);
        q.sumarDerrotado();
        String m3 = q.sumarDerrotado();
        comprobar("crecimiento: el sexto da +1 Inteligencia", m3 != null && m3.contains("Inteligencia") && q.getJugador().getInteligencia() == 4);
        comprobar("crecimiento: cuenta 6 derrotados", q.getDerrotados() == 6);

        // Validaciones
        try {
            new Partida(1, new Jugador(), new PilaEnlazada<Integer>(), new PilaEnlazada<Registro>(), 0, eventos);
            comprobar("partida: ruta vacía debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("partida: ruta vacía lanza excepción", true);
        }
        try {
            new Registro(0, "");
            comprobar("registro: texto vacío debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("registro: texto vacío lanza excepción", true);
        }
    }

    // Persistencia
    private static void pruebasArchivos() {
        ListaEnlazada<EventoNarrativo> eventos = new GestorArchivos("datos").cargarEventos();
        File carpeta = carpetaTemporal("guardado");
        GestorArchivos gestor = new GestorArchivos(carpeta.getPath());
        comprobar("archivos: sin guardado, existeGuardado es false", !gestor.existeGuardado());
        comprobar("archivos: cargar sin archivo devuelve null", gestor.cargar(eventos) == null);

        Partida p = Partida.nueva(2024, eventos);
        p.getJugador().aprenderHabilidad(new HabilidadCuracion());
        p.getJugador().mejorarAtributo(Jugador.FUERZA, 2);
        p.getJugador().cambiarOro(37);
        p.getJugador().recibirDanio(12);
        p.getJugador().cambiarMana(-4);
        p.moverA(p.getAdyacentes().obtener(0));
        p.registrar(p.getNodoActual(), "Venciste a un lobo | con una barra");
        p.sumarDerrotado();
        p.moverA(p.getAdyacentes().obtener(0));
        comprobar("archivos: guardar devuelve true", gestor.guardar(p));
        comprobar("archivos: existe el guardado", gestor.existeGuardado());

        Partida c = gestor.cargar(eventos);
        comprobar("archivos: se pudo cargar", c != null);
        if (c != null) {
            Jugador a = p.getJugador();
            Jugador b = c.getJugador();
            comprobar("archivos: misma semilla", c.getSemilla() == p.getSemilla());
            comprobar("archivos: mismos atributos", a.getFuerza() == b.getFuerza() && a.getDestreza() == b.getDestreza()
                    && a.getInteligencia() == b.getInteligencia());
            comprobar("archivos: misma vida, maná y oro", a.getVida() == b.getVida() && a.getMana() == b.getMana() && a.getOro() == b.getOro());
            comprobar("archivos: mismas habilidades", b.cantidadHabilidades() == 2 && b.conoceHabilidad("ATAQUE") && b.conoceHabilidad("CURACION"));
            comprobar("archivos: mismo nodo actual", c.getNodoActual() == p.getNodoActual());
            comprobar("archivos: misma ruta", c.getRuta().tamanio() == p.getRuta().tamanio());
            comprobar("archivos: nodos visitados marcados en el grafo cargado", c.getMapa().getNodo(c.getNodoActual()).estaVisitado());
            comprobar("archivos: mismos derrotados", c.getDerrotados() == p.getDerrotados());
            comprobar("archivos: mismo diario (cantidad y orden)", c.getDiarioCompleto().tamanio() == p.getDiarioCompleto().tamanio()
                    && c.getDiarioCompleto().obtener(0).getTexto().equals(p.getDiarioCompleto().obtener(0).getTexto()));
            comprobar("archivos: el texto del diario con '|' se conserva",
                    c.getUltimosRegistros(5).obtener(1).getTexto().equals("Venciste a un lobo | con una barra"));
            comprobar("archivos: el mapa cargado es idéntico",
                    c.getMapa().cantidadNodos() == p.getMapa().cantidadNodos()
                            && c.getMapa().getGrafo().cantidadAristas() == p.getMapa().getGrafo().cantidadAristas());
        }

        // Archivos dañados: todos deben dar null, nunca una excepción
        comprobar("danado: número inválido", cargaConLinea(gestor, eventos, "FUERZA=", "FUERZA=abc") == null);
        comprobar("danado: atributo imposible", cargaConLinea(gestor, eventos, "FUERZA=", "FUERZA=0") == null);
        comprobar("danado: vida cero", cargaConLinea(gestor, eventos, "VIDA=", "VIDA=0") == null);
        comprobar("danado: versión desconocida", cargaConLinea(gestor, eventos, "VERSION=", "VERSION=9") == null);
        comprobar("danado: habilidad inventada", cargaConLinea(gestor, eventos, "HABILIDADES=", "HABILIDADES=ATAQUE,FUEGO") == null);
        comprobar("danado: sin la habilidad ATAQUE", cargaConLinea(gestor, eventos, "HABILIDADES=", "HABILIDADES=CURACION") == null);
        comprobar("danado: sin habilidades", cargaConLinea(gestor, eventos, "HABILIDADES=", "HABILIDADES=") == null);
        comprobar("danado: ruta que salta nodos", cargaConLinea(gestor, eventos, "RUTA=", "RUTA=0,99") == null);
        comprobar("danado: ruta que no empieza en el inicio", cargaConLinea(gestor, eventos, "RUTA=", "RUTA=5") == null);
        comprobar("danado: nodo actual que no coincide con la ruta", cargaConLinea(gestor, eventos, "NODO_ACTUAL=", "NODO_ACTUAL=0") == null);
        comprobar("danado: falta una clave", cargaConLinea(gestor, eventos, "ORO=", "OTRA=1") == null);
        escribirArchivo(gestor.rutaGuardado(), "esto no es un guardado\n\u0000basura");
        comprobar("danado: archivo con basura", gestor.cargar(eventos) == null);
        escribirArchivo(gestor.rutaGuardado(), "");
        comprobar("danado: archivo vacío", gestor.cargar(eventos) == null);
        comprobar("danado: el archivo no se borra solo", gestor.existeGuardado());
        comprobar("archivos: borrarGuardado", gestor.borrarGuardado() && !gestor.existeGuardado());
        comprobar("archivos: borrar sin archivo devuelve false", !gestor.borrarGuardado());
        try {
            new GestorArchivos("");
            comprobar("archivos: carpeta vacía debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("archivos: carpeta vacía lanza excepción", true);
        }
    }

    // Guarda una partida válida, cambia una línea y trata de cargarla
    private static Partida cargaConLinea(GestorArchivos gestor, ListaEnlazada<EventoNarrativo> eventos, String prefijo, String nueva) {
        Partida p = Partida.nueva(2024, eventos);
        p.moverA(p.getAdyacentes().obtener(0));
        gestor.guardar(p);
        ListaEnlazada<String> lineas = gestor.leerLineas(gestor.rutaGuardado());
        String texto = "";
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i);
            texto = texto + (linea.startsWith(prefijo) ? nueva : linea) + "\n";
        }
        escribirArchivo(gestor.rutaGuardado(), texto);
        return gestor.cargar(eventos);
    }

    private static void escribirArchivo(String ruta, String texto) {
        try (PrintWriter salida = new PrintWriter(ruta, "UTF-8")) {
            salida.print(texto);
        } catch (java.io.IOException e) {
            comprobar("no se pudo escribir el archivo de prueba " + ruta, false);
        }
    }

    private static File carpetaTemporal(String nombre) {
        File carpeta = new File(System.getProperty("java.io.tmpdir"), "sendero-" + nombre + "-" + System.nanoTime());
        carpeta.mkdirs();
        return carpeta;
    }

    // Sprites
    private static void pruebasSprites() {
        boolean todosBien = true;
        for (int i = 0; i < CatalogoSprites.CANTIDAD; i++) {
            Sprite s = CatalogoSprites.proceduralDe(CatalogoSprites.nombrePorOrden(i));
            if (s.getAncho() != 16 || s.getAlto() != 16) {
                todosBien = false;
            }
        }
        comprobar("sprites: los " + CatalogoSprites.CANTIDAD + " sprites miden 16x16", todosBien);
        comprobar("sprites: existe / no existe", CatalogoSprites.existe("jugador") && !CatalogoSprites.existe("dragon"));
        try {
            new Sprite("abc/de");
            comprobar("sprite: filas desparejas debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("sprite: filas desparejas lanza excepción", true);
        }
        try {
            CatalogoSprites.proceduralDe("dragon");
            comprobar("sprites: nombre inexistente debe fallar", false);
        } catch (IllegalArgumentException ex) {
            comprobar("sprites: nombre inexistente lanza excepción", true);
        }

        BufferedImage img = new Sprite("K./.K").crearImagen(3);
        comprobar("sprite: crearImagen agranda (2x2 -> 6x6)", img.getWidth() == 6 && img.getHeight() == 6);
        comprobar("sprite: '.' es transparente y 'K' es opaco", (img.getRGB(3, 0) >>> 24) == 0 && (img.getRGB(0, 0) >>> 24) == 255);

        // Respaldo: carpeta sin imágenes
        File carpeta = carpetaTemporal("assets");
        GestorSprites gestor = new GestorSprites(carpeta.getPath());
        comprobar("sprites: sin .png no hay archivo", !gestor.tieneArchivo("jugador"));
        BufferedImage respaldo = gestor.obtener("jugador", 4);
        comprobar("sprites: sin .png se usa el dibujo procedural (64x64)", respaldo.getWidth() == 64 && respaldo.getHeight() == 64);
        comprobar("sprites: el caché devuelve la misma imagen", gestor.obtener("jugador", 4) == respaldo);
        comprobar("sprites: otra escala es otra imagen", gestor.obtener("jugador", 2).getWidth() == 32);

        // .png propio: se usa en lugar del dibujo
        try {
            BufferedImage rojo = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    rojo.setRGB(x, y, 0xFFFF0000);
                }
            }
            ImageIO.write(rojo, "png", new File(carpeta, "lobo.png"));
            GestorSprites conArchivo = new GestorSprites(carpeta.getPath());
            comprobar("sprites: detecta el .png", conArchivo.tieneArchivo("lobo"));
            BufferedImage leida = conArchivo.obtener("lobo", 2);
            comprobar("sprites: usa el .png (pixel rojo)", leida.getRGB(5, 5) == 0xFFFF0000 && leida.getWidth() == 32);

            // .png dañado: vuelve al dibujo procedural
            escribirArchivo(new File(carpeta, "brujo.png").getPath(), "no soy una imagen");
            BufferedImage danado = new GestorSprites(carpeta.getPath()).obtener("brujo", 2);
            comprobar("sprites: un .png danado usa el respaldo", danado.getWidth() == 32 && danado.getRGB(0, 0) >>> 24 == 0);
        } catch (java.io.IOException e) {
            comprobar("no se pudo crear el .png de prueba", false);
        }
    }

    // Pantallas, manejadas con el motor sin ventana
    private static void pruebasPantallas() {
        Recursos recursos = new Recursos("datos", "assets");
        comprobar("recursos: se cargaron los eventos", recursos.getEventos().tamanio() >= 9);

        // Menú -> mapa
        Motor motor = new Motor(1024, 640);
        motor.iniciar(new EscenaMenu(recursos));
        motor.simular(0.1);
        motor.capturar();
        comprobar("pantalla: arranca en el menú", nombreEscena(motor).equals("EscenaMenu"));
        motor.getEntrada().presionarTecla(KeyEvent.VK_ENTER);
        motor.simular(0.1);
        motor.getEntrada().soltarTecla(KeyEvent.VK_ENTER);
        motor.simular(0.1);
        comprobar("pantalla: ENTER en 'Nueva partida' abre el mapa", nombreEscena(motor).equals("EscenaMapa"));
        motor.capturar();

        // Mapa: elegir y avanzar
        Partida p = Partida.nueva(555, recursos.getEventos());
        EscenaMapa mapa = new EscenaMapa(recursos, p, false);
        motor = new Motor(1024, 640);
        motor.iniciar(mapa);
        motor.simular(0.1);
        int primero = mapa.getSeleccion();
        pulsar(motor, KeyEvent.VK_DOWN);
        comprobar("mapa: la flecha abajo cambia la selección", mapa.getSeleccion() != primero);
        pulsar(motor, KeyEvent.VK_H);
        comprobar("mapa: H abre el diario", mapa.estaMostrandoDiario());
        motor.capturar();
        pulsar(motor, KeyEvent.VK_H);
        comprobar("mapa: H lo cierra", !mapa.estaMostrandoDiario());
        pulsar(motor, KeyEvent.VK_ENTER);
        motor.simular(0.1);
        String escena = nombreEscena(motor);
        comprobar("mapa: ENTER abre combate o evento", escena.equals("EscenaCombate") || escena.equals("EscenaEvento"));
        comprobar("mapa: el héroe se movió por el grafo", p.getNodoActual() != p.getMapa().getIdInicio());
        motor.capturar();

        // Combate por la interfaz
        Partida pc = Partida.nueva(9, recursos.getEventos());
        Enemigo debil = new Esqueleto();
        EscenaCombate escCombate = new EscenaCombate(recursos, pc, debil, false);
        motor = new Motor(1024, 640);
        motor.iniciar(escCombate);
        motor.simular(0.2);
        motor.capturar();
        comprobar("combate (pantalla): empieza eligiendo", escCombate.getCombate().getEstado() == Combate.ELECCION);
        pulsar(motor, KeyEvent.VK_1);
        comprobar("combate (pantalla): la tecla 1 inicia la barra", escCombate.getCombate().getEstado() == Combate.APUNTANDO);
        motor.simular(0.3);
        motor.capturar();
        pulsar(motor, KeyEvent.VK_SPACE);
        comprobar("combate (pantalla): ESPACIO frena la barra", escCombate.getCombate().getEstado() == Combate.RESULTADO);
        motor.simular(0.6);
        pulsar(motor, KeyEvent.VK_ENTER);
        comprobar("combate (pantalla): ENTER continúa", escCombate.getCombate().getEstado() != Combate.RESULTADO);
        motor.simular(1.5);
        comprobar("combate (pantalla): el enemigo actúa solo tras una espera", escCombate.getCombate().getRondas() >= 1);

        // Evento por la interfaz
        Partida pe = Partida.nueva(3, recursos.getEventos());
        EscenaEvento escEvento = new EscenaEvento(recursos, pe, pe.crearEventoCofre(), true);
        motor = new Motor(1024, 640);
        motor.iniciar(escEvento);
        motor.simular(0.5);
        motor.capturar();
        pulsar(motor, KeyEvent.VK_3);
        comprobar("evento (pantalla): la tecla 3 elige la poción y pasa al resultado", escEvento.getFase() == 1);
        comprobar("evento (pantalla): se anotó en el diario", pe.getUltimosRegistros(1).obtener(0).getTexto().contains("poción"));
        motor.capturar();
        pulsar(motor, KeyEvent.VK_ENTER);
        motor.simular(0.1);
        comprobar("evento (pantalla): ENTER vuelve al mapa", nombreEscena(motor).equals("EscenaMapa"));

        // Fin
        motor = new Motor(1024, 640);
        motor.iniciar(new EscenaFin(recursos, pe, true));
        motor.simular(0.2);
        motor.capturar();
        motor.simular(1.0);
        pulsar(motor, KeyEvent.VK_ENTER);
        motor.simular(0.1);
        comprobar("fin: ENTER vuelve al menú", nombreEscena(motor).equals("EscenaMenu"));
    }

    private static void pulsar(Motor motor, int tecla) {
        motor.getEntrada().presionarTecla(tecla);
        motor.simular(0.02);
        motor.getEntrada().soltarTecla(tecla);
        motor.simular(0.02);
    }

    private static String nombreEscena(Motor motor) {
        return motor.getEscenaActual().getClass().getSimpleName();
    }

    // Aventura completa automática: un "jugador robot" recorre el mapa usando solo la lógica (sin pantallas).
    // Sirve para comprobar que nunca se traba y para ver qué tan equilibrado está el juego.
    private static void pruebasAventuraCompleta() {
        ListaEnlazada<EventoNarrativo> eventos = new GestorArchivos("datos").cargarEventos();
        int victorias = 0;
        int partidas = 60;
        boolean ningunaSeTraba = true;
        for (int semilla = 0; semilla < partidas; semilla++) {
            int resultado = jugarRobot(semilla, eventos);
            if (resultado < 0) {
                ningunaSeTraba = false;
            }
            if (resultado == 1) {
                victorias++;
            }
        }
        System.out.println("(el robot ganó " + victorias + " de " + partidas + " aventuras)");
        comprobar("aventura: todas las partidas terminan (sin trabarse)", ningunaSeTraba);
        comprobar("aventura: el juego no es imposible (el robot gana alguna)", victorias > 0);
        comprobar("aventura: el juego no es trivial (el robot no gana todas)", victorias < partidas);
    }

    // Devuelve 1 si ganó, 0 si murió, -1 si algo se trabó
    private static int jugarRobot(int semilla, ListaEnlazada<EventoNarrativo> eventos) {
        Partida p = Partida.nueva(semilla, eventos);
        Jugador j = p.getJugador();
        java.util.Random azar = new java.util.Random(semilla);
        for (int paso = 0; paso < 40; paso++) {
            ListaEnlazada<Integer> vecinos = p.getAdyacentes();
            if (vecinos.estaVacia()) {
                return -1;
            }
            // El robot prefiere cofres y eventos antes que combates
            int elegido = vecinos.obtener(0);
            for (int i = 0; i < vecinos.tamanio(); i++) {
                TipoNodo t = p.getMapa().getNodo(vecinos.obtener(i)).getTipo();
                if (t == TipoNodo.COFRE || t == TipoNodo.EVENTO || t == TipoNodo.JEFE) {
                    elegido = vecinos.obtener(i);
                    break;
                }
            }
            p.moverA(elegido);
            NodoMapa nodo = p.getNodoActualCompleto();
            Enemigo enemigo = null;
            if (nodo.getTipo() == TipoNodo.COMBATE || nodo.getTipo() == TipoNodo.JEFE) {
                enemigo = p.crearEnemigoDeNodo(nodo);
            } else {
                EventoNarrativo ev = nodo.getTipo() == TipoNodo.COFRE ? p.crearEventoCofre() : p.crearEventoDeNodo(nodo);
                OpcionEvento mejor = null;
                for (int i = 0; i < ev.cantidadOpciones(); i++) {
                    OpcionEvento o = ev.getOpcion(i);
                    if (o.puedeElegir(j) && (mejor == null || azar.nextInt(2) == 0)) {
                        mejor = o;
                    }
                }
                if (mejor == null) {
                    return -1;
                }
                mejor.aplicar(j);
                if (mejor.getEmboscada() != null) {
                    enemigo = FabricaEnemigos.crearPorNombre(mejor.getEmboscada(), nodo.getPiso());
                }
            }
            if (enemigo != null) {
                boolean sobrevive = peleaRobot(p, enemigo, nodo.getTipo() == TipoNodo.JEFE, azar);
                if (!sobrevive) {
                    return 0;
                }
                if (nodo.getTipo() == TipoNodo.JEFE) {
                    return 1;
                }
            }
        }
        return -1;
    }

    // Pelea automática: cura si está bajo, usa el golpe potente si puede, si no ataca.
    // El robot frena la barra con una puntería razonable (a veces falla).
    private static boolean peleaRobot(Partida p, Enemigo enemigo, boolean esJefe, java.util.Random azar) {
        Jugador j = p.getJugador();
        Combate c = new Combate(j, enemigo, esJefe);
        for (int pasos = 0; pasos < 400 && !c.haTerminado(); pasos++) {
            int estado = c.getEstado();
            if (estado == Combate.ELECCION) {
                int habilidad = 0;
                if (j.getVida() < j.getVidaMaxima() / 2 && j.conoceHabilidad("CURACION") && j.puedePagar(4)) {
                    habilidad = indiceDe(j, "CURACION");
                } else if (j.conoceHabilidad("GOLPE") && j.puedePagar(5)) {
                    habilidad = indiceDe(j, "GOLPE");
                }
                c.elegirHabilidad(habilidad);
            } else if (estado == Combate.APUNTANDO) {
                double error = (azar.nextDouble() - 0.5) * 2 * 130;
                c.getBarra().setPosicion(Math.max(0, Math.min(600, 300 + error)));
                c.detenerBarra();
            } else {
                c.continuar();
            }
        }
        if (c.getEstado() == Combate.VICTORIA) {
            p.sumarDerrotado();
            return true;
        }
        return c.getEstado() == Combate.HUIDA;
    }

    private static int indiceDe(Jugador j, String codigo) {
        for (int i = 0; i < j.cantidadHabilidades(); i++) {
            if (j.getHabilidad(i).getCodigo().equals(codigo)) {
                return i;
            }
        }
        return 0;
    }
}

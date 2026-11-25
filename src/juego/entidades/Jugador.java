package juego.entidades;

import estructuras.ListaEnlazada;

// El héroe humano. Guarda su clase, atributos, vida, maná, energía, oro, su arma, sus puños y
// su repertorio de HECHIZOS (máximo 4). Las fórmulas son simples:
//   Fuerza        -> vida máxima y daño físico
//   Destreza      -> defensa, energía máxima y minijuegos de combate
//   Inteligencia  -> maná máximo, potencia mágica y curación
// Recursos: el MANÁ se gasta en hechizos; la ENERGÍA se gasta en armas y puños y se recupera cada ronda.
public final class Jugador {

    // Constantes
    public static final int FUERZA = 0;
    public static final int DESTREZA = 1;
    public static final int INTELIGENCIA = 2;
    public static final int MAX_HABILIDADES = 4;
    private static final int ATRIBUTO_MAXIMO = 20;
    public static final int ENERGIA_POR_RONDA = 3;

    // Atributos
    private final Clase clase;
    private int fuerza;
    private int destreza;
    private int inteligencia;
    private int vida;
    private int mana;
    private int energia;
    private int oro;
    private boolean guardia;
    private final EstadoVeneno veneno = new EstadoVeneno();
    private final HabilidadArma arma;
    private final HabilidadPunos punos = new HabilidadPunos();
    private final ListaEnlazada<Habilidad> habilidades = new ListaEnlazada<>();

    // Constructor de una partida nueva con la clase elegida: atributos de la clase, todo lleno
    // y un solo hechizo (el de la clase)
    public Jugador(Clase clase) {
        this(clase, clase.getFuerza(), clase.getDestreza(), clase.getInteligencia(), 0, 0, 0);
        this.vida = getVidaMaxima();
        this.mana = getManaMaximo();
        habilidades.agregar(CatalogoHabilidades.crear(clase.getHechizoInicial()));
    }

    // Héroe por defecto: el druida (atributos 3/3/3)
    public Jugador() {
        this(Clase.DRUIDA);
    }

    // Constructor para cargar una partida: sin hechizos (se agregan después)
    public Jugador(Clase clase, int fuerza, int destreza, int inteligencia, int vida, int mana, int oro) {
        if (clase == null) {
            throw new IllegalArgumentException("el héroe necesita una clase");
        }
        validarAtributo(fuerza, "fuerza");
        validarAtributo(destreza, "destreza");
        validarAtributo(inteligencia, "inteligencia");
        this.clase = clase;
        this.arma = clase.crearArma();
        this.fuerza = fuerza;
        this.destreza = destreza;
        this.inteligencia = inteligencia;
        this.vida = limitar(vida, 0, getVidaMaxima());
        this.mana = limitar(mana, 0, getManaMaximo());
        this.energia = getEnergiaMaxima();
        this.oro = Math.max(0, oro);
    }

    // Igual, sin clase: usa la de por defecto (lo usan las pruebas)
    public Jugador(int fuerza, int destreza, int inteligencia, int vida, int mana, int oro) {
        this(Clase.DRUIDA, fuerza, destreza, inteligencia, vida, mana, oro);
    }

    // Estadísticas derivadas (las fórmulas del juego)
    public int getVidaMaxima() {
        return 20 + 5 * fuerza;
    }

    public int getManaMaximo() {
        return 6 + 3 * inteligencia;
    }

    public int getEnergiaMaxima() {
        return 6 + 2 * destreza;
    }

    public int getDefensa() {
        return destreza / 2;
    }

    public int getDanioFisico() {
        return 2 + 2 * fuerza;
    }

    public int getPotenciaMagica() {
        return 3 + 3 * inteligencia;
    }

    public int getPotenciaCuracion() {
        return 6 + 3 * inteligencia;
    }

    // Bonus de la barra: más Destreza = zona crítica más ancha y cursor más lento
    public int getBonusZonaCritica() {
        return 4 * destreza;
    }

    public double getFactorVelocidadBarra() {
        return Math.max(0.5, 1.0 - 0.03 * destreza);
    }

    // Combate
    // Devuelve el daño realmente recibido (la defensa lo reduce, pero siempre entra al menos 1)
    public int recibirDanio(int danioBruto) {
        if (danioBruto < 0) {
            throw new IllegalArgumentException("el daño no puede ser negativo: " + danioBruto);
        }
        if (guardia) {
            danioBruto = danioBruto / 2;
        }
        int real = Math.max(1, danioBruto - getDefensa());
        vida = Math.max(0, vida - real);
        return real;
    }

    // Veneno: se aplica al terminar el turno del héroe
    public void envenenar(int turnos, int danio) {
        veneno.aplicar(turnos, danio);
    }

    // Devuelve el daño que hizo el veneno en este turno (0 si no estaba envenenado)
    public int aplicarVeneno() {
        int danio = veneno.pasarTurno();
        vida = Math.max(0, vida - danio);
        return danio;
    }

    public boolean estaEnvenenado() {
        return veneno.estaActivo();
    }

    // Guardia: mientras dura la ronda, los golpes recibidos valen la mitad
    public void setGuardia(boolean activa) {
        guardia = activa;
    }

    public boolean estaEnGuardia() {
        return guardia;
    }

    // Al empezar un combate: energía llena y sin estados de la pelea anterior
    public void prepararCombate() {
        energia = getEnergiaMaxima();
        guardia = false;
        veneno.limpiar();
    }

    // Devuelve cuánta vida se recuperó realmente
    public int curar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("la curación no puede ser negativa: " + cantidad);
        }
        int antes = vida;
        vida = Math.min(getVidaMaxima(), vida + cantidad);
        return vida - antes;
    }

    public boolean estaMuerto() {
        return vida <= 0;
    }

    // Cambios por eventos: la vida nunca baja de 1 (un evento no mata)
    public void cambiarVida(int delta) {
        vida = limitar(vida + delta, 1, getVidaMaxima());
    }

    public void cambiarMana(int delta) {
        mana = limitar(mana + delta, 0, getManaMaximo());
    }

    public void cambiarOro(int delta) {
        oro = Math.max(0, oro + delta);
    }

    public boolean puedePagar(int costoMana) {
        return mana >= costoMana;
    }

    public boolean puedePagarEnergia(int costoEnergia) {
        return energia >= costoEnergia;
    }

    public void gastarEnergia(int cantidad) {
        if (cantidad < 0 || cantidad > energia) {
            throw new IllegalArgumentException("energía insuficiente: se pide " + cantidad + " y hay " + energia);
        }
        energia = energia - cantidad;
    }

    public void recuperarEnergia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("la energía a recuperar no puede ser negativa: " + cantidad);
        }
        energia = Math.min(getEnergiaMaxima(), energia + cantidad);
    }

    public void gastarMana(int cantidad) {
        if (cantidad < 0 || cantidad > mana) {
            throw new IllegalArgumentException("maná insuficiente: se pide " + cantidad + " y hay " + mana);
        }
        mana = mana - cantidad;
    }

    // Atributos: subir uno también sube el máximo correspondiente
    public void mejorarAtributo(int cual, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("la mejora debe ser positiva: " + cantidad);
        }
        if (cual == FUERZA) {
            fuerza = Math.min(ATRIBUTO_MAXIMO, fuerza + cantidad);
            vida = Math.min(getVidaMaxima(), vida + 5 * cantidad);
        } else if (cual == DESTREZA) {
            destreza = Math.min(ATRIBUTO_MAXIMO, destreza + cantidad);
            energia = Math.min(getEnergiaMaxima(), energia + 2 * cantidad);
        } else if (cual == INTELIGENCIA) {
            inteligencia = Math.min(ATRIBUTO_MAXIMO, inteligencia + cantidad);
            mana = Math.min(getManaMaximo(), mana + 3 * cantidad);
        } else {
            throw new IllegalArgumentException("atributo desconocido: " + cual);
        }
    }

    // Repertorio de hechizos (lista enlazada, máximo 4)
    public boolean aprenderHabilidad(Habilidad nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("la habilidad no puede ser nula");
        }
        if (conoceHabilidad(nueva.getCodigo()) || habilidades.tamanio() >= MAX_HABILIDADES) {
            return false;
        }
        habilidades.agregar(nueva);
        return true;
    }

    public boolean conoceHabilidad(String codigo) {
        for (int i = 0; i < habilidades.tamanio(); i++) {
            if (habilidades.obtener(i).getCodigo().equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    public Habilidad getHabilidad(int indice) {
        return habilidades.obtener(indice);
    }

    public int cantidadHabilidades() {
        return habilidades.tamanio();
    }

    // Código del primer hechizo del catálogo que todavía no se conoce.
    // Devuelve null si no hay lugar (ya tiene 4) o si ya los conoce todos.
    public String proximaHabilidadPorAprender() {
        if (habilidades.tamanio() >= MAX_HABILIDADES) {
            return null;
        }
        for (int i = 0; i < CatalogoHabilidades.CANTIDAD; i++) {
            String codigo = CatalogoHabilidades.codigoPorOrden(i);
            if (!conoceHabilidad(codigo)) {
                return codigo;
            }
        }
        return null;
    }

    // Getters
    public Clase getClase() {
        return clase;
    }

    public HabilidadArma getArma() {
        return arma;
    }

    public HabilidadPunos getPunos() {
        return punos;
    }

    public int getEnergia() {
        return energia;
    }

    public int getFuerza() {
        return fuerza;
    }

    public int getDestreza() {
        return destreza;
    }

    public int getInteligencia() {
        return inteligencia;
    }

    public int getVida() {
        return vida;
    }

    public int getMana() {
        return mana;
    }

    public int getOro() {
        return oro;
    }

    // Auxiliares privados
    private static void validarAtributo(int valor, String nombre) {
        if (valor < 1 || valor > ATRIBUTO_MAXIMO) {
            throw new IllegalArgumentException(nombre + " debe estar entre 1 y " + ATRIBUTO_MAXIMO + ": " + valor);
        }
    }

    private static int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}

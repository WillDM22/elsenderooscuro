package juego.entidades;

import estructuras.ListaEnlazada;

// El héroe humano. Guarda atributos, vida, maná, oro y su repertorio de habilidades (máximo 4).
// Las fórmulas son simples:
//   Fuerza        -> vida máxima y daño físico
//   Destreza      -> defensa y minijuego de la barra
//   Inteligencia  -> maná máximo, potencia mágica y curación
public final class Jugador {

    // Constantes
    public static final int FUERZA = 0;
    public static final int DESTREZA = 1;
    public static final int INTELIGENCIA = 2;
    public static final int MAX_HABILIDADES = 4;
    private static final int ATRIBUTO_INICIAL = 3;
    private static final int ATRIBUTO_MAXIMO = 20;

    // Atributos
    private int fuerza;
    private int destreza;
    private int inteligencia;
    private int vida;
    private int mana;
    private int oro;
    private final ListaEnlazada<Habilidad> habilidades = new ListaEnlazada<>();

    // Constructor de una partida nueva: atributos 3/3/3, vida y maná llenos, solo sabe atacar
    public Jugador() {
        this(ATRIBUTO_INICIAL, ATRIBUTO_INICIAL, ATRIBUTO_INICIAL, 0, 0, 0);
        this.vida = getVidaMaxima();
        this.mana = getManaMaximo();
        habilidades.agregar(new HabilidadAtaque());
    }

    // Constructor para cargar una partida: sin habilidades (se agregan después)
    public Jugador(int fuerza, int destreza, int inteligencia, int vida, int mana, int oro) {
        validarAtributo(fuerza, "fuerza");
        validarAtributo(destreza, "destreza");
        validarAtributo(inteligencia, "inteligencia");
        this.fuerza = fuerza;
        this.destreza = destreza;
        this.inteligencia = inteligencia;
        this.vida = limitar(vida, 0, getVidaMaxima());
        this.mana = limitar(mana, 0, getManaMaximo());
        this.oro = Math.max(0, oro);
    }

    // Estadísticas derivadas (las fórmulas del juego)
    public int getVidaMaxima() {
        return 20 + 5 * fuerza;
    }

    public int getManaMaximo() {
        return 6 + 3 * inteligencia;
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
        int real = Math.max(1, danioBruto - getDefensa());
        vida = Math.max(0, vida - real);
        return real;
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
        } else if (cual == INTELIGENCIA) {
            inteligencia = Math.min(ATRIBUTO_MAXIMO, inteligencia + cantidad);
            mana = Math.min(getManaMaximo(), mana + 3 * cantidad);
        } else {
            throw new IllegalArgumentException("atributo desconocido: " + cual);
        }
    }

    // Repertorio de habilidades (lista enlazada, máximo 4)
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

    // Código de la primera habilidad del catálogo que todavía no se conoce (null si ya las tiene todas)
    public String proximaHabilidadPorAprender() {
        for (int i = 0; i < CatalogoHabilidades.CANTIDAD; i++) {
            String codigo = CatalogoHabilidades.codigoPorOrden(i);
            if (!conoceHabilidad(codigo)) {
                return codigo;
            }
        }
        return null;
    }

    // Getters
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

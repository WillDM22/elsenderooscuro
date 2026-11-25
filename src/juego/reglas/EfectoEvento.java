package juego.reglas;

import juego.entidades.CatalogoHabilidades;
import juego.entidades.Jugador;

// Un efecto concreto de la opción de un evento: "+5 de vida", "-10 de oro", "aprendes una habilidad"...
public class EfectoEvento {

    // Tipos de efecto
    public enum Tipo {
        VIDA, MANA, ORO, FUERZA, DESTREZA, INTELIGENCIA, HABILIDAD, COMBATE
    }

    // Atributos
    private final Tipo tipo;
    private final int valor;
    private final String texto;

    // Constructor: "valor" para los numéricos, "texto" para HABILIDAD (código) y COMBATE (nombre del enemigo)
    public EfectoEvento(Tipo tipo, int valor, String texto) {
        if (tipo == null) {
            throw new IllegalArgumentException("el efecto necesita un tipo");
        }
        if (tipo == Tipo.HABILIDAD && !CatalogoHabilidades.esValido(texto)) {
            throw new IllegalArgumentException("habilidad desconocida: " + texto);
        }
        if (tipo == Tipo.COMBATE && (texto == null || texto.isEmpty())) {
            throw new IllegalArgumentException("el combate necesita el nombre del enemigo");
        }
        this.tipo = tipo;
        this.valor = valor;
        this.texto = texto;
    }

    // Aplica el efecto al jugador. Devuelve una descripción corta ("+5 vida") o null si no hay nada que mostrar.
    public String aplicar(Jugador jugador) {
        if (tipo == Tipo.VIDA) {
            jugador.cambiarVida(valor);
            return signo(valor) + " vida";
        }
        if (tipo == Tipo.MANA) {
            jugador.cambiarMana(valor);
            return signo(valor) + " maná";
        }
        if (tipo == Tipo.ORO) {
            jugador.cambiarOro(valor);
            return signo(valor) + " oro";
        }
        if (tipo == Tipo.FUERZA || tipo == Tipo.DESTREZA || tipo == Tipo.INTELIGENCIA) {
            return aplicarAtributo(jugador);
        }
        if (tipo == Tipo.HABILIDAD) {
            return aplicarHabilidad(jugador);
        }
        return null;
    }

    private String aplicarAtributo(Jugador jugador) {
        int cual = Jugador.FUERZA;
        String nombre = "Fuerza";
        if (tipo == Tipo.DESTREZA) {
            cual = Jugador.DESTREZA;
            nombre = "Destreza";
        } else if (tipo == Tipo.INTELIGENCIA) {
            cual = Jugador.INTELIGENCIA;
            nombre = "Inteligencia";
        }
        if (valor > 0) {
            jugador.mejorarAtributo(cual, valor);
        }
        return signo(valor) + " " + nombre;
    }

    private String aplicarHabilidad(Jugador jugador) {
        if (jugador.conoceHabilidad(texto)) {
            return "ya conocías esa habilidad";
        }
        boolean aprendida = jugador.aprenderHabilidad(CatalogoHabilidades.crear(texto));
        if (!aprendida) {
            return "no tienes lugar para otra habilidad";
        }
        return "aprendes " + CatalogoHabilidades.crear(texto).getNombre();
    }

    private String signo(int numero) {
        if (numero >= 0) {
            return "+" + numero;
        }
        return String.valueOf(numero);
    }

    // Getters
    public Tipo getTipo() {
        return tipo;
    }

    public int getValor() {
        return valor;
    }

    public String getTexto() {
        return texto;
    }
}

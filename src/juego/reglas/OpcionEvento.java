package juego.reglas;

import estructuras.ListaEnlazada;
import juego.entidades.Jugador;

// Una de las opciones (2 o 3) que se le ofrecen al jugador en un evento.
// Guarda el texto del botón, el relato del resultado y la LISTA de efectos que produce.
public class OpcionEvento {

    // Atributos
    private final String texto;
    private final String resultado;
    private final ListaEnlazada<EfectoEvento> efectos;

    // Constructor
    public OpcionEvento(String texto, String resultado, ListaEnlazada<EfectoEvento> efectos) {
        if (texto == null || texto.isEmpty()) {
            throw new IllegalArgumentException("la opción necesita un texto");
        }
        if (efectos == null) {
            throw new IllegalArgumentException("la lista de efectos no puede ser nula");
        }
        this.texto = texto;
        this.resultado = resultado == null ? "" : resultado;
        this.efectos = efectos;
    }

    // Una opción que gasta oro solo se puede elegir si alcanza el oro
    public boolean puedeElegir(Jugador jugador) {
        int costo = 0;
        for (int i = 0; i < efectos.tamanio(); i++) {
            EfectoEvento efecto = efectos.obtener(i);
            if (efecto.getTipo() == EfectoEvento.Tipo.ORO && efecto.getValor() < 0) {
                costo = costo - efecto.getValor();
            }
        }
        return jugador.getOro() >= costo;
    }

    // Aplica todos los efectos. Devuelve un resumen como "+10 oro, -3 vida".
    public String aplicar(Jugador jugador) {
        String resumen = "";
        for (int i = 0; i < efectos.tamanio(); i++) {
            String parte = efectos.obtener(i).aplicar(jugador);
            if (parte != null) {
                if (!resumen.isEmpty()) {
                    resumen = resumen + ", ";
                }
                resumen = resumen + parte;
            }
        }
        return resumen;
    }

    // Nombre del enemigo si la opción provoca una emboscada; null si no
    public String getEmboscada() {
        for (int i = 0; i < efectos.tamanio(); i++) {
            EfectoEvento efecto = efectos.obtener(i);
            if (efecto.getTipo() == EfectoEvento.Tipo.COMBATE) {
                return efecto.getTexto();
            }
        }
        return null;
    }

    // Getters
    public String getTexto() {
        return texto;
    }

    public String getResultado() {
        return resultado;
    }

    public int cantidadEfectos() {
        return efectos.tamanio();
    }

    public EfectoEvento getEfecto(int indice) {
        return efectos.obtener(indice);
    }
}

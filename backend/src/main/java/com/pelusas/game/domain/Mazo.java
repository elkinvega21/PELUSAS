package com.pelusas.game.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Montón de polvo (mazo) de 110 cartas:
 * valores 1-5 → 13 copias c/u (65) y valores 6-10 → 9 copias c/u (45).
 */
public class Mazo {

    public static final int TOTAL_CARTAS = 110;
    public static final int COPIAS_VALORES_BAJOS = 13; // valores 1 a 5
    public static final int COPIAS_VALORES_ALTOS = 9;  // valores 6 a 10
    public static final int ULTIMO_VALOR_BAJO = 5;

    private final List<Carta> cartas;

    private Mazo(List<Carta> cartas) {
        this.cartas = cartas;
    }

    /** Crea un mazo completo (110 cartas) sin barajar. */
    public static Mazo completo() {
        List<Carta> cartas = new ArrayList<>(TOTAL_CARTAS);
        for (int valor = Carta.VALOR_MINIMO; valor <= Carta.VALOR_MAXIMO; valor++) {
            int copias = copiasDelValor(valor);
            for (int i = 0; i < copias; i++) {
                cartas.add(new Carta(valor));
            }
        }
        return new Mazo(cartas);
    }

    /** Mazo con cartas específicas; se sacarán en el orden indicado (útil para pruebas). */
    static Mazo deCartas(List<Carta> enOrdenDeExtraccion) {
        List<Carta> cartas = new ArrayList<>(enOrdenDeExtraccion);
        Collections.reverse(cartas);
        return new Mazo(cartas);
    }

    /** Cantidad de copias que existen de un valor en el mazo completo. */
    public static int copiasDelValor(int valor) {
        new Carta(valor); // valida el rango
        return valor <= ULTIMO_VALOR_BAJO ? COPIAS_VALORES_BAJOS : COPIAS_VALORES_ALTOS;
    }

    /** Mezcla las cartas. El {@link Random} se recibe para poder probarlo de forma determinista. */
    void barajar(Random random) {
        Collections.shuffle(cartas, random);
    }

    /** Saca la carta superior del mazo. Solo {@link Partida} puede hacerlo, para que pasen por las reglas. */
    Carta sacar() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío");
        }
        return cartas.remove(cartas.size() - 1);
    }

    /** Copia de las cartas en el orden en que se sacarán (la primera es la que sale primero). */
    List<Carta> enOrdenDeExtraccion() {
        List<Carta> copia = new ArrayList<>(cartas);
        Collections.reverse(copia);
        return copia;
    }

    public int restantes() {
        return cartas.size();
    }

    public boolean estaVacio() {
        return cartas.isEmpty();
    }

    /** Vista de solo lectura del contenido actual (la última carta es la que se saca primero). */
    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }
}

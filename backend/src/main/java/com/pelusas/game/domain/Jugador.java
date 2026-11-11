package com.pelusas.game.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Jugador de una partida.
 * - cartasEnMesa: cartas boca arriba delante del jugador (en riesgo).
 * - montonPuntos: cartas guardadas y seguras.
 *
 * Los métodos que modifican el estado son package-private: solo {@link Partida}
 * puede alterarlos, para que todas las reglas pasen por ella.
 */
public class Jugador {

    public static final int MAX_CARTAS_ROBO = 3;

    private final String nombre;
    private final List<Carta> cartasEnMesa = new ArrayList<>();
    private final List<Carta> montonPuntos = new ArrayList<>();

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Carta> getCartasEnMesa() {
        return Collections.unmodifiableList(cartasEnMesa);
    }

    public List<Carta> getMontonPuntos() {
        return Collections.unmodifiableList(montonPuntos);
    }

    /** Cartas en mesa agrupadas por valor: valor → cantidad, ordenado por valor. */
    public Map<Integer, Integer> getMesaAgrupada() {
        Map<Integer, Integer> grupos = new TreeMap<>();
        for (Carta carta : cartasEnMesa) {
            grupos.merge(carta.valor(), 1, Integer::sum);
        }
        return Collections.unmodifiableMap(grupos);
    }

    /** Cuántas cartas de ese valor hay en el montón de puntos. */
    public int cantidadEnMonton(int valor) {
        return (int) montonPuntos.stream().filter(c -> c.valor() == valor).count();
    }

    /** Suma de los valores nominales del montón de puntos. */
    public int puntos() {
        return montonPuntos.stream().mapToInt(Carta::valor).sum();
    }

    /** Indica si el jugador ya tiene delante alguna carta de ese valor. */
    public boolean tieneEnMesa(int valor) {
        return cartasEnMesa.stream().anyMatch(c -> c.valor() == valor);
    }

    void agregarAlMonton(Carta carta) {
        montonPuntos.add(carta);
    }

    void recibirEnMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    /**
     * Quita de la mesa como máximo 3 cartas de ese valor.
     * Si el jugador tiene más de 3 cartas de ese valor, solo se pueden robar 3 de esas (máximo 3).
     */
    List<Carta> retirarDeMesa(int valor) {
        return retirarDeMesa(valor, MAX_CARTAS_ROBO);
    }

    List<Carta> retirarDeMesa(int valor, int maximo) {
        List<Carta> retiradas = new ArrayList<>();
        Iterator<Carta> it = cartasEnMesa.iterator();
        while (it.hasNext() && retiradas.size() < maximo) {
            Carta c = it.next();
            if (c.valor() == valor) {
                retiradas.add(c);
                it.remove();
            }
        }
        return retiradas;
    }

    /** Pasa todas las cartas de la mesa al montón de puntos. Devuelve cuántas guardó. */
    int guardarMesa() {
        int guardadas = cartasEnMesa.size();
        montonPuntos.addAll(cartasEnMesa);
        cartasEnMesa.clear();
        return guardadas;
    }

    /** Quita todas las cartas de la mesa (sin guardarlas) y las devuelve. */
    List<Carta> vaciarMesa() {
        List<Carta> retiradas = List.copyOf(cartasEnMesa);
        cartasEnMesa.clear();
        return retiradas;
    }
}

package com.pelusas.game.domain;

import java.util.ArrayList;
import java.util.Collections;
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

    /** Suma de los valores nominales del montón de puntos. */
    public int puntos() {
        return montonPuntos.stream().mapToInt(Carta::valor).sum();
    }

    void recibirEnMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    /** Quita de la mesa todas las cartas de ese valor y las devuelve. */
    List<Carta> retirarDeMesa(int valor) {
        List<Carta> retiradas = cartasEnMesa.stream().filter(c -> c.valor() == valor).toList();
        cartasEnMesa.removeIf(c -> c.valor() == valor);
        return retiradas;
    }

    /** Pasa todas las cartas de la mesa al montón de puntos. Devuelve cuántas guardó. */
    int guardarMesa() {
        int guardadas = cartasEnMesa.size();
        montonPuntos.addAll(cartasEnMesa);
        cartasEnMesa.clear();
        return guardadas;
    }
}
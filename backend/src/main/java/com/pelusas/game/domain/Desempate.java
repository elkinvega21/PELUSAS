package com.pelusas.game.domain;

import java.util.List;

/**
 * Determina el ganador al finalizar la partida.
 *
 * 1. Gana quien tenga más puntos.
 * 2. Si hay empate, se compara la cantidad de cartas de valor 1 en el montón de puntos;
 *    si persiste, las de valor 2, y así sucesivamente hasta el 10.
 *    En cada valor siguen en competencia solo los jugadores con la mayor cantidad.
 * 3. Si tras el valor 10 siguen igualados, todos ellos son ganadores (empate).
 */
final class Desempate {

    private Desempate() {
    }

    static List<Jugador> determinarGanadores(List<Jugador> jugadores) {
        int maxPuntos = jugadores.stream().mapToInt(Jugador::puntos).max().orElse(0);
        List<Jugador> candidatos = jugadores.stream().filter(j -> j.puntos() == maxPuntos).toList();

        for (int valor = Carta.VALOR_MINIMO; valor <= Carta.VALOR_MAXIMO && candidatos.size() > 1; valor++) {
            final int v = valor;
            int maxCantidad = candidatos.stream().mapToInt(j -> j.cantidadEnMonton(v)).max().orElse(0);
            candidatos = candidatos.stream().filter(j -> j.cantidadEnMonton(v) == maxCantidad).toList();
        }
        return candidatos;
    }
}

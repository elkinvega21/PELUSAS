package com.pelusas.game.domain;

/**
 * Resultado final de un jugador.
 *
 * @param nombre            nombre del jugador
 * @param cartasAcumuladas  cantidad de cartas en su montón de puntos al finalizar
 * @param puntos            suma de los valores de esas cartas
 * @param ganador           true si es ganador (puede haber más de uno si el empate persiste)
 */
public record ResultadoJugador(String nombre, int cartasAcumuladas, int puntos, boolean ganador) {
}

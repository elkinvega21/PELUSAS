package com.pelusas.game.domain;

import java.util.List;

/**
 * Resultado de sacar una carta del mazo.
 * @param carta         la carta que salió del mazo
 * @param robadas       las cartas que se tomaron de otros jugadores por tener el mismo valor
 * @param turnoPerdido  si el jugador perdió el turno por repetir valor teniendo >= 3 cartas
 * @param descartadas   las cartas que se descartaron al perder el turno
 */
public record ResultadoSacarCarta(
        Carta carta,
        List<Carta> robadas,
        boolean turnoPerdido,
        List<Carta> descartadas
) {
    public ResultadoSacarCarta(Carta carta, List<Carta> robadas) {
        this(carta, robadas, false, List.of());
    }
}

package com.pelusas.game.domain;

import java.util.List;

/**
 * Resultado de sacar una carta del mazo.
 * @param carta   la carta que salió del mazo
 * @param robadas las cartas que se tomaron de otros jugadores por tener el mismo valor
 */
public record ResultadoSacarCarta(Carta carta, List<Carta> robadas) {
}

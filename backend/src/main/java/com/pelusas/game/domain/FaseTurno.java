package com.pelusas.game.domain;

/**
 * Fase en la que se encuentra el jugador activo dentro de su turno.
 * INICIO: aún no ha sacado carta (puede guardar pelusas o empezar a buscar).
 * BUSCANDO: ya sacó al menos una carta (puede seguir sacando o plantarse).
 */
public enum FaseTurno {
    INICIO,
    BUSCANDO
}

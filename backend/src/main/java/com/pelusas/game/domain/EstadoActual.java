package com.pelusas.game.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vista completa del estado de una partida, calculada por el backend.
 * El frontend solo la muestra: no decide qué acciones están permitidas ni quién ganó.
 * No incluye el contenido ni el orden del mazo, solo cuántas cartas quedan.
 *
 * @param jugadorActual   nombre del jugador al que le toca
 * @param cartasRestantes cartas que quedan en el montón de polvo
 * @param cartasDescartadas cartas fuera de juego por pérdidas de turno
 * @param acciones        qué acciones puede hacer ahora mismo el jugador actual
 * @param ganadores       nombres de los ganadores (vacío mientras la partida está en curso)
 */
public record EstadoActual(
        UUID id,
        EstadoPartida estado,
        FaseTurno fase,
        String jugadorActual,
        int cartasRestantes,
        int cartasDescartadas,
        List<JugadorEstado> jugadores,
        Acciones acciones,
        List<String> ganadores) {

    /**
     * @param mesaAgrupada  cartas boca arriba delante del jugador agrupadas por valor (valor → cantidad)
     * @param cartasEnMesa  total de cartas boca arriba
     * @param cartasEnMonton cartas en su montón de puntos
     * @param puntos        suma de los valores de su montón de puntos
     */
    public record JugadorEstado(String nombre, Map<Integer, Integer> mesaAgrupada, int cartasEnMesa,
                                int cartasEnMonton, int puntos) {
    }

    public record Acciones(boolean guardarPelusas, boolean sacarCarta, boolean plantarse) {
    }
}

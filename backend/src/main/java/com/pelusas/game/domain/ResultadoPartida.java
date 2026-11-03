package com.pelusas.game.domain;

import java.util.List;

/** Resultado final de una partida: un {@link ResultadoJugador} por cada jugador. */
public record ResultadoPartida(List<ResultadoJugador> jugadores) {

    public List<ResultadoJugador> ganadores() {
        return jugadores.stream().filter(ResultadoJugador::ganador).toList();
    }

    /** true si, tras aplicar el desempate, siguen empatados dos o más jugadores. */
    public boolean hayEmpate() {
        return ganadores().size() > 1;
    }
}

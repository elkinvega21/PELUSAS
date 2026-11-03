package com.pelusas.game.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Foto inmutable del estado completo de una {@link Partida}, pensada para guardarla y
 * reconstruirla ({@link Partida#snapshot()} y {@link Partida#restaurar(PartidaSnapshot)}).
 * No depende de ninguna tecnología de persistencia.
 *
 * @param mazo     cartas del montón de polvo, en el orden en que se van a sacar (la primera sale primero)
 * @param descarte cartas descartadas por pérdidas de turno
 * @param jugadores jugadores en orden de turno
 */
public record PartidaSnapshot(
        UUID id,
        LocalDateTime fechaCreacion,
        EstadoPartida estado,
        FaseTurno fase,
        int indiceJugadorActual,
        List<Carta> mazo,
        List<Carta> descarte,
        List<JugadorSnapshot> jugadores) {

    /**
     * @param mesa   cartas boca arriba delante del jugador
     * @param monton cartas de su montón de puntos
     * @param ganador true si es ganador (solo puede serlo en una partida finalizada)
     */
    public record JugadorSnapshot(String nombre, List<Carta> mesa, List<Carta> monton, boolean ganador) {

        public int puntos() {
            return monton.stream().mapToInt(Carta::valor).sum();
        }

        public int cartasAcumuladas() {
            return monton.size();
        }
    }
}

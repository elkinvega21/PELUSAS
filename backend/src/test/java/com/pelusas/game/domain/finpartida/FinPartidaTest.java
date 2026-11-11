package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de la finalización de la partida (req. 9). Jugadores: Ana (inicia), Luis, Eva. */
class FinPartidaTest {

    private static Carta c(int valor) {
        return new Carta(valor);
    }

    /** Mazo exacto: la partida termina al sacar la última carta. Inicia Ana. */
    private static Partida partida(int... valores) {
        List<Carta> cartas = Arrays.stream(valores).mapToObj(Carta::new).toList();
        return Partida.conMazo(List.of("Ana", "Luis", "Eva"), Mazo.deCartas(cartas), 0);
    }

    private static Jugador jugador(Partida p, int indice) {
        return p.getJugadores().get(indice);
    }

    // ---- cuándo finaliza ----

    @Test
    void laPartidaNoFinalizaMientrasQuedenCartas() {
        Partida p = partida(1, 2);
        p.sacarCarta("Ana");
        assertEquals(EstadoPartida.EN_CURSO, p.getEstado());
        assertTrue(p.getGanadores().isEmpty());
    }

    @Test
    void laPartidaFinalizaAlSacarLaUltimaCarta() {
        Partida p = partida(1, 2);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        assertTrue(p.getMazo().estaVacio());
    }

    // ---- 1. guardado automático ----

    @Test
    void alFinalizarTodosGuardanLasCartasQueTienenDelante() {
        Partida p = partida(5, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
        assertTrue(jugador(p, 1).getCartasEnMesa().isEmpty());
        assertEquals(List.of(c(5)), jugador(p, 0).getMontonPuntos());
        assertEquals(List.of(c(3)), jugador(p, 1).getMontonPuntos());
    }

    @Test
    void tambienGuardaElJugadorQueSacoLaUltimaCartaConCartasRobadas() {
        // Luis saca el 5 que dejó Ana: la roba y, al terminar el mazo, guarda ambas.
        Partida p = partida(5, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        assertEquals(List.of(c(5), c(5)), jugador(p, 1).getMontonPuntos());
        assertTrue(jugador(p, 0).getMontonPuntos().isEmpty());
        assertEquals(10, jugador(p, 1).puntos());
    }

    // ---- 2. suma de puntos ----

    @Test
    void sumaLosValoresDeLosMontonesDePuntos() {
        // Ana: 10 + 4; Luis: 7; Eva: 2.
        Partida p = partida(10, 4, 7, 2);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        assertEquals(14, jugador(p, 0).puntos());
        assertEquals(7, jugador(p, 1).puntos());
        assertEquals(2, jugador(p, 2).puntos());
    }

    @Test
    void lasCartasGuardadasAntesTambienSuman() {
        // Ana guarda su 6 en su segundo turno y luego termina la partida con un 1 delante.
        Partida p = partida(6, 2, 3, 1);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        p.guardarPelusas("Ana");
        p.sacarCarta("Ana");
        assertEquals(7, jugador(p, 0).puntos());
    }

    @Test
    void lasCartasDescartadasNoSuman() {
        // Ana junta 4, 5 y repite 4 con la última carta: pierde y la partida termina sin puntos para ella.
        Partida p = partida(6, 4, 5, 4);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.sacarCarta("Luis");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertTrue(r.turnoPerdido());
        assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        assertEquals(0, jugador(p, 1).puntos());
        assertEquals(6, jugador(p, 0).puntos());
        assertEquals(3, p.getDescarte().size());
    }

    // ---- 3. ganador ----

    @Test
    void ganaElJugadorConMasPuntos() {
        Partida p = partida(10, 4, 7, 2);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        assertEquals(List.of(jugador(p, 0)), p.getGanadores());
    }

    @Test
    void ganaQuienTieneMasPuntosAunqueTengaMenosCartas() {
        // Ana: un 9 (9 puntos). Luis: 3 y 4 (7 puntos con más cartas). Eva: 1.
        Partida p = partida(9, 3, 1, 4);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        p.sacarCarta("Ana");
        assertEquals(List.of(jugador(p, 0)), p.getGanadores());
    }

    // ---- acciones tras finalizar ----

    @Test
    void trasFinalizarNoSePuedeActuar() {
        Partida p = partida(1);
        p.sacarCarta("Ana");
        assertThrows(AccionInvalidaException.class, () -> p.sacarCarta("Ana"));
        assertThrows(AccionInvalidaException.class, () -> p.plantarse("Ana"));
        assertThrows(AccionInvalidaException.class, () -> p.guardarPelusas("Ana"));
        assertThrows(AccionInvalidaException.class, () -> p.sacarCarta("Luis"));
    }

    // ---- resultado ----

    @Test
    void elResultadoNoEstaDisponibleAntesDeFinalizar() {
        Partida p = partida(1, 2);
        assertThrows(AccionInvalidaException.class, p::getResultado);
    }

    @Test
    void elResultadoIncluyeNombreCartasPuntosYGanador() {
        Partida p = partida(10, 4, 7, 2);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");

        ResultadoPartida r = p.getResultado();
        assertEquals(List.of(
                new ResultadoJugador("Ana", 2, 14, true),
                new ResultadoJugador("Luis", 1, 7, false),
                new ResultadoJugador("Eva", 1, 2, false)), r.jugadores());
        assertEquals(List.of(new ResultadoJugador("Ana", 2, 14, true)), r.ganadores());
        assertFalse(r.hayEmpate());
    }

    @Test
    void laListaDeGanadoresNoSePuedeModificar() {
        Partida p = partida(1);
        p.sacarCarta("Ana");
        assertThrows(UnsupportedOperationException.class, () -> p.getGanadores().clear());
    }

    // ---- empate persistente ----

    @Test
    void conTodosSinPuntosHayEmpateEntreTodos() {
        // Ana pierde con 4, 5, 4 en la última carta: nadie suma y los tres empatan.
        Partida p = partida(4, 5, 4);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        assertEquals(3, p.getGanadores().size());
        assertTrue(p.getResultado().hayEmpate());
    }
}

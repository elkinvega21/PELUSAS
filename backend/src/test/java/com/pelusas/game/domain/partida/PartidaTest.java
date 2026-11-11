package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PartidaTest {

    private static Partida nueva(String... nombres) {
        return Partida.crear(Arrays.asList(nombres), new Random(1));
    }

    // ---- creación ----

    @Test
    void creaPartidaConIdYFecha() {
        Partida p = nueva("Ana", "Luis");
        assertNotNull(p.getId());
        assertNotNull(p.getFechaCreacion());
    }

    @Test
    void cadaPartidaTieneIdDistinto() {
        assertNotEquals(nueva("Ana", "Luis").getId(), nueva("Ana", "Luis").getId());
    }

    @Test
    void sobrecargaSinRandomTambienCrea() {
        assertTrue(Partida.crear(List.of("Ana", "Luis")).esEstadoInicialValido());
    }

    // ---- jugadores ----

    @Test
    void defineLosJugadoresEnElOrdenIndicado() {
        Partida p = nueva("Ana", "Luis", "Eva");
        assertEquals(List.of("Ana", "Luis", "Eva"),
                p.getJugadores().stream().map(Jugador::getNombre).toList());
    }

    @Test
    void aceptaDosJugadoresComoMinimo() {
        assertEquals(2, nueva("Ana", "Luis").getJugadores().size());
    }

    @Test
    void rechazaListaNula() {
        assertThrows(IllegalArgumentException.class, () -> Partida.crear(null));
    }

    @Test
    void rechazaUnSoloJugador() {
        assertThrows(IllegalArgumentException.class, () -> nueva("Ana"));
    }

    @Test
    void rechazaListaVacia() {
        assertThrows(IllegalArgumentException.class, () -> Partida.crear(List.of()));
    }

    @Test
    void rechazaNombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> nueva("Ana", null));
    }

    @Test
    void rechazaNombreEnBlanco() {
        assertThrows(IllegalArgumentException.class, () -> nueva("Ana", "   "));
    }

    @Test
    void rechazaNombresRepetidosIgnorandoMayusculasYEspacios() {
        assertThrows(IllegalArgumentException.class, () -> nueva("Ana", " ana "));
    }

    @Test
    void recortaEspaciosDelNombre() {
        assertEquals("Ana", nueva("  Ana ", "Luis").getJugadores().get(0).getNombre());
    }

    @Test
    void laListaDeJugadoresNoSePuedeModificar() {
        Partida p = nueva("Ana", "Luis");
        assertThrows(UnsupportedOperationException.class, () -> p.getJugadores().clear());
    }

    // ---- mazo ----

    @Test
    void inicializaElMazoCon110Cartas() {
        assertEquals(110, nueva("Ana", "Luis").getMazo().restantes());
    }

    @Test
    void elMazoQuedaBarajado() {
        List<Carta> sinBarajar = Mazo.completo().getCartas();
        assertNotEquals(sinBarajar, nueva("Ana", "Luis").getMazo().getCartas());
    }

    // ---- jugador inicial ----

    @Test
    void elJugadorInicialEstaDentroDelRango() {
        Partida p = nueva("Ana", "Luis", "Eva");
        assertTrue(p.getIndiceJugadorActual() >= 0 && p.getIndiceJugadorActual() < 3);
        assertSame(p.getJugadores().get(p.getIndiceJugadorActual()), p.getJugadorActual());
    }

    @Test
    void elJugadorInicialEsDeterministaConLaMismaSemilla() {
        assertEquals(nueva("Ana", "Luis", "Eva").getIndiceJugadorActual(),
                nueva("Ana", "Luis", "Eva").getIndiceJugadorActual());
    }

    @Test
    void cualquierJugadorPuedeSerElInicial() {
        Set<Integer> iniciales = new HashSet<>();
        for (long semilla = 0; semilla < 200; semilla++) {
            iniciales.add(Partida.crear(List.of("Ana", "Luis", "Eva"), new Random(semilla))
                    .getIndiceJugadorActual());
        }
        assertEquals(Set.of(0, 1, 2), iniciales);
    }

    // ---- estado inicial ----

    @Test
    void laPartidaNuevaEstaEnCurso() {
        assertEquals(EstadoPartida.EN_CURSO, nueva("Ana", "Luis").getEstado());
    }

    @Test
    void losJugadoresEmpiezanSinCartasNiPuntos() {
        for (Jugador j : nueva("Ana", "Luis").getJugadores()) {
            assertTrue(j.getCartasEnMesa().isEmpty());
            assertTrue(j.getMontonPuntos().isEmpty());
            assertEquals(0, j.puntos());
        }
    }

    @Test
    void elEstadoInicialEsValido() {
        assertTrue(nueva("Ana", "Luis").esEstadoInicialValido());
    }

    @Test
    void elEstadoInicialDejaDeSerValidoSiSeSacaUnaCarta() {
        Partida p = nueva("Ana", "Luis");
        p.getMazo().sacar();
        assertFalse(p.esEstadoInicialValido());
    }
}

package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Pruebas del turno (req. 7). Jugadores: Ana (inicia), Luis, Eva. */
class TurnoTest {

    private static Carta c(int valor) {
        return new Carta(valor);
    }

    /** Partida con mazo fijo: las cartas salen en el orden indicado. Inicia Ana. */
    private static Partida partida(int... valores) {
        List<Carta> cartas = Arrays.stream(valores).mapToObj(Carta::new).toList();
        return Partida.conMazo(List.of("Ana", "Luis", "Eva"), Mazo.deCartas(cartas), 0);
    }

    private static Jugador jugador(Partida p, int indice) {
        return p.getJugadores().get(indice);
    }

    // ---- validación de turno ----

    @Test
    void laPartidaNuevaEmpiezaEnFaseInicio() {
        assertEquals(FaseTurno.INICIO, partida(1).getFase());
    }

    @Test
    void sacarCartaFueraDeTurnoLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).sacarCarta("Luis"));
    }

    @Test
    void guardarFueraDeTurnoLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).guardarPelusas("Luis"));
    }

    @Test
    void plantarseFueraDeTurnoLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).plantarse("Luis"));
    }

    @Test
    void nombreNuloLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).sacarCarta(null));
    }

    @Test
    void elNombreSeComparaSinImportarMayusculasNiEspacios() {
        Partida p = partida(1);
        assertDoesNotThrow(() -> p.sacarCarta("  ANA "));
    }

    // ---- paso 2: sacar carta ----

    @Test
    void sacarCartaLaDejaVisibleDelanteDelJugador() {
        Partida p = partida(4, 5, 6);
        ResultadoSacarCarta r = p.sacarCarta("Ana");
        assertEquals(c(4), r.carta());
        assertEquals(List.of(c(4)), jugador(p, 0).getCartasEnMesa());
        assertEquals(2, p.getMazo().restantes());
    }

    @Test
    void sacarCartaSinRivalesConCartasNoRobaNada() {
        assertTrue(partida(4).sacarCarta("Ana").robadas().isEmpty());
    }

    @Test
    void sacarCartaPasaLaFaseABuscando() {
        Partida p = partida(4, 5);
        p.sacarCarta("Ana");
        assertEquals(FaseTurno.BUSCANDO, p.getFase());
    }

    @Test
    void sePuedeSeguirSacandoCartasEnElMismoTurno() {
        Partida p = partida(1, 2, 3);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        assertEquals(3, jugador(p, 0).getCartasEnMesa().size());
        assertEquals(0, p.getIndiceJugadorActual());
    }

    @Test
    void sacarConMazoVacioLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida().sacarCarta("Ana"));
    }

    @Test
    void lasCartasDelMismoValorSeAgrupan() {
        Partida p = partida(4, 4);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        assertEquals(Map.of(4, 2), jugador(p, 0).getMesaAgrupada());
    }

    @Test
    void laMesaAgrupadaSeOrdenaPorValorYSeparaLosDistintos() {
        Partida p = partida(7, 2, 7);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        Map<Integer, Integer> grupos = jugador(p, 0).getMesaAgrupada();
        assertEquals(List.of(2, 7), List.copyOf(grupos.keySet()));
        assertEquals(Map.of(2, 1, 7, 2), grupos);
    }

    @Test
    void laMesaAgrupadaNoSePuedeModificar() {
        Jugador j = jugador(partida(1), 0);
        assertThrows(UnsupportedOperationException.class, () -> j.getMesaAgrupada().put(1, 1));
    }

    // ---- plantarse ----

    @Test
    void plantarseDespuesDeSacarPasaElTurnoAlSiguiente() {
        Partida p = partida(4, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        assertEquals(1, p.getIndiceJugadorActual());
        assertEquals("Luis", p.getJugadorActual().getNombre());
        assertEquals(FaseTurno.INICIO, p.getFase());
    }

    @Test
    void plantarseDejaLasCartasDelanteDelJugador() {
        Partida p = partida(4, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        assertEquals(List.of(c(4)), jugador(p, 0).getCartasEnMesa());
    }

    @Test
    void plantarseSinSacarCartaLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).plantarse("Ana"));
    }

    @Test
    void elTurnoVuelveAlPrimerJugadorTrasElUltimo() {
        Partida p = partida(1, 2, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        assertEquals(0, p.getIndiceJugadorActual());
    }

    // ---- paso 1: guardar pelusas ----

    /** Ana, Luis y Eva sacan 1, 2 y 3 y se plantan; vuelve a Ana con 1 carta en mesa. */
    private static Partida rondaCompleta(int... valores) {
        Partida p = partida(valores);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        return p;
    }

    @Test
    void guardarPasaLasCartasDeLaMesaAlMontonDePuntos() {
        Partida p = rondaCompleta(1, 2, 3);
        int guardadas = p.guardarPelusas("Ana");
        Jugador ana = jugador(p, 0);
        assertEquals(1, guardadas);
        assertTrue(ana.getCartasEnMesa().isEmpty());
        assertEquals(List.of(c(1)), ana.getMontonPuntos());
        assertEquals(1, ana.puntos());
    }

    @Test
    void guardarNoCambiaLaFaseYPermiteSacarDespues() {
        Partida p = rondaCompleta(1, 2, 3, 4);
        p.guardarPelusas("Ana");
        assertEquals(FaseTurno.INICIO, p.getFase());
        assertDoesNotThrow(() -> p.sacarCarta("Ana"));
    }

    @Test
    void guardarSinCartasEnMesaLanzaExcepcion() {
        assertThrows(AccionInvalidaException.class, () -> partida(1).guardarPelusas("Ana"));
    }

    @Test
    void guardarDespuesDeSacarCartaLanzaExcepcion() {
        Partida p = rondaCompleta(1, 2, 3, 4);
        p.sacarCarta("Ana");
        assertThrows(AccionInvalidaException.class, () -> p.guardarPelusas("Ana"));
    }

    @Test
    void guardarDosVecesSeguidasLanzaExcepcion() {
        Partida p = rondaCompleta(1, 2, 3);
        p.guardarPelusas("Ana");
        assertThrows(AccionInvalidaException.class, () -> p.guardarPelusas("Ana"));
    }

    @Test
    void lasCartasGuardadasYaNoSePuedenRobar() {
        // Ana guarda su 1, saca 4 y se planta; Luis saca otro 1 y no le roba nada a Ana.
        Partida p = rondaCompleta(1, 2, 3, 4, 1);
        p.guardarPelusas("Ana");
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertTrue(r.robadas().isEmpty());
        assertEquals(List.of(c(1)), jugador(p, 0).getMontonPuntos());
    }

    // ---- paso 3: robar ----

    @Test
    void sacarUnValorQueTieneOtroJugadorLeRobaSusCartas() {
        Partida p = partida(5, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertEquals(List.of(c(5)), r.robadas());
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
        assertEquals(List.of(c(5), c(5)), jugador(p, 1).getCartasEnMesa());
    }

    @Test
    void elRoboSoloAfectaAlMismoValor() {
        // Ana tiene 5, Luis tiene 3, Eva saca 5: solo roba a Ana.
        Partida p = partida(5, 3, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        assertEquals(List.of(c(3)), jugador(p, 1).getCartasEnMesa());
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
        assertEquals(Map.of(5, 2), jugador(p, 2).getMesaAgrupada());
    }

    @Test
    void elRoboTomaTodasLasCartasDeEseValor() {
        // Ana junta dos 5 y se planta; Luis saca un 5 y se lleva los dos: queda con tres.
        Partida p = partida(5, 5, 5);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertEquals(2, r.robadas().size());
        assertEquals(Map.of(5, 3), jugador(p, 1).getMesaAgrupada());
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
    }

    @Test
    void lasCartasRobadasNoVanAlMontonDeQuienLasPierde() {
        Partida p = partida(5, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        assertTrue(jugador(p, 0).getMontonPuntos().isEmpty());
        assertTrue(jugador(p, 1).getMontonPuntos().isEmpty());
    }

    @Test
    void laListaDeRobadasNoSePuedeModificar() {
        ResultadoSacarCarta r = partida(5).sacarCarta("Ana");
        assertThrows(UnsupportedOperationException.class, () -> r.robadas().add(c(1)));
    }

    // ---- estado inicial ----

    @Test
    void elEstadoInicialDejaDeSerValidoTrasUnaAccion() {
        Partida p = Partida.crear(List.of("Ana", "Luis"));
        assertTrue(p.esEstadoInicialValido());
        p.sacarCarta(p.getJugadorActual().getNombre());
        assertFalse(p.esEstadoInicialValido());
    }
}

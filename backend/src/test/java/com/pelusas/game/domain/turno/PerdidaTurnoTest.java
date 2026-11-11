package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Pruebas de la regla de pérdida del turno (req. 8). Jugadores: Ana (inicia), Luis, Eva. */
class PerdidaTurnoTest {

    private static Carta c(int valor) {
        return new Carta(valor);
    }

    /** Mazo exacto: las cartas salen en el orden indicado y la partida termina al sacar la última. */
    private static Partida partidaExacta(int... valores) {
        List<Carta> cartas = Arrays.stream(valores).mapToObj(Carta::new).toList();
        return Partida.conMazo(List.of("Ana", "Luis", "Eva"), Mazo.deCartas(cartas), 0);
    }

    /** Igual que la exacta, pero con una carta de reserva al final que no se saca: la partida sigue en curso. */
    private static Partida partida(int... valores) {
        int[] conReserva = Arrays.copyOf(valores, valores.length + 1);
        conReserva[valores.length] = 10;
        return partidaExacta(conReserva);
    }

    private static Jugador jugador(Partida p, int indice) {
        return p.getJugadores().get(indice);
    }

    /** Ana saca todas las cartas indicadas seguidas; devuelve el resultado de la última. */
    private static ResultadoSacarCarta anaSaca(Partida p, int veces) {
        ResultadoSacarCarta r = null;
        for (int i = 0; i < veces; i++) {
            r = p.sacarCarta("Ana");
        }
        return r;
    }

    // ---- casos límite del requerimiento ----

    @Test
    void repetirConMenosDeTresCartasNoPierde() {
        // Tiene un 4 y saca otro 4: 2 cartas en total.
        Partida p = partida(4, 4);
        ResultadoSacarCarta r = anaSaca(p, 2);
        assertFalse(r.turnoPerdido());
        assertEquals(Map.of(4, 2), jugador(p, 0).getMesaAgrupada());
        assertEquals(0, p.getIndiceJugadorActual());
    }

    @Test
    void repetirConExactamenteTresCartasPierde() {
        // 4, 5 y otro 4: 3 cartas en total.
        Partida p = partida(4, 5, 4);
        ResultadoSacarCarta r = anaSaca(p, 3);
        assertTrue(r.turnoPerdido());
    }

    @Test
    void repetirConMasDeTresCartasPierde() {
        // 4, 5, 6 y otro 4: 4 cartas en total.
        Partida p = partida(4, 5, 6, 4);
        ResultadoSacarCarta r = anaSaca(p, 4);
        assertTrue(r.turnoPerdido());
    }

    @Test
    void repetirUnValorDistintoConTresCartasTambienPierde() {
        // 4, 5 y otro 5: 3 cartas en total.
        Partida p = partida(4, 5, 5);
        assertTrue(anaSaca(p, 3).turnoPerdido());
    }

    @Test
    void unaCartaNuevaNuncaHacePerderElTurno() {
        // Cuatro cartas distintas: sin repetición no se pierde.
        Partida p = partida(1, 2, 3, 4);
        ResultadoSacarCarta r = anaSaca(p, 4);
        assertFalse(r.turnoPerdido());
        assertEquals(4, jugador(p, 0).getCartasEnMesa().size());
        assertEquals(0, p.getIndiceJugadorActual());
    }

    

    @Test
    void alPerderSeDescartanTodasLasCartasDelanteDelJugador() {
        Partida p = partida(4, 5, 4);
        ResultadoSacarCarta r = anaSaca(p, 3);
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
        assertEquals(List.of(c(4), c(5), c(4)), r.descartadas());
        assertEquals(List.of(c(4), c(5), c(4)), p.getDescarte());
    }

    @Test
    void laCartaRepetidaTambienSeDescarta() {
        Partida p = partida(4, 5, 6, 4);
        ResultadoSacarCarta r = anaSaca(p, 4);
        assertEquals(c(4), r.carta());
        assertEquals(4, r.descartadas().size());
        assertTrue(r.descartadas().contains(c(6)));
    }

    @Test
    void lasCartasDescartadasNoVanAlMontonDePuntos() {
        Partida p = partida(4, 5, 4);
        anaSaca(p, 3);
        assertTrue(jugador(p, 0).getMontonPuntos().isEmpty());
        assertEquals(0, jugador(p, 0).puntos());
    }

    @Test
    void laPerdidaNoAfectaALasCartasYaGuardadas() {
        Partida p = partida(1, 2, 3, 4, 5, 4);
        anaSaca(p, 1);
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        p.guardarPelusas("Ana");
        anaSaca(p, 3);
        assertEquals(List.of(c(1)), jugador(p, 0).getMontonPuntos());
        assertEquals(1, jugador(p, 0).puntos());
    }

    @Test
    void alPerderElTurnoPasaAlSiguienteJugadorEnFaseInicio() {
        Partida p = partida(4, 5, 4, 9);
        anaSaca(p, 3);
        assertEquals(1, p.getIndiceJugadorActual());
        assertEquals("Luis", p.getJugadorActual().getNombre());
        assertEquals(FaseTurno.INICIO, p.getFase());
    }

    @Test
    void alPerderNoHaceFaltaPlantarseYElJugadorYaNoPuedeActuar() {
        Partida p = partida(4, 5, 4, 9);
        anaSaca(p, 3);
        assertThrows(AccionInvalidaException.class, () -> p.sacarCarta("Ana"));
        assertThrows(AccionInvalidaException.class, () -> p.plantarse("Ana"));
    }

    @Test
    void elSiguienteJugadorPuedeJugarConNormalidad() {
        Partida p = partida(4, 5, 4, 9);
        anaSaca(p, 3);
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertEquals(c(9), r.carta());
        assertFalse(r.turnoPerdido());
        assertEquals(List.of(c(9)), jugador(p, 1).getCartasEnMesa());
    }

    @Test
    void lasCartasDescartadasNoSePuedenRobar() {
        // Ana pierde con 4, 5, 4; Luis saca otro 4 y no recibe nada.
        Partida p = partida(4, 5, 4, 4);
        anaSaca(p, 3);
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertTrue(r.robadas().isEmpty());
        assertEquals(List.of(c(4)), jugador(p, 1).getCartasEnMesa());
    }

    @Test
    void sinPerdidaElResultadoNoTraeDescartes() {
        ResultadoSacarCarta r = partida(4).sacarCarta("Ana");
        assertFalse(r.turnoPerdido());
        assertTrue(r.descartadas().isEmpty());
        assertTrue(partida(4).getDescarte().isEmpty());
    }

    @Test
    void alPerderNoSeRobaNada() {
        Partida p = partida(4, 5, 4);
        assertTrue(anaSaca(p, 3).robadas().isEmpty());
    }

    // ---- interacción con el robo ----

    @Test
    void tambienSeDescartanLasCartasQueFueronRobadas() {
        // Ana deja un 5; Luis saca otro 5 (le roba el de Ana, 2 cartas) y vuelve a sacar un 5:
        // repite con 3 cartas en total y pierde las tres.
        Partida p = partida(5, 5, 5);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertTrue(r.turnoPerdido());
        assertEquals(3, r.descartadas().size());
        assertTrue(jugador(p, 1).getCartasEnMesa().isEmpty());
        assertTrue(jugador(p, 0).getCartasEnMesa().isEmpty());
    }

    @Test
    void robarCartasDelMismoValorNoCuentaComoRepeticion() {
        // Luis tiene 5 y 6; saca un 4 que Ana tenía delante y se lo lleva: queda con 4 cartas
        // (5, 6, 4, 4) pero el 4 no era repetido para él, así que no pierde.
        Partida p = partida(4, 5, 6, 4);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.sacarCarta("Luis");
        ResultadoSacarCarta r = p.sacarCarta("Luis");
        assertFalse(r.turnoPerdido());
        assertEquals(List.of(c(4)), r.robadas());
        assertEquals(Map.of(4, 2, 5, 1, 6, 1), jugador(p, 1).getMesaAgrupada());
    }

    // ---- consistencia ----

    @Test
    void lasCartasSeConservanTrasUnaPerdida() {
        Partida p = partidaExacta(4, 5, 4, 9, 9);
        anaSaca(p, 3);
        int enJuego = p.getMazo().restantes() + p.getDescarte().size();
        assertEquals(5, enJuego);
    }

    @Test
    void elDescarteEmpiezaVacioYNoSePuedeModificar() {
        Partida p = Partida.crear(List.of("Ana", "Luis"));
        assertTrue(p.getDescarte().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> p.getDescarte().add(c(1)));
    }

    @Test
    void elEstadoInicialDejaDeSerValidoSiHayDescarte() {
        Partida p = partida(4, 5, 4);
        anaSaca(p, 3);
        assertFalse(p.esEstadoInicialValido());
    }
}

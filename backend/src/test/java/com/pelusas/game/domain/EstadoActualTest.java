package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** El backend informa el estado y las acciones permitidas; el frontend no las deduce. */
class EstadoActualTest {

    private static Partida partidaExacta(int... valores) {
        List<Carta> cartas = Arrays.stream(valores).mapToObj(Carta::new).toList();
        return Partida.conMazo(List.of("Ana", "Luis", "Eva"), Mazo.deCartas(cartas), 0);
    }

    /** Con una carta de reserva al final para que la partida no termine. */
    private static Partida partida(int... valores) {
        int[] conReserva = Arrays.copyOf(valores, valores.length + 1);
        conReserva[valores.length] = 10;
        return partidaExacta(conReserva);
    }

    // ---- estado de una partida nueva ----

    @Test
    void laPartidaNuevaInformaSuEstadoCompleto() {
        Partida p = Partida.crear(List.of("Ana", "Luis"));
        EstadoActual e = p.getEstadoActual();
        assertEquals(p.getId(), e.id());
        assertEquals(EstadoPartida.EN_CURSO, e.estado());
        assertEquals(FaseTurno.INICIO, e.fase());
        assertEquals(p.getJugadorActual().getNombre(), e.jugadorActual());
        assertEquals(110, e.cartasRestantes());
        assertEquals(0, e.cartasDescartadas());
        assertTrue(e.ganadores().isEmpty());
        assertEquals(List.of("Ana", "Luis"), e.jugadores().stream().map(EstadoActual.JugadorEstado::nombre).toList());
    }

    @Test
    void alInicioNadaEstaEnMesaNiEnMonton() {
        for (EstadoActual.JugadorEstado j : Partida.crear(List.of("Ana", "Luis")).getEstadoActual().jugadores()) {
            assertTrue(j.mesaAgrupada().isEmpty());
            assertEquals(0, j.cartasEnMesa());
            assertEquals(0, j.cartasEnMonton());
            assertEquals(0, j.puntos());
        }
    }

    @Test
    void alInicioSoloSePuedeSacarCarta() {
        EstadoActual.Acciones a = partida(1).getEstadoActual().acciones();
        assertFalse(a.guardarPelusas());
        assertTrue(a.sacarCarta());
        assertFalse(a.plantarse());
    }

    // ---- acciones permitidas según la fase ----

    @Test
    void trasSacarCartaSePuedeSeguirOPlantarseYNoGuardar() {
        Partida p = partida(4, 5);
        p.sacarCarta("Ana");
        EstadoActual e = p.getEstadoActual();
        assertEquals(FaseTurno.BUSCANDO, e.fase());
        assertEquals(new EstadoActual.Acciones(false, true, true), e.acciones());
    }

    @Test
    void alVolverSuTurnoConCartasEnMesaPuedeGuardar() {
        Partida p = partida(1, 2, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        assertEquals("Ana", p.getEstadoActual().jugadorActual());
        assertEquals(new EstadoActual.Acciones(true, true, false), p.getEstadoActual().acciones());
    }

    @Test
    void trasGuardarYaNoPuedeGuardarDeNuevo() {
        Partida p = partida(1, 2, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        p.guardarPelusas("Ana");
        assertFalse(p.getEstadoActual().acciones().guardarPelusas());
    }

    @Test
    void sinCartasEnMesaNoSePuedeGuardar() {
        assertFalse(partida(1).puedeGuardarPelusas());
    }

    @Test
    void conElMazoVacioNoSePuedeSacarCarta() {
        assertFalse(partidaExacta().puedeSacarCarta());
    }

    // ---- reflejo de lo que pasó ----

    @Test
    void laMesaSeInformaAgrupadaPorValor() {
        Partida p = partida(7, 7, 2);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        EstadoActual.JugadorEstado ana = p.getEstadoActual().jugadores().get(0);
        assertEquals(Map.of(7, 2, 2, 1), ana.mesaAgrupada());
        assertEquals(3, ana.cartasEnMesa());
        assertEquals(1, p.getEstadoActual().cartasRestantes()); // solo queda la carta de reserva
    }

    @Test
    void elEstadoInformaLosPuntosYElMonton() {
        Partida p = partida(6, 2, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        p.plantarse("Luis");
        p.sacarCarta("Eva");
        p.plantarse("Eva");
        p.guardarPelusas("Ana");
        EstadoActual.JugadorEstado ana = p.getEstadoActual().jugadores().get(0);
        assertEquals(1, ana.cartasEnMonton());
        assertEquals(6, ana.puntos());
        assertEquals(0, ana.cartasEnMesa());
    }

    @Test
    void elEstadoInformaElDescarteTrasUnaPerdida() {
        Partida p = partida(4, 5, 4, 9);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        EstadoActual e = p.getEstadoActual();
        assertEquals(3, e.cartasDescartadas());
        assertEquals("Luis", e.jugadorActual());
        assertTrue(e.jugadores().get(0).mesaAgrupada().isEmpty());
    }

    @Test
    void elEstadoNoCambiaAlConsultarlo() {
        Partida p = partida(4, 5);
        p.sacarCarta("Ana");
        assertEquals(p.getEstadoActual(), p.getEstadoActual());
    }

    // ---- partida finalizada ----

    @Test
    void unaPartidaFinalizadaNoPermiteNingunaAccionEInformaGanadores() {
        Partida p = partidaExacta(9, 3);
        p.sacarCarta("Ana");
        p.plantarse("Ana");
        p.sacarCarta("Luis");
        EstadoActual e = p.getEstadoActual();
        assertEquals(EstadoPartida.FINALIZADA, e.estado());
        assertEquals(new EstadoActual.Acciones(false, false, false), e.acciones());
        assertEquals(List.of("Ana"), e.ganadores());
        assertEquals(0, e.cartasRestantes());
        assertEquals(9, e.jugadores().get(0).puntos());
    }

    @Test
    void elEstadoInformaTodosLosGanadoresSiHayEmpate() {
        Partida p = partidaExacta(4, 5, 4);
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        p.sacarCarta("Ana");
        assertEquals(List.of("Ana", "Luis", "Eva"), p.getEstadoActual().ganadores());
    }

    // ---- encapsulación: las reglas no se pueden saltar desde fuera del dominio ----

    @Test
    void elMazoNoEsAccesibleNiModificableDesdeFueraDelPaquete() throws Exception {
        assertFalse(Modifier.isPublic(Partida.class.getDeclaredMethod("getMazo").getModifiers()));
        assertFalse(Modifier.isPublic(Mazo.class.getDeclaredMethod("sacar").getModifiers()));
        assertFalse(Modifier.isPublic(Mazo.class.getDeclaredMethod("barajar", java.util.Random.class).getModifiers()));
    }

    @Test
    void losJugadoresNoSePuedenModificarDesdeFueraDelPaquete() throws Exception {
        for (String metodo : List.of("recibirEnMesa", "guardarMesa", "vaciarMesa")) {
            boolean publico = Arrays.stream(Jugador.class.getDeclaredMethods())
                    .filter(m -> m.getName().equals(metodo))
                    .anyMatch(m -> Modifier.isPublic(m.getModifiers()));
            assertFalse(publico, metodo);
        }
        boolean retirarPublico = Arrays.stream(Jugador.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("retirarDeMesa"))
                .anyMatch(m -> Modifier.isPublic(m.getModifiers()));
        assertFalse(retirarPublico);
    }
}

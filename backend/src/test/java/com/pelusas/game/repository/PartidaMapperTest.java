package com.pelusas.game.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.pelusas.game.domain.Carta;
import com.pelusas.game.domain.EstadoPartida;
import com.pelusas.game.domain.FaseTurno;
import com.pelusas.game.domain.Partida;
import com.pelusas.game.domain.PartidaSnapshot;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Conversión entre la foto del dominio y las entidades (sin base de datos). */
class PartidaMapperTest {

    private static final List<String> JUGADORES = List.of("Ana", "Luis", "Eva");

    private static Partida nueva(long semilla) {
        return Partida.crear(JUGADORES, new Random(semilla));
    }

    private static Partida terminada(long semilla) {
        Random azar = new Random(semilla);
        Partida p = nueva(semilla);
        while (p.getEstado() == EstadoPartida.EN_CURSO) {
            String actual = p.getJugadorActual().getNombre();
            if (p.puedeGuardarPelusas() && azar.nextInt(3) == 0) {
                p.guardarPelusas(actual);
            } else if (p.puedePlantarse() && azar.nextInt(3) == 0) {
                p.plantarse(actual);
            } else {
                p.sacarCarta(actual);
            }
        }
        return p;
    }

    // ---- texto de cartas ----

    @Test
    void codificaLasCartasSeparadasPorComas() {
        assertEquals("4,6,9", PartidaMapper.codificar(List.of(new Carta(4), new Carta(6), new Carta(9))));
        assertEquals("10", PartidaMapper.codificar(List.of(new Carta(10))));
    }

    @Test
    void unaListaVaciaSeCodificaComoTextoVacio() {
        assertEquals("", PartidaMapper.codificar(List.of()));
    }

    @Test
    void decodificaElTextoDeCartas() {
        assertEquals(List.of(new Carta(4), new Carta(6), new Carta(10)), PartidaMapper.decodificar("4,6,10"));
        assertEquals(List.of(new Carta(1), new Carta(2)), PartidaMapper.decodificar(" 1 , 2 "));
    }

    @Test
    void textoVacioNuloOEnBlancoSeDecodificaComoListaVacia() {
        assertTrue(PartidaMapper.decodificar("").isEmpty());
        assertTrue(PartidaMapper.decodificar("   ").isEmpty());
        assertTrue(PartidaMapper.decodificar(null).isEmpty());
    }

    @Test
    void rechazaTextoDeCartasInvalido() {
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.decodificar("4,a,6"));
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.decodificar("4,11"));
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.decodificar("4,,6"));
    }

    // ---- foto → entidad ----

    @Test
    void creaLaEntidadConLosDatosDeLaPartida() {
        Partida p = nueva(1);
        PartidaSnapshot foto = p.snapshot();
        PartidaEntity e = PartidaMapper.aEntidad(foto);

        assertEquals(foto.id(), e.getId());
        assertEquals(foto.fechaCreacion(), e.getFechaCreacion());
        assertEquals(EstadoPartida.EN_CURSO, e.getEstado());
        assertEquals(FaseTurno.INICIO, e.getFase());
        assertEquals(foto.indiceJugadorActual(), e.getIndiceJugadorActual());
        assertEquals(110, PartidaMapper.decodificar(e.getMazo()).size());
        assertEquals("", e.getDescarte());
    }

    @Test
    void creaUnaEntidadPorJugadorEnOrdenYConLaRelacionInversa() {
        PartidaEntity e = PartidaMapper.aEntidad(nueva(1).snapshot());
        assertEquals(3, e.getJugadores().size());
        for (int i = 0; i < 3; i++) {
            JugadorEntity j = e.getJugadores().get(i);
            assertEquals(JUGADORES.get(i), j.getNombre());
            assertEquals(i, j.getOrden());
            assertSame(e, j.getPartida());
        }
    }

    @Test
    void guardaCartasPuntosYGanadorDeCadaJugador() {
        Partida p = terminada(3);
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        assertEquals(EstadoPartida.FINALIZADA, e.getEstado());
        for (int i = 0; i < e.getJugadores().size(); i++) {
            JugadorEntity j = e.getJugadores().get(i);
            var esperado = p.getResultado().jugadores().get(i);
            assertEquals(esperado.cartasAcumuladas(), j.getCartasAcumuladas());
            assertEquals(esperado.puntos(), j.getPuntos());
            assertEquals(esperado.ganador(), j.isGanador());
            assertEquals("", j.getMesa()); // al final todos guardan lo que tenían delante
        }
        assertTrue(e.getJugadores().stream().anyMatch(JugadorEntity::isGanador));
    }

    // ---- ida y vuelta ----

    @Test
    void laIdaYVueltaConservaLaFoto() {
        for (Partida p : List.of(nueva(1), terminada(3), midTurno())) {
            PartidaSnapshot foto = p.snapshot();
            assertEquals(foto, PartidaMapper.aSnapshot(PartidaMapper.aEntidad(foto)));
        }
    }

    private static Partida midTurno() {
        Partida p = nueva(14);
        p.sacarCarta(p.getJugadorActual().getNombre());
        p.sacarCarta(p.getJugadorActual().getNombre());
        return p;
    }

    @Test
    void laPartidaReconstruidaDesdeLaEntidadEsIgualALaOriginal() {
        Partida original = midTurno();
        Partida restaurada = Partida.restaurar(PartidaMapper.aSnapshot(PartidaMapper.aEntidad(original.snapshot())));
        assertEquals(original.getEstadoActual(), restaurada.getEstadoActual());
    }

    @Test
    void aSnapshotOrdenaLosJugadoresPorSuOrdenAunqueLaListaVengaDesordenada() {
        PartidaEntity e = PartidaMapper.aEntidad(nueva(1).snapshot());
        JugadorEntity primero = e.getJugadores().remove(0);
        e.getJugadores().add(primero); // ahora el orden de la lista es 1, 2, 0
        List<String> nombres = PartidaMapper.aSnapshot(e).jugadores().stream()
                .map(PartidaSnapshot.JugadorSnapshot::nombre).toList();
        assertEquals(JUGADORES, nombres);
    }

    // ---- actualización ----

    @Test
    void actualizarCambiaElEstadoSinReemplazarLasFilasDeLosJugadores() {
        Partida p = nueva(14);
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        List<JugadorEntity> antes = List.copyOf(e.getJugadores());

        String actual = p.getJugadorActual().getNombre();
        p.sacarCarta(actual);
        PartidaMapper.actualizar(e, p.snapshot());

        assertEquals(FaseTurno.BUSCANDO, e.getFase());
        assertEquals(109, PartidaMapper.decodificar(e.getMazo()).size());
        for (int i = 0; i < 3; i++) {
            assertSame(antes.get(i), e.getJugadores().get(i));
        }
        JugadorEntity quienJugo = e.getJugadores().stream()
                .filter(j -> j.getNombre().equals(actual)).findFirst().orElseThrow();
        assertEquals(1, PartidaMapper.decodificar(quienJugo.getMesa()).size());
    }

    @Test
    void actualizarNoCambiaLaFechaDeCreacion() {
        Partida p = nueva(1);
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        var fecha = e.getFechaCreacion();
        p.sacarCarta(p.getJugadorActual().getNombre());
        PartidaMapper.actualizar(e, p.snapshot());
        assertEquals(fecha, e.getFechaCreacion());
    }

    @Test
    void actualizarRegistraLaPerdidaYElDescarte() {
        Partida p = Partida.crear(List.of("Ana", "Luis"), new Random(14));
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        for (int i = 0; i < 4; i++) {
            p.sacarCarta("Ana");
        }
        PartidaMapper.actualizar(e, p.snapshot());
        assertEquals("4,6,9,4", e.getDescarte());
        assertEquals(1, e.getIndiceJugadorActual());
        assertEquals("", e.getJugadores().get(0).getMesa());
    }

    @Test
    void actualizarRechazaUnaFotoDeOtraPartida() {
        PartidaEntity e = PartidaMapper.aEntidad(nueva(1).snapshot());
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.actualizar(e, nueva(1).snapshot()));
    }

    @Test
    void actualizarRechazaUnaCantidadDeJugadoresDistinta() {
        Partida p = nueva(1);
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        e.getJugadores().remove(2);
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.actualizar(e, p.snapshot()));
    }

    @Test
    void actualizarRechazaUnJugadorConOtroNombre() {
        Partida p = nueva(1);
        PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
        e.getJugadores().get(1).setNombre("Otro");
        assertThrows(IllegalArgumentException.class, () -> PartidaMapper.actualizar(e, p.snapshot()));
    }

    @Test
    void guardarYContinuarEnPartidasCompletasConservaLasReglas() {
        // Simula lo que hará el servicio: tras cada acción guarda, y antes de la siguiente reconstruye.
        for (long semilla = 0; semilla < 60; semilla++) {
            Random azar = new Random(semilla);
            Partida p = nueva(semilla);
            PartidaEntity e = PartidaMapper.aEntidad(p.snapshot());
            while (p.getEstado() == EstadoPartida.EN_CURSO) {
                Partida cargada = Partida.restaurar(PartidaMapper.aSnapshot(e));
                String actual = cargada.getJugadorActual().getNombre();
                int sorteo = azar.nextInt(4);
                if (cargada.puedeGuardarPelusas() && sorteo == 0) {
                    cargada.guardarPelusas(actual);
                    p.guardarPelusas(actual);
                } else if (cargada.puedePlantarse() && sorteo == 1) {
                    cargada.plantarse(actual);
                    p.plantarse(actual);
                } else {
                    assertEquals(p.sacarCarta(actual), cargada.sacarCarta(actual), "semilla " + semilla);
                }
                PartidaMapper.actualizar(e, cargada.snapshot());
                assertEquals(p.getEstadoActual(), cargada.getEstadoActual(), "semilla " + semilla);
            }
            assertEquals(p.getResultado(), Partida.restaurar(PartidaMapper.aSnapshot(e)).getResultado());
        }
    }
}

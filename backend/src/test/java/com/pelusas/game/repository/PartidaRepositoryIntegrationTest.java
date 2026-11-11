package com.pelusas.game.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.pelusas.game.domain.EstadoPartida;
import com.pelusas.game.domain.FaseTurno;
import com.pelusas.game.domain.Partida;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Integración con base de datos (requerimiento 13.1).
 *
 * Usa una base H2 real y los repositorios reales de Spring Data: no hay mocks. {@code @DataJpaTest}
 * levanta solo la capa JPA con una base embebida y deshace cada prueba al terminar.
 * Tras guardar se llama a {@code clear()} para que cada lectura vaya realmente a la base de datos
 * y no a la caché de la sesión.
 */
@DataJpaTest
class PartidaRepositoryIntegrationTest {

    private static final List<String> JUGADORES = List.of("Ana", "Luis", "Eva");

    @Autowired
    private PartidaRepository repositorio;

    @Autowired
    private TestEntityManager em;

    // ------------------------------------------------------------------
    // Ayudas
    // ------------------------------------------------------------------

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

    /** Guarda la partida y vacía la sesión para que lo siguiente se lea de la base de datos. */
    private PartidaEntity guardar(Partida partida) {
        PartidaEntity guardada = repositorio.saveAndFlush(PartidaMapper.aEntidad(partida.snapshot()));
        em.clear();
        return guardada;
    }

    private PartidaEntity cargar(UUID id) {
        return repositorio.findById(id).orElseThrow();
    }

    private long contarJugadores() {
        return em.getEntityManager().createQuery("select count(j) from JugadorEntity j", Long.class).getSingleResult();
    }

    // ------------------------------------------------------------------
    // Persistencia de partidas
    // ------------------------------------------------------------------

    @Test
    void persisteUnaPartidaYLaRecuperaConSusDatos() {
        Partida p = nueva(1);
        guardar(p);

        PartidaEntity leida = cargar(p.getId());
        assertEquals(p.getId(), leida.getId());
        assertEquals(EstadoPartida.EN_CURSO, leida.getEstado());
        assertEquals(FaseTurno.INICIO, leida.getFase());
        assertEquals(p.getIndiceJugadorActual(), leida.getIndiceJugadorActual());
        assertEquals(p.getFechaCreacion().truncatedTo(ChronoUnit.MILLIS), leida.getFechaCreacion().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(110, PartidaMapper.decodificar(leida.getMazo()).size());
    }

    @Test
    void laPartidaRecuperadaDeLaBaseSeReconstruyeIdentica() {
        Partida p = nueva(14);
        p.sacarCarta(p.getJugadorActual().getNombre());
        p.sacarCarta(p.getJugadorActual().getNombre());
        guardar(p);

        Partida restaurada = Partida.restaurar(PartidaMapper.aSnapshot(cargar(p.getId())));
        assertEquals(p.getEstadoActual(), restaurada.getEstadoActual());
    }

    @Test
    void recuperarUnaPartidaInexistenteDevuelveVacio() {
        assertEquals(Optional.empty(), repositorio.findById(UUID.randomUUID()));
    }

    @Test
    void persisteVariasPartidasIndependientes() {
        Partida a = nueva(1);
        Partida b = nueva(2);
        guardar(a);
        guardar(b);
        assertEquals(2, repositorio.count());
        assertNotEquals(cargar(a.getId()).getMazo(), cargar(b.getId()).getMazo());
    }

    // ------------------------------------------------------------------
    // Persistencia de jugadores
    // ------------------------------------------------------------------

    @Test
    void persisteLosJugadoresEnSuPropiaTabla() {
        guardar(nueva(1));
        guardar(nueva(2));
        assertEquals(6, contarJugadores());
    }

    @Test
    void recuperaLosJugadoresEnOrdenConSusDatos() {
        Partida p = nueva(14);
        for (int i = 0; i < 3; i++) {
            p.sacarCarta(p.getJugadorActual().getNombre());
        }
        guardar(p);

        List<JugadorEntity> jugadores = cargar(p.getId()).getJugadores();
        assertEquals(JUGADORES, jugadores.stream().map(JugadorEntity::getNombre).toList());
        assertEquals(List.of(0, 1, 2), jugadores.stream().map(JugadorEntity::getOrden).toList());
        assertNotNull(jugadores.get(0).getId());
        JugadorEntity quienJugo = jugadores.get(p.getIndiceJugadorActual());
        assertEquals(3, PartidaMapper.decodificar(quienJugo.getMesa()).size());
    }

    // ------------------------------------------------------------------
    // Actualización del estado del juego
    // ------------------------------------------------------------------

    @Test
    void actualizaElEstadoDeUnaPartidaYLoGuardaEnLaBase() {
        Partida p = nueva(14);
        guardar(p);

        PartidaEntity entidad = cargar(p.getId());
        Partida cargada = Partida.restaurar(PartidaMapper.aSnapshot(entidad));
        String actual = cargada.getJugadorActual().getNombre();
        cargada.sacarCarta(actual);
        PartidaMapper.actualizar(entidad, cargada.snapshot());
        repositorio.saveAndFlush(entidad);
        em.clear();

        PartidaEntity releida = cargar(p.getId());
        assertEquals(FaseTurno.BUSCANDO, releida.getFase());
        assertEquals(109, PartidaMapper.decodificar(releida.getMazo()).size());
        assertEquals(cargada.getEstadoActual(), Partida.restaurar(PartidaMapper.aSnapshot(releida)).getEstadoActual());
    }

    @Test
    void actualizarNoDuplicaFilasNiCambiaLaIdentidadDeLosJugadores() {
        Partida p = nueva(1);
        guardar(p);
        List<Long> idsAntes = cargar(p.getId()).getJugadores().stream().map(JugadorEntity::getId).toList();
        em.clear();

        PartidaEntity entidad = cargar(p.getId());
        p.sacarCarta(p.getJugadorActual().getNombre());
        PartidaMapper.actualizar(entidad, p.snapshot());
        repositorio.saveAndFlush(entidad);
        em.clear();

        assertEquals(1, repositorio.count());
        assertEquals(3, contarJugadores());
        assertEquals(idsAntes, cargar(p.getId()).getJugadores().stream().map(JugadorEntity::getId).toList());
    }

    @Test
    void cadaActualizacionIncrementaLaVersion() {
        Partida p = nueva(1);
        guardar(p);
        Long antes = cargar(p.getId()).getVersion();
        em.clear();

        PartidaEntity entidad = cargar(p.getId());
        p.sacarCarta(p.getJugadorActual().getNombre());
        PartidaMapper.actualizar(entidad, p.snapshot());
        repositorio.saveAndFlush(entidad);
        em.clear();

        assertTrue(cargar(p.getId()).getVersion() > antes);
    }

    @Test
    void unaPartidaCompletaSeJuegaGuardandoYRecargandoEnCadaPaso() {
        Random azar = new Random(5);
        Partida referencia = nueva(5);
        guardar(referencia);

        while (referencia.getEstado() == EstadoPartida.EN_CURSO) {
            PartidaEntity entidad = cargar(referencia.getId());
            Partida cargada = Partida.restaurar(PartidaMapper.aSnapshot(entidad));
            String actual = cargada.getJugadorActual().getNombre();
            int sorteo = azar.nextInt(4);
            if (cargada.puedeGuardarPelusas() && sorteo == 0) {
                cargada.guardarPelusas(actual);
                referencia.guardarPelusas(actual);
            } else if (cargada.puedePlantarse() && sorteo == 1) {
                cargada.plantarse(actual);
                referencia.plantarse(actual);
            } else {
                assertEquals(referencia.sacarCarta(actual), cargada.sacarCarta(actual));
            }
            PartidaMapper.actualizar(entidad, cargada.snapshot());
            repositorio.saveAndFlush(entidad);
            em.clear();
        }

        PartidaEntity registroFinal = cargar(referencia.getId());
        assertEquals(EstadoPartida.FINALIZADA, registroFinal.getEstado());
        assertEquals(referencia.getResultado(),
                Partida.restaurar(PartidaMapper.aSnapshot(registroFinal)).getResultado());
    }

    @Test
    void unaActualizacionConUnaVersionVencidaSeRechaza() {
        Partida p = nueva(1);
        guardar(p);

        PartidaEntity vieja = cargar(p.getId());   // versión leída antes
        em.clear();
        PartidaEntity reciente = cargar(p.getId());
        reciente.setFase(FaseTurno.BUSCANDO);
        repositorio.saveAndFlush(reciente);          // alguien más actualizó primero
        em.clear();

        vieja.setFase(FaseTurno.BUSCANDO);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> repositorio.saveAndFlush(vieja));
    }

    // ------------------------------------------------------------------
    // Relaciones entre entidades
    // ------------------------------------------------------------------

    @Test
    void cadaJugadorApuntaASuPartida() {
        Partida p = nueva(1);
        guardar(p);

        PartidaEntity leida = cargar(p.getId());
        for (JugadorEntity jugador : leida.getJugadores()) {
            assertSame(leida, jugador.getPartida());
            assertEquals(p.getId(), jugador.getPartida().getId());
        }
    }

    @Test
    void alBorrarUnaPartidaSeBorranSusJugadores() {
        Partida borrada = nueva(1);
        Partida conservada = nueva(2);
        guardar(borrada);
        guardar(conservada);

        repositorio.deleteById(borrada.getId());
        repositorio.flush();
        em.clear();

        assertFalse(repositorio.existsById(borrada.getId()));
        assertTrue(repositorio.existsById(conservada.getId()));
        assertEquals(3, contarJugadores()); // solo quedan los de la partida conservada
    }

    @Test
    void unJugadorQuitadoDeLaPartidaSeBorraDeLaBase() {
        Partida p = nueva(1);
        guardar(p);

        PartidaEntity entidad = cargar(p.getId());
        entidad.getJugadores().remove(2);
        repositorio.saveAndFlush(entidad);
        em.clear();

        assertEquals(2, cargar(p.getId()).getJugadores().size());
        assertEquals(2, contarJugadores());
    }

    @Test
    void noPuedenExistirDosJugadoresConElMismoOrdenEnUnaPartida() {
        PartidaEntity entidad = PartidaMapper.aEntidad(nueva(1).snapshot());
        entidad.getJugadores().get(1).setOrden(0);
        assertThrows(DataIntegrityViolationException.class, () -> repositorio.saveAndFlush(entidad));
    }

    // ------------------------------------------------------------------
    // Histórico de partidas terminadas
    // ------------------------------------------------------------------

    @Test
    void elHistoricoDevuelveSoloLasPartidasTerminadasDeLaMasRecienteALaMasAntigua() {
        LocalDateTime ahora = LocalDateTime.now();
        Partida antigua = terminada(3);
        Partida reciente = terminada(4);
        Partida enCurso = nueva(5);

        PartidaEntity a = PartidaMapper.aEntidad(antigua.snapshot());
        a.setFechaCreacion(ahora.minusDays(2));
        PartidaEntity r = PartidaMapper.aEntidad(reciente.snapshot());
        r.setFechaCreacion(ahora.minusDays(1));
        repositorio.saveAndFlush(a);
        repositorio.saveAndFlush(r);
        repositorio.saveAndFlush(PartidaMapper.aEntidad(enCurso.snapshot()));
        em.clear();

        List<PartidaEntity> historico = repositorio.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA);

        assertEquals(List.of(reciente.getId(), antigua.getId()), historico.stream().map(PartidaEntity::getId).toList());
    }

    @Test
    void elHistoricoGuardaPorCadaJugadorNombreCartasPuntosYGanador() {
        Partida p = terminada(3);
        guardar(p);

        PartidaEntity registro = repositorio.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA).get(0);
        assertEquals(p.getId(), registro.getId());
        assertEquals(3, registro.getJugadores().size());
        for (int i = 0; i < 3; i++) {
            JugadorEntity guardado = registro.getJugadores().get(i);
            var esperado = p.getResultado().jugadores().get(i);
            assertEquals(esperado.nombre(), guardado.getNombre());
            assertEquals(esperado.cartasAcumuladas(), guardado.getCartasAcumuladas());
            assertEquals(esperado.puntos(), guardado.getPuntos());
            assertEquals(esperado.ganador(), guardado.isGanador());
        }
        assertTrue(registro.getJugadores().stream().anyMatch(JugadorEntity::isGanador));
    }

    @Test
    void elHistoricoEstaVacioSiNoHayPartidasTerminadas() {
        guardar(nueva(1));
        assertTrue(repositorio.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA).isEmpty());
    }

    @Test
    void unaPartidaQuePasaAFinalizadaEntraAlHistorico() {
        Partida p = nueva(5);
        guardar(p);
        assertTrue(repositorio.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA).isEmpty());

        Random azar = new Random(5);
        while (p.getEstado() == EstadoPartida.EN_CURSO) {
            String actual = p.getJugadorActual().getNombre();
            if (p.puedePlantarse() && azar.nextInt(3) == 0) {
                p.plantarse(actual);
            } else if (p.puedeGuardarPelusas() && azar.nextInt(3) == 0) {
                p.guardarPelusas(actual);
            } else {
                p.sacarCarta(actual);
            }
        }
        PartidaEntity entidad = cargar(p.getId());
        PartidaMapper.actualizar(entidad, p.snapshot());
        repositorio.saveAndFlush(entidad);
        em.clear();

        List<PartidaEntity> historico = repositorio.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA);
        assertEquals(List.of(p.getId()), historico.stream().map(PartidaEntity::getId).toList());
    }
}

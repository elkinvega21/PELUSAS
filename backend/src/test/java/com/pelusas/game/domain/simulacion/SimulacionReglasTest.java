package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Juega partidas completas con acciones aleatorias (válidas e inválidas, del jugador que
 * corresponde y de otros) y comprueba tras cada paso que el backend aplica las reglas:
 * turno, mazo, robo, repetición, pérdida, guardado, fin de partida y estado.
 *
 * Cada resultado se compara con un "oráculo" que recalcula la regla de forma independiente.
 */
class SimulacionReglasTest {

    private static final int VALOR_TOTAL_DEL_MAZO = 13 * (1 + 2 + 3 + 4 + 5) + 9 * (6 + 7 + 8 + 9 + 10);

    private enum Accion { SACAR, PLANTARSE, GUARDAR }

    private int perdidas;
    private int robos;
    private int guardados;
    private int plantadas;
    private int rechazadas;
    private int partidasTerminadas;
    private int turnosAjenosRechazados;

    @Test
    void lasReglasSeCumplenEnPartidasCompletas() {
        for (long semilla = 0; semilla < 400; semilla++) {
            simular(semilla);
        }
        // La prueba no es vacía: todas las situaciones importantes ocurrieron.
        assertEquals(400, partidasTerminadas);
        assertTrue(perdidas > 100, "pérdidas: " + perdidas);
        assertTrue(robos > 100, "robos: " + robos);
        assertTrue(guardados > 100, "guardados: " + guardados);
        assertTrue(plantadas > 100, "plantadas: " + plantadas);
        assertTrue(rechazadas > 100, "rechazadas: " + rechazadas);
        assertTrue(turnosAjenosRechazados > 100, "fuera de turno: " + turnosAjenosRechazados);
    }

    private void simular(long semilla) {
        Random azar = new Random(semilla);
        int cantidad = 2 + (int) (semilla % 4); // de 2 a 5 jugadores
        List<String> nombres = IntStream.range(0, cantidad).mapToObj(i -> "J" + i).toList();
        Partida p = Partida.crear(nombres, azar);
        assertTrue(p.esEstadoInicialValido());

        int pasos = 0;
        while (p.getEstado() == EstadoPartida.EN_CURSO) {
            assertTrue(++pasos < 20000, "la partida no termina (semilla " + semilla + ")");
            verificarInvariantes(p, semilla);
            intentarAccion(p, nombres, azar, semilla);
        }
        verificarInvariantes(p, semilla);
        verificarFinal(p, semilla);
        partidasTerminadas++;
    }

    // ------------------------------------------------------------------
    // Una acción aleatoria
    // ------------------------------------------------------------------

    private void intentarAccion(Partida p, List<String> nombres, Random azar, long semilla) {
        String actual = p.getJugadorActual().getNombre();
        String actor = azar.nextInt(5) == 0 ? nombres.get(azar.nextInt(nombres.size())) : actual;
        int sorteo = azar.nextInt(8);
        Accion accion = sorteo < 4 ? Accion.SACAR : sorteo < 6 ? Accion.PLANTARSE : Accion.GUARDAR;

        boolean esSuTurno = actor.equals(actual);
        boolean permitida = esSuTurno && switch (accion) {
            case SACAR -> p.puedeSacarCarta();
            case PLANTARSE -> p.puedePlantarse();
            case GUARDAR -> p.puedeGuardarPelusas();
        };

        String contexto = " (semilla " + semilla + ", " + accion + " por " + actor + ")";

        if (!permitida) {
            EstadoActual antes = p.getEstadoActual();
            assertThrows(AccionInvalidaException.class, () -> ejecutar(p, actor, accion), "debía rechazarse" + contexto);
            assertEquals(antes, p.getEstadoActual(), "una acción rechazada no debe cambiar nada" + contexto);
            rechazadas++;
            if (!esSuTurno) {
                turnosAjenosRechazados++;
            }
            return;
        }

        switch (accion) {
            case SACAR -> verificarSacar(p, actor, contexto);
            case PLANTARSE -> verificarPlantarse(p, actor, contexto);
            case GUARDAR -> verificarGuardar(p, actor, contexto);
        }
    }

    private static void ejecutar(Partida p, String actor, Accion accion) {
        switch (accion) {
            case SACAR -> p.sacarCarta(actor);
            case PLANTARSE -> p.plantarse(actor);
            case GUARDAR -> p.guardarPelusas(actor);
        }
    }

    private void verificarSacar(Partida p, String actor, String contexto) {
        Jugador activo = p.getJugadorActual();
        List<Jugador> jugadores = p.getJugadores();
        int indiceAntes = p.getIndiceJugadorActual();

        // Oráculo: lo que debe ocurrir según las reglas, calculado antes de actuar.
        List<Carta> cartasDelMazo = p.getMazo().getCartas();
        Carta siguiente = cartasDelMazo.get(cartasDelMazo.size() - 1);
        int valor = siguiente.valor();
        int enMesaAntes = activo.getCartasEnMesa().size();
        boolean repetida = activo.tieneEnMesa(valor);
        boolean debePerder = repetida && enMesaAntes + 1 >= 3;
        Map<String, Integer> cartasAntesPorOtro = new HashMap<>();
        int debenRobarse = 0;
        for (Jugador otro : jugadores) {
            if (otro != activo) {
                int cant = otro.getMesaAgrupada().getOrDefault(valor, 0);
                cartasAntesPorOtro.put(otro.getNombre(), cant);
                debenRobarse += Math.min(Jugador.MAX_CARTAS_ROBO, cant);
            }
        }
        int descarteAntes = p.getDescarte().size();
        int restantesAntes = p.getCartasRestantes();

        ResultadoSacarCarta r = p.sacarCarta(actor);

        assertEquals(siguiente, r.carta(), "debe salir la carta superior" + contexto);
        assertEquals(restantesAntes - 1, p.getCartasRestantes(), "el mazo baja en uno" + contexto);
        assertEquals(debePerder, r.turnoPerdido(), "pérdida de turno" + contexto);
        boolean terminoLaPartida = p.getEstado() == EstadoPartida.FINALIZADA;
        assertEquals(restantesAntes - 1 == 0, terminoLaPartida, "fin exacto al vaciarse el mazo" + contexto);

        if (debePerder) {
            perdidas++;
            assertTrue(r.robadas().isEmpty(), "al perder no se roba" + contexto);
            assertEquals(enMesaAntes + 1, r.descartadas().size(), "se descarta todo lo de la mesa" + contexto);
            assertEquals(descarteAntes + enMesaAntes + 1, p.getDescarte().size(), "el descarte crece" + contexto);
            assertTrue(activo.getCartasEnMesa().isEmpty(), "la mesa queda vacía" + contexto);
            if (!terminoLaPartida) {
                assertEquals((indiceAntes + 1) % jugadores.size(), p.getIndiceJugadorActual(),
                        "pasa el turno" + contexto);
                assertEquals(FaseTurno.INICIO, p.getFase(), contexto);
            }
        } else {
            assertTrue(r.descartadas().isEmpty(), contexto);
            assertEquals(descarteAntes, p.getDescarte().size(), "sin pérdida no hay descarte" + contexto);
            assertEquals(debenRobarse, r.robadas().size(), "robo automático" + contexto);
            assertTrue(r.robadas().stream().allMatch(c -> c.valor() == valor), "solo se roba el mismo valor" + contexto);
            if (debenRobarse > 0) {
                robos++;
            }
            assertEquals(indiceAntes, p.getIndiceJugadorActual(), "sigue siendo su turno" + contexto);
            if (!terminoLaPartida) {
                assertEquals(enMesaAntes + 1 + debenRobarse, activo.getCartasEnMesa().size(), contexto);
                assertEquals(FaseTurno.BUSCANDO, p.getFase(), contexto);
                for (Jugador otro : jugadores) {
                    if (otro != activo) {
                        int cantAntes = cartasAntesPorOtro.getOrDefault(otro.getNombre(), 0);
                        if (cantAntes <= Jugador.MAX_CARTAS_ROBO) {
                            assertFalse(otro.tieneEnMesa(valor), "nadie más conserva ese valor" + contexto);
                        } else {
                            assertEquals(cantAntes - Jugador.MAX_CARTAS_ROBO,
                                    otro.getMesaAgrupada().getOrDefault(valor, 0),
                                    "conserva las cartas excedentes de 3" + contexto);
                        }
                    }
                }
            }
        }
    }

    private void verificarPlantarse(Partida p, String actor, String contexto) {
        int indiceAntes = p.getIndiceJugadorActual();
        int enMesa = p.getJugadorActual().getCartasEnMesa().size();
        Jugador activo = p.getJugadorActual();
        p.plantarse(actor);
        plantadas++;
        assertEquals((indiceAntes + 1) % p.getJugadores().size(), p.getIndiceJugadorActual(), contexto);
        assertEquals(FaseTurno.INICIO, p.getFase(), contexto);
        assertEquals(enMesa, activo.getCartasEnMesa().size(), "al plantarse conserva sus cartas" + contexto);
    }

    private void verificarGuardar(Partida p, String actor, String contexto) {
        Jugador activo = p.getJugadorActual();
        int enMesa = activo.getCartasEnMesa().size();
        int enMonton = activo.getMontonPuntos().size();
        int puntosAntes = activo.puntos();
        int valorMesa = activo.getCartasEnMesa().stream().mapToInt(Carta::valor).sum();

        int guardadas = p.guardarPelusas(actor);

        guardados++;
        assertEquals(enMesa, guardadas, contexto);
        assertTrue(activo.getCartasEnMesa().isEmpty(), contexto);
        assertEquals(enMonton + enMesa, activo.getMontonPuntos().size(), contexto);
        assertEquals(puntosAntes + valorMesa, activo.puntos(), contexto);
        assertEquals(FaseTurno.INICIO, p.getFase(), "guardar no consume el turno" + contexto);
    }

    // ------------------------------------------------------------------
    // Invariantes: deben cumplirse siempre
    // ------------------------------------------------------------------

    private void verificarInvariantes(Partida p, long semilla) {
        String contexto = " (semilla " + semilla + ")";

        // Las 110 cartas se conservan y la distribución por valor no cambia.
        int[] porValor = new int[Carta.VALOR_MAXIMO + 1];
        List<Carta> todas = new ArrayList<>(p.getMazo().getCartas());
        todas.addAll(p.getDescarte());
        for (Jugador j : p.getJugadores()) {
            todas.addAll(j.getCartasEnMesa());
            todas.addAll(j.getMontonPuntos());
        }
        assertEquals(Mazo.TOTAL_CARTAS, todas.size(), "se conservan las 110 cartas" + contexto);
        todas.forEach(c -> porValor[c.valor()]++);
        for (int v = Carta.VALOR_MINIMO; v <= Carta.VALOR_MAXIMO; v++) {
            assertEquals(Mazo.copiasDelValor(v), porValor[v], "copias del valor " + v + contexto);
        }

        // Un mismo valor nunca está boca arriba delante de dos jugadores a la vez.
        for (int v = Carta.VALOR_MINIMO; v <= Carta.VALOR_MAXIMO; v++) {
            final int valor = v;
            long conEseValor = p.getJugadores().stream().filter(j -> j.tieneEnMesa(valor)).count();
            assertTrue(conEseValor <= 2, "el valor " + v + " está en mesa de varios jugadores" + contexto);
        }

        // La partida está finalizada si y solo si el mazo está vacío.
        assertEquals(p.getMazo().estaVacio(), p.getEstado() == EstadoPartida.FINALIZADA, contexto);

        // El estado informado coincide con lo que las acciones realmente permiten.
        EstadoActual e = p.getEstadoActual();
        assertEquals(p.puedeGuardarPelusas(), e.acciones().guardarPelusas(), contexto);
        assertEquals(p.puedeSacarCarta(), e.acciones().sacarCarta(), contexto);
        assertEquals(p.puedePlantarse(), e.acciones().plantarse(), contexto);
        assertEquals(p.getCartasRestantes(), e.cartasRestantes(), contexto);
    }

    private void verificarFinal(Partida p, long semilla) {
        String contexto = " (semilla " + semilla + ")";

        for (Jugador j : p.getJugadores()) {
            assertTrue(j.getCartasEnMesa().isEmpty(), "todos guardan lo que tenían delante" + contexto);
        }

        // Todos los puntos del mazo están en algún montón o en el descarte.
        int puntosJugadores = p.getJugadores().stream().mapToInt(Jugador::puntos).sum();
        int puntosDescartados = p.getDescarte().stream().mapToInt(Carta::valor).sum();
        assertEquals(VALOR_TOTAL_DEL_MAZO, puntosJugadores + puntosDescartados, contexto);

        // Ganadores: máxima puntuación y, entre esos, el mayor vector (cantidad de 1, de 2, ... de 10).
        int maxPuntos = p.getJugadores().stream().mapToInt(Jugador::puntos).max().getAsInt();
        List<Jugador> candidatos = p.getJugadores().stream().filter(j -> j.puntos() == maxPuntos).toList();
        List<Jugador> esperados = new ArrayList<>();
        for (Jugador j : candidatos) {
            boolean superado = candidatos.stream().anyMatch(otro -> comparar(otro, j) > 0);
            if (!superado) {
                esperados.add(j);
            }
        }
        assertEquals(esperados, p.getGanadores(), "ganadores" + contexto);
        assertFalse(p.getGanadores().isEmpty(), contexto);

        ResultadoPartida resultado = p.getResultado();
        assertEquals(p.getGanadores().size(), resultado.ganadores().size(), contexto);
        assertEquals(p.getGanadores().size() > 1, resultado.hayEmpate(), contexto);
        assertEquals(new EstadoActual.Acciones(false, false, false), p.getEstadoActual().acciones(), contexto);

        // Ya no se puede hacer nada, ni siquiera el jugador al que le tocaba.
        String nombre = p.getJugadorActual().getNombre();
        assertThrows(AccionInvalidaException.class, () -> p.sacarCarta(nombre), contexto);
        assertThrows(AccionInvalidaException.class, () -> p.plantarse(nombre), contexto);
        assertThrows(AccionInvalidaException.class, () -> p.guardarPelusas(nombre), contexto);
    }

    /** Comparación lexicográfica por cantidad de cartas de valor 1, 2, ... 10 en el montón. */
    private static int comparar(Jugador a, Jugador b) {
        for (int v = Carta.VALOR_MINIMO; v <= Carta.VALOR_MAXIMO; v++) {
            int diferencia = Integer.compare(a.cantidadEnMonton(v), b.cantidadEnMonton(v));
            if (diferencia != 0) {
                return diferencia;
            }
        }
        return 0;
    }
}


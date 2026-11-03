package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Pruebas con Mockito del único colaborador externo del dominio: el generador de números
 * aleatorios. El doble responde siempre "el límite menos uno": así el barajado no mueve ninguna
 * carta y el jugador inicial es el último, lo que permite comprobar que la partida usa el azar.
 */
class AzarPartidaTest {

    private static Random azarQueDevuelveElUltimo() {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenAnswer(invocacion -> invocacion.<Integer>getArgument(0) - 1);
        return random;
    }

    @Test
    void elJugadorInicialLoDecideElAzarEntreLosJugadoresDeLaPartida() {
        Random random = azarQueDevuelveElUltimo();

        Partida p = Partida.crear(List.of("Ana", "Luis", "Eva"), random);

        assertEquals(2, p.getIndiceJugadorActual());
        assertEquals("Eva", p.getJugadorActual().getNombre());
        verify(random, atLeastOnce()).nextInt(3);
    }

    @Test
    void elMazoSeBarajaUsandoElAzarRecibido() {
        Random random = azarQueDevuelveElUltimo();
        Mazo mazo = Mazo.completo();
        List<Carta> antes = List.copyOf(mazo.getCartas());

        mazo.barajar(random);

        verify(random, atLeastOnce()).nextInt(Mazo.TOTAL_CARTAS);
        assertEquals(antes, mazo.getCartas()); // con este doble el barajado no mueve nada
    }

    @Test
    void crearUnaPartidaBarajaElMazoCompletoConElAzarRecibido() {
        Random random = azarQueDevuelveElUltimo();

        Partida p = Partida.crear(List.of("Ana", "Luis"), random);

        verify(random, atLeastOnce()).nextInt(Mazo.TOTAL_CARTAS);
        assertEquals(Mazo.completo().getCartas(), p.getMazo().getCartas());
        assertTrue(p.esEstadoInicialValido());
    }
}

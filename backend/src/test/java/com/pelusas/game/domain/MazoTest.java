package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MazoTest {

    private static long contar(Mazo mazo, int valor) {
        return mazo.getCartas().stream().filter(c -> c.valor() == valor).count();
    }

    @Test
    void mazoCompletoTiene110Cartas() {
        assertEquals(110, Mazo.completo().restantes());
    }

    @Test
    void valoresDel1Al5Tienen13Copias() {
        Mazo mazo = Mazo.completo();
        for (int v = 1; v <= 5; v++) {
            assertEquals(13, contar(mazo, v), "valor " + v);
        }
    }

    @Test
    void valoresDel6Al10Tienen9Copias() {
        Mazo mazo = Mazo.completo();
        for (int v = 6; v <= 10; v++) {
            assertEquals(9, contar(mazo, v), "valor " + v);
        }
    }

    @Test
    void valoresDel1Al5SumanEn65Cartas() {
        long total = Mazo.completo().getCartas().stream().filter(c -> c.valor() <= 5).count();
        assertEquals(65, total);
    }

    @Test
    void valoresDel6Al10SumanEn45Cartas() {
        long total = Mazo.completo().getCartas().stream().filter(c -> c.valor() >= 6).count();
        assertEquals(45, total);
    }

    @Test
    void laDistribucionNoEsUniforme() {
        Mazo mazo = Mazo.completo();
        assertNotEquals(contar(mazo, 1), contar(mazo, 10));
        assertEquals(13, contar(mazo, 5));
        assertEquals(9, contar(mazo, 6));
    }

    @Test
    void copiasDelValorRechazaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Mazo.copiasDelValor(0));
        assertThrows(IllegalArgumentException.class, () -> Mazo.copiasDelValor(11));
    }

    @Test
    void barajarConservaLasMismasCartas() {
        Mazo mazo = Mazo.completo();
        mazo.barajar(new Random(42));
        assertEquals(110, mazo.restantes());
        for (int v = 1; v <= 10; v++) {
            assertEquals(Mazo.copiasDelValor(v), contar(mazo, v));
        }
    }

    @Test
    void barajarCambiaElOrden() {
        List<Carta> original = List.copyOf(Mazo.completo().getCartas());
        Mazo mazo = Mazo.completo();
        mazo.barajar(new Random(42));
        assertNotEquals(original, mazo.getCartas());
    }

    @Test
    void barajarConLaMismaSemillaEsDeterminista() {
        Mazo a = Mazo.completo();
        Mazo b = Mazo.completo();
        a.barajar(new Random(7));
        b.barajar(new Random(7));
        assertEquals(a.getCartas(), b.getCartas());
    }

    @Test
    void sacarRetiraLaUltimaCartaYReduceElMazo() {
        Mazo mazo = Mazo.completo();
        Carta esperada = mazo.getCartas().get(109);
        assertEquals(esperada, mazo.sacar());
        assertEquals(109, mazo.restantes());
    }

    @Test
    void deCartasSacaEnElOrdenIndicado() {
        Mazo mazo = Mazo.deCartas(List.of(new Carta(3), new Carta(7), new Carta(1)));
        assertEquals(new Carta(3), mazo.sacar());
        assertEquals(new Carta(7), mazo.sacar());
        assertEquals(new Carta(1), mazo.sacar());
        assertTrue(mazo.estaVacio());
    }

    @Test
    void sacarDeMazoVacioLanzaExcepcion() {
        Mazo mazo = Mazo.completo();
        for (int i = 0; i < 110; i++) {
            mazo.sacar();
        }
        assertTrue(mazo.estaVacio());
        assertThrows(IllegalStateException.class, mazo::sacar);
    }

    @Test
    void mazoNuevoNoEstaVacio() {
        assertFalse(Mazo.completo().estaVacio());
    }

    @Test
    void laVistaDeCartasNoSePuedeModificar() {
        Mazo mazo = Mazo.completo();
        assertThrows(UnsupportedOperationException.class, () -> mazo.getCartas().clear());
    }
}
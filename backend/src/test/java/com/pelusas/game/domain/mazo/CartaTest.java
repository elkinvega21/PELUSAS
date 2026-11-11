package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CartaTest {

    @Test
    void aceptaLosLimitesDelRango() {
        assertEquals(1, new Carta(1).valor());
        assertEquals(10, new Carta(10).valor());
    }

    @Test
    void rechazaValorPorDebajoDelMinimo() {
        assertThrows(IllegalArgumentException.class, () -> new Carta(0));
    }

    @Test
    void rechazaValorPorEncimaDelMaximo() {
        assertThrows(IllegalArgumentException.class, () -> new Carta(11));
    }

    @Test
    void cartasConMismoValorSonIguales() {
        assertEquals(new Carta(4), new Carta(4));
    }
}

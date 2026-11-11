package com.pelusas.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas del mecanismo de desempate (req. 9): primero puntos, luego cantidad de 1, 2, 3... 10. */
class DesempateTest {

    /** Jugador cuyo montón de puntos contiene exactamente las cartas indicadas. */
    private static Jugador jugador(String nombre, int... valores) {
        Jugador j = new Jugador(nombre);
        for (int v : valores) {
            j.recibirEnMesa(new Carta(v));
        }
        j.guardarMesa();
        return j;
    }

    private static int[] repetir(int valor, int veces) {
        int[] r = new int[veces];
        java.util.Arrays.fill(r, valor);
        return r;
    }

    // ---- sin empate ----

    @Test
    void gana_el_de_mayor_puntaje() {
        Jugador a = jugador("A", 10);
        Jugador b = jugador("B", 5, 4);
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void el_puntaje_decide_aunque_otro_tenga_mas_cartas_de_valor_1() {
        Jugador a = jugador("A", 9);
        Jugador b = jugador("B", 1, 1, 1, 1, 1);
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void el_ganador_puede_ser_cualquiera_de_la_lista() {
        Jugador a = jugador("A", 2);
        Jugador b = jugador("B", 8);
        Jugador c = jugador("C", 5);
        assertEquals(List.of(b), Desempate.determinarGanadores(List.of(a, b, c)));
    }

    // ---- desempate por valor ----

    @Test
    void con_empate_gana_quien_tiene_mas_cartas_de_valor_1() {
        Jugador a = jugador("A", 1, 1, 4); // 6 puntos, dos unos
        Jugador b = jugador("B", 1, 5);    // 6 puntos, un uno
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(b, a)));
    }

    @Test
    void si_los_unos_empatan_se_comparan_los_doses() {
        Jugador a = jugador("A", 1, 2, 2); // 5 puntos: un 1, dos 2
        Jugador b = jugador("B", 1, 4);    // 5 puntos: un 1, ningún 2
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void sigue_comparando_hasta_encontrar_la_diferencia() {
        Jugador a = jugador("A", 3, 3); // 6 puntos: dos 3
        Jugador b = jugador("B", 6);    // 6 puntos: ningún 3
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void puede_llegar_a_decidirse_en_valores_altos() {
        Jugador a = jugador("A", repetir(9, 10));  // 90 puntos: diez 9
        Jugador b = jugador("B", repetir(10, 9));  // 90 puntos: nueve 10
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void el_valor_mas_bajo_tiene_prioridad() {
        // Ambos con 12 puntos: A tiene más 1 (gana aunque B tenga más 4).
        Jugador a = jugador("A", 1, 1, 10);
        Jugador b = jugador("B", 4, 4, 4);
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b)));
    }

    // ---- varios jugadores ----

    @Test
    void solo_compiten_los_empatados_en_puntos() {
        // C tiene más unos pero menos puntos: no entra al desempate.
        Jugador a = jugador("A", 5, 1);
        Jugador b = jugador("B", 6);
        Jugador c = jugador("C", 1, 1, 1);
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b, c)));
    }

    @Test
    void con_tres_empatados_se_eliminan_los_que_tienen_menos_cartas_del_valor() {
        Jugador a = jugador("A", 1, 1, 4); // dos 1
        Jugador b = jugador("B", 1, 5);    // un 1
        Jugador c = jugador("C", 6);       // ningún 1
        assertEquals(List.of(a), Desempate.determinarGanadores(List.of(a, b, c)));
    }

    @Test
    void si_dos_empatan_en_lo_mas_alto_siguen_ambos_y_el_tercero_queda_fuera() {
        Jugador a = jugador("A", 1, 1, 4); // dos 1, un 4
        Jugador b = jugador("B", 1, 1, 4); // igual que A
        Jugador c = jugador("C", 1, 5);    // un 1
        List<Jugador> ganadores = Desempate.determinarGanadores(List.of(a, b, c));
        assertEquals(List.of(a, b), ganadores);
    }

    @Test
    void entre_los_que_siguen_se_continua_con_el_siguiente_valor() {
        // Los tres suman 8. En los 1: A y B tienen dos y C uno (C queda fuera).
        // En los 2: B tiene tres y A ninguno, así que gana B.
        Jugador a = jugador("A", 1, 1, 6);
        Jugador b = jugador("B", 1, 1, 2, 2, 2);
        Jugador c = jugador("C", 1, 7);
        assertEquals(List.of(b), Desempate.determinarGanadores(List.of(a, b, c)));
    }

    // ---- empate total ----

    @Test
    void si_todo_coincide_hay_empate_y_ganan_todos_los_igualados() {
        Jugador a = jugador("A", 3, 3);
        Jugador b = jugador("B", 3, 3);
        assertEquals(List.of(a, b), Desempate.determinarGanadores(List.of(a, b)));
    }

    @Test
    void el_empate_total_excluye_a_quien_tiene_menos_puntos() {
        Jugador a = jugador("A", 5);
        Jugador b = jugador("B", 5);
        Jugador c = jugador("C", 2);
        assertEquals(List.of(a, b), Desempate.determinarGanadores(List.of(a, b, c)));
    }

    @Test
    void sin_cartas_todos_empatan() {
        Jugador a = jugador("A");
        Jugador b = jugador("B");
        assertEquals(List.of(a, b), Desempate.determinarGanadores(List.of(a, b)));
    }

    // ---- Jugador ----

    @Test
    void cantidadEnMonton_cuenta_solo_el_valor_pedido() {
        Jugador j = jugador("A", 2, 2, 5);
        assertEquals(2, j.cantidadEnMonton(2));
        assertEquals(1, j.cantidadEnMonton(5));
        assertEquals(0, j.cantidadEnMonton(9));
    }
}

package com.pelusas.game.domain;

/**
 * Carta del juego. Su única propiedad es el valor (1 a 10).
 * Es inmutable: dos cartas con el mismo valor son equivalentes.
 */
public record Carta(int valor) {

    public static final int VALOR_MINIMO = 1;
    public static final int VALOR_MAXIMO = 10;

    public Carta {
        if (valor < VALOR_MINIMO || valor > VALOR_MAXIMO) {
            throw new IllegalArgumentException(
                    "El valor de la carta debe estar entre " + VALOR_MINIMO + " y " + VALOR_MAXIMO + ": " + valor);
        }
    }
}

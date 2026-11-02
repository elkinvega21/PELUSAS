package com.pelusas.game.domain;

/** Se lanza cuando se intenta una acción que las reglas no permiten en ese momento. */
public class AccionInvalidaException extends RuntimeException {

    public AccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}

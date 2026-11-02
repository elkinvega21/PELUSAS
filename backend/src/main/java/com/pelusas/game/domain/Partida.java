package com.pelusas.game.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Partida de Pelusas.
 *
 * Creación (req. 5.1): se crea únicamente mediante {@link #crear}.
 * Turno (req. 7): {@link #guardarPelusas}, {@link #sacarCarta} y {@link #plantarse}.
 * Toda acción valida que el jugador que la pide sea el jugador activo.
 */
public class Partida {

    public static final int MIN_JUGADORES = 2;

    private final UUID id;
    private final LocalDateTime fechaCreacion;
    private final List<Jugador> jugadores;
    private final Mazo mazo;
    private int indiceJugadorActual;
    private FaseTurno fase;
    private EstadoPartida estado;

    private Partida(List<Jugador> jugadores, Mazo mazo, int indiceInicial) {
        this.id = UUID.randomUUID();
        this.fechaCreacion = LocalDateTime.now();
        this.jugadores = jugadores;
        this.mazo = mazo;
        this.indiceJugadorActual = indiceInicial;
        this.fase = FaseTurno.INICIO;
        this.estado = EstadoPartida.EN_CURSO;
    }

    public static Partida crear(List<String> nombres) {
        return crear(nombres, new Random());
    }

    public static Partida crear(List<String> nombres, Random random) {
        List<Jugador> jugadores = definirJugadores(nombres);

        Mazo mazo = Mazo.completo();
        mazo.barajar(random);

        int indiceInicial = random.nextInt(jugadores.size());

        return new Partida(jugadores, mazo, indiceInicial);
    }

    /** Crea una partida con un mazo y jugador inicial dados (para pruebas deterministas). */
    static Partida conMazo(List<String> nombres, Mazo mazo, int indiceInicial) {
        return new Partida(definirJugadores(nombres), mazo, indiceInicial);
    }

    private static List<Jugador> definirJugadores(List<String> nombres) {
        if (nombres == null || nombres.size() < MIN_JUGADORES) {
            throw new IllegalArgumentException("Se requieren al menos " + MIN_JUGADORES + " jugadores");
        }

        List<Jugador> jugadores = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (String nombre : nombres) {
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre de un jugador no puede estar vacío");
            }
            String limpio = nombre.trim();
            if (!vistos.add(limpio.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Nombre de jugador repetido: " + limpio);
            }
            jugadores.add(new Jugador(limpio));
        }
        return jugadores;
    }

    // ------------------------------------------------------------------
    // Turno
    // ------------------------------------------------------------------

    /**
     * Paso 1 — Guardar pelusas. Solo al inicio del turno (antes de sacar carta):
     * pasa las cartas de la mesa del jugador a su montón de puntos, donde quedan seguras.
     *
     * @return cantidad de cartas guardadas
     */
    public int guardarPelusas(String nombreJugador) {
        validarTurno(nombreJugador);
        if (fase != FaseTurno.INICIO) {
            throw new AccionInvalidaException("Solo se pueden guardar pelusas al inicio del turno");
        }
        if (getJugadorActual().getCartasEnMesa().isEmpty()) {
            throw new AccionInvalidaException("No hay pelusas para guardar");
        }
        return getJugadorActual().guardarMesa();
    }

    /**
     * Pasos 2 y 3 — Buscar pelusas y robar. Saca una carta del mazo, la deja visible delante
     * del jugador y le pasa automáticamente todas las cartas del mismo valor que otros
     * jugadores tengan boca arriba.
     */
    public ResultadoSacarCarta sacarCarta(String nombreJugador) {
        validarTurno(nombreJugador);
        if (mazo.estaVacio()) {
            throw new AccionInvalidaException("El mazo está vacío");
        }

        Jugador actual = getJugadorActual();
        Carta carta = mazo.sacar();
        actual.recibirEnMesa(carta);

        List<Carta> robadas = new ArrayList<>();
        for (Jugador otro : jugadores) {
            if (otro != actual) {
                robadas.addAll(otro.retirarDeMesa(carta.valor()));
            }
        }
        robadas.forEach(actual::recibirEnMesa);

        fase = FaseTurno.BUSCANDO;
        return new ResultadoSacarCarta(carta, List.copyOf(robadas));
    }

    /**
     * Finaliza voluntariamente el turno. Las cartas quedan boca arriba delante del jugador
     * hasta su próximo turno. Requiere haber sacado al menos una carta.
     */
    public void plantarse(String nombreJugador) {
        validarTurno(nombreJugador);
        if (fase != FaseTurno.BUSCANDO) {
            throw new AccionInvalidaException("Debes sacar al menos una carta antes de plantarte");
        }
        pasarTurno();
    }

    private void validarTurno(String nombreJugador) {
        if (nombreJugador == null
                || !getJugadorActual().getNombre().equalsIgnoreCase(nombreJugador.trim())) {
            throw new AccionInvalidaException("No es el turno de " + nombreJugador);
        }
    }

    private void pasarTurno() {
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
        fase = FaseTurno.INICIO;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /**
     * Comprueba que la partida cumple el estado inicial válido: mazo completo de 110 cartas,
     * ningún jugador con cartas ni puntos, jugador inicial existente, turno en fase INICIO
     * y partida en curso.
     */
    public boolean esEstadoInicialValido() {
        boolean mazoCompleto = mazo.restantes() == Mazo.TOTAL_CARTAS;
        boolean jugadoresLimpios = jugadores.stream()
                .allMatch(j -> j.getCartasEnMesa().isEmpty() && j.getMontonPuntos().isEmpty());
        boolean inicialValido = indiceJugadorActual >= 0 && indiceJugadorActual < jugadores.size();
        return mazoCompleto
                && jugadoresLimpios
                && inicialValido
                && jugadores.size() >= MIN_JUGADORES
                && fase == FaseTurno.INICIO
                && estado == EstadoPartida.EN_CURSO;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public Mazo getMazo() {
        return mazo;
    }

    public int getIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public Jugador getJugadorActual() {
        return jugadores.get(indiceJugadorActual);
    }

    public FaseTurno getFase() {
        return fase;
    }

    public EstadoPartida getEstado() {
        return estado;
    }
}
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
 * Pérdida del turno (req. 8): al repetir un valor teniendo 3 o más cartas delante.
 * Finalización (req. 9): al agotarse el mazo se guardan las cartas en mesa, se suman los
 * puntos y se determina el ganador (con desempate).
 * Toda acción valida que el jugador que la pide sea el jugador activo.
 */
public class Partida {

    public static final int MIN_JUGADORES = 2;
    /** Con esta cantidad de cartas delante (incluida la repetida), repetir un valor hace perder el turno. */
    public static final int CARTAS_PARA_PERDER_TURNO = 3;

    private final UUID id;
    private final LocalDateTime fechaCreacion;
    private final List<Jugador> jugadores;
    private final Mazo mazo;
    private final List<Carta> descarte = new ArrayList<>();
    private List<Jugador> ganadores = List.of();
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
     * Pasos 2 y 3 — Buscar pelusas y robar. Saca una carta del mazo y la deja visible delante
     * del jugador.
     *
     * Si el valor ya estaba delante del jugador y con esta carta tiene 3 o más cartas en total,
     * pierde el turno: todas sus cartas van al descarte (no al montón de puntos) y el turno
     * pasa al siguiente jugador.
     *
     * En caso contrario, le pasa automáticamente todas las cartas del mismo valor que otros
     * jugadores tengan boca arriba.
     */
    public ResultadoSacarCarta sacarCarta(String nombreJugador) {
        validarTurno(nombreJugador);
        if (mazo.estaVacio()) {
            throw new AccionInvalidaException("El mazo está vacío");
        }

        Jugador actual = getJugadorActual();
        Carta carta = mazo.sacar();
        boolean repetida = actual.tieneEnMesa(carta.valor());
        actual.recibirEnMesa(carta);

        ResultadoSacarCarta resultado;
        if (repetida && actual.getCartasEnMesa().size() >= CARTAS_PARA_PERDER_TURNO) {
            List<Carta> descartadas = actual.vaciarMesa();
            descarte.addAll(descartadas);
            pasarTurno();
            resultado = new ResultadoSacarCarta(carta, List.of(), true, descartadas);
        } else {
            List<Carta> robadas = new ArrayList<>();
            for (Jugador otro : jugadores) {
                if (otro != actual) {
                    robadas.addAll(otro.retirarDeMesa(carta.valor()));
                }
            }
            robadas.forEach(actual::recibirEnMesa);
            fase = FaseTurno.BUSCANDO;
            resultado = new ResultadoSacarCarta(carta, List.copyOf(robadas), false, List.of());
        }

        if (mazo.estaVacio()) {
            finalizar();
        }
        return resultado;
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

    /**
     * Finaliza la partida: cada jugador guarda las cartas que tenga delante, se suman los
     * puntos y se determina el ganador (aplicando el desempate si hace falta).
     */
    private void finalizar() {
        jugadores.forEach(Jugador::guardarMesa);
        ganadores = Desempate.determinarGanadores(jugadores);
        estado = EstadoPartida.FINALIZADA;
    }

    private void validarTurno(String nombreJugador) {
        if (estado == EstadoPartida.FINALIZADA) {
            throw new AccionInvalidaException("La partida ya finalizó");
        }
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

    /** ¿Puede el jugador actual guardar sus pelusas ahora? (al inicio del turno y con cartas delante) */
    public boolean puedeGuardarPelusas() {
        return estado == EstadoPartida.EN_CURSO
                && fase == FaseTurno.INICIO
                && !getJugadorActual().getCartasEnMesa().isEmpty();
    }

    /**
     * ¿Puede el jugador actual sacar una carta ahora? Basta con que queden cartas: la partida
     * finaliza exactamente cuando el mazo se vacía, así que una partida terminada nunca puede sacar.
     */
    public boolean puedeSacarCarta() {
        return !mazo.estaVacio();
    }

    /** ¿Puede el jugador actual plantarse ahora? (debe haber sacado al menos una carta en el turno) */
    public boolean puedePlantarse() {
        return estado == EstadoPartida.EN_CURSO && fase == FaseTurno.BUSCANDO;
    }

    /** Estado completo de la partida, con las acciones permitidas, calculado por el backend. */
    public EstadoActual getEstadoActual() {
        List<EstadoActual.JugadorEstado> vistaJugadores = jugadores.stream()
                .map(j -> new EstadoActual.JugadorEstado(
                        j.getNombre(),
                        j.getMesaAgrupada(),
                        j.getCartasEnMesa().size(),
                        j.getMontonPuntos().size(),
                        j.puntos()))
                .toList();
        return new EstadoActual(
                id,
                estado,
                fase,
                getJugadorActual().getNombre(),
                mazo.restantes(),
                descarte.size(),
                vistaJugadores,
                new EstadoActual.Acciones(puedeGuardarPelusas(), puedeSacarCarta(), puedePlantarse()),
                ganadores.stream().map(Jugador::getNombre).toList());
    }

    /**
     * Comprueba que la partida cumple el estado inicial válido: mazo completo de 110 cartas,
     * ningún jugador con cartas ni puntos, jugador inicial existente, turno en fase INICIO
     * y partida en curso.
     */
    public boolean esEstadoInicialValido() {
        boolean mazoCompleto = mazo.restantes() == Mazo.TOTAL_CARTAS;
        boolean jugadoresLimpios = jugadores.stream()
                .allMatch(j -> j.getCartasEnMesa().isEmpty() && j.getMontonPuntos().isEmpty());
        boolean sinDescarte = descarte.isEmpty();
        boolean inicialValido = indiceJugadorActual >= 0 && indiceJugadorActual < jugadores.size();
        return mazoCompleto
                && jugadoresLimpios
                && sinDescarte
                && inicialValido
                && jugadores.size() >= MIN_JUGADORES
                && fase == FaseTurno.INICIO
                && estado == EstadoPartida.EN_CURSO;
    }

    /** Jugadores ganadores: uno solo salvo que el empate persista tras el desempate. Vacío si no ha terminado. */
    public List<Jugador> getGanadores() {
        return Collections.unmodifiableList(ganadores);
    }

    /** Resultado final. Solo disponible cuando la partida ha finalizado. */
    public ResultadoPartida getResultado() {
        if (estado != EstadoPartida.FINALIZADA) {
            throw new AccionInvalidaException("La partida aún no ha finalizado");
        }
        List<ResultadoJugador> resultados = jugadores.stream()
                .map(j -> new ResultadoJugador(
                        j.getNombre(), j.getMontonPuntos().size(), j.puntos(), ganadores.contains(j)))
                .toList();
        return new ResultadoPartida(resultados);
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

    /** Acceso interno al mazo (no público: sus mutadores no deben usarse fuera de las reglas). */
    Mazo getMazo() {
        return mazo;
    }

    public int getCartasRestantes() {
        return mazo.restantes();
    }

    public int getIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public Jugador getJugadorActual() {
        return jugadores.get(indiceJugadorActual);
    }

    /** Cartas descartadas por pérdidas de turno (fuera de juego). */
    public List<Carta> getDescarte() {
        return Collections.unmodifiableList(descarte);
    }

    public FaseTurno getFase() {
        return fase;
    }

    public EstadoPartida getEstado() {
        return estado;
    }
}
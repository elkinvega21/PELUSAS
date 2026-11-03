package com.pelusas.game.repository;

import com.pelusas.game.domain.Carta;
import com.pelusas.game.domain.PartidaSnapshot;
import com.pelusas.game.domain.PartidaSnapshot.JugadorSnapshot;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Convierte entre la foto del dominio y las entidades JPA. No contiene reglas del juego. */
public final class PartidaMapper {

    private PartidaMapper() {
    }

    /** Crea las entidades (partida y sus jugadores) a partir de la foto. */
    public static PartidaEntity aEntidad(PartidaSnapshot foto) {
        PartidaEntity entidad = PartidaEntity.nueva();
        entidad.setId(foto.id());
        entidad.setFechaCreacion(foto.fechaCreacion());
        for (int orden = 0; orden < foto.jugadores().size(); orden++) {
            JugadorEntity jugador = JugadorEntity.nuevo();
            jugador.setOrden(orden);
            jugador.setNombre(foto.jugadores().get(orden).nombre());
            entidad.agregarJugador(jugador);
        }
        actualizar(entidad, foto);
        return entidad;
    }

    /**
     * Actualiza una entidad existente con el nuevo estado de la partida, modificando sus filas
     * en lugar de reemplazarlas. La partida y sus jugadores deben ser los mismos.
     */
    public static void actualizar(PartidaEntity entidad, PartidaSnapshot foto) {
        if (!foto.id().equals(entidad.getId())) {
            throw new IllegalArgumentException("La foto no corresponde a la partida " + entidad.getId());
        }
        List<JugadorEntity> jugadores = ordenados(entidad);
        if (jugadores.size() != foto.jugadores().size()) {
            throw new IllegalArgumentException("La cantidad de jugadores no coincide");
        }

        entidad.setEstado(foto.estado());
        entidad.setFase(foto.fase());
        entidad.setIndiceJugadorActual(foto.indiceJugadorActual());
        entidad.setMazo(codificar(foto.mazo()));
        entidad.setDescarte(codificar(foto.descarte()));

        for (int i = 0; i < jugadores.size(); i++) {
            JugadorEntity jugador = jugadores.get(i);
            JugadorSnapshot datos = foto.jugadores().get(i);
            if (!jugador.getNombre().equals(datos.nombre())) {
                throw new IllegalArgumentException("El jugador " + (i + 1) + " no coincide: "
                        + jugador.getNombre() + " / " + datos.nombre());
            }
            jugador.setMesa(codificar(datos.mesa()));
            jugador.setMonton(codificar(datos.monton()));
            jugador.setCartasAcumuladas(datos.cartasAcumuladas());
            jugador.setPuntos(datos.puntos());
            jugador.setGanador(datos.ganador());
        }
    }

    /** Reconstruye la foto del dominio a partir de las entidades guardadas. */
    public static PartidaSnapshot aSnapshot(PartidaEntity entidad) {
        List<JugadorSnapshot> jugadores = ordenados(entidad).stream()
                .map(j -> new JugadorSnapshot(j.getNombre(), decodificar(j.getMesa()),
                        decodificar(j.getMonton()), j.isGanador()))
                .toList();
        return new PartidaSnapshot(entidad.getId(), entidad.getFechaCreacion(), entidad.getEstado(),
                entidad.getFase(), entidad.getIndiceJugadorActual(), decodificar(entidad.getMazo()),
                decodificar(entidad.getDescarte()), jugadores);
    }

    private static List<JugadorEntity> ordenados(PartidaEntity entidad) {
        return entidad.getJugadores().stream().sorted(Comparator.comparingInt(JugadorEntity::getOrden)).toList();
    }

    /** [4, 6, 9] → "4,6,9" (lista vacía → ""). */
    static String codificar(List<Carta> cartas) {
        return cartas.stream().map(c -> String.valueOf(c.valor())).collect(Collectors.joining(","));
    }

    /** "4,6,9" → [4, 6, 9] ("" o nulo → lista vacía). Lanza IllegalArgumentException si el texto es inválido. */
    static List<Carta> decodificar(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(texto.split(","))
                .map(valor -> new Carta(Integer.parseInt(valor.trim())))
                .toList();
    }
}

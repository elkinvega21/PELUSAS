package com.pelusas.game.repository;

import com.pelusas.game.domain.EstadoPartida;
import com.pelusas.game.domain.FaseTurno;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Partida guardada: su estado completo (para poder retomarla) y, si está finalizada, su registro
 * en el histórico. Las cartas se guardan como texto de valores separados por comas ("4,6,9").
 */
@Entity
@Table(name = "partidas")
public class PartidaEntity {

    @Id
    private UUID id;

    /** Control de concurrencia optimista: evita que dos actualizaciones simultáneas se pisen. */
    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartida estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaseTurno fase;

    @Column(nullable = false)
    private int indiceJugadorActual;

    /** Montón de polvo, en el orden en que se sacarán las cartas. */
    @Column(nullable = false, length = 512)
    private String mazo = "";

    @Column(nullable = false, length = 512)
    private String descarte = "";

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<JugadorEntity> jugadores = new ArrayList<>();

    protected PartidaEntity() {
        // requerido por JPA
    }

    /** Agrega un jugador y mantiene coherente el lado inverso de la relación. */
    public void agregarJugador(JugadorEntity jugador) {
        jugador.setPartida(this);
        jugadores.add(jugador);
    }

    public static PartidaEntity nueva() {
        return new PartidaEntity();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public FaseTurno getFase() {
        return fase;
    }

    public void setFase(FaseTurno fase) {
        this.fase = fase;
    }

    public int getIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public void setIndiceJugadorActual(int indiceJugadorActual) {
        this.indiceJugadorActual = indiceJugadorActual;
    }

    public String getMazo() {
        return mazo;
    }

    public void setMazo(String mazo) {
        this.mazo = mazo;
    }

    public String getDescarte() {
        return descarte;
    }

    public void setDescarte(String descarte) {
        this.descarte = descarte;
    }

    public List<JugadorEntity> getJugadores() {
        return jugadores;
    }
}

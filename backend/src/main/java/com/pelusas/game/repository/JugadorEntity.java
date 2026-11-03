package com.pelusas.game.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Jugador de una partida guardada. Además de las cartas, guarda los datos que exige el histórico:
 * cartas acumuladas, puntuación final e indicador de ganador.
 */
@Entity
@Table(name = "jugadores", uniqueConstraints = @UniqueConstraint(columnNames = {"partida_id", "orden"}))
public class JugadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    /** Posición en el orden de turnos (0, 1, 2...). */
    @Column(nullable = false)
    private int orden;

    @Column(nullable = false, length = 60)
    private String nombre;

    /** Cartas boca arriba delante del jugador. */
    @Column(nullable = false, length = 512)
    private String mesa = "";

    /** Montón de puntos. */
    @Column(nullable = false, length = 512)
    private String monton = "";

    @Column(nullable = false)
    private int cartasAcumuladas;

    @Column(nullable = false)
    private int puntos;

    @Column(nullable = false)
    private boolean ganador;

    protected JugadorEntity() {
        // requerido por JPA
    }

    public static JugadorEntity nuevo() {
        return new JugadorEntity();
    }

    public Long getId() {
        return id;
    }

    public PartidaEntity getPartida() {
        return partida;
    }

    void setPartida(PartidaEntity partida) {
        this.partida = partida;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getMonton() {
        return monton;
    }

    public void setMonton(String monton) {
        this.monton = monton;
    }

    public int getCartasAcumuladas() {
        return cartasAcumuladas;
    }

    public void setCartasAcumuladas(int cartasAcumuladas) {
        this.cartasAcumuladas = cartasAcumuladas;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public boolean isGanador() {
        return ganador;
    }

    public void setGanador(boolean ganador) {
        this.ganador = ganador;
    }
}

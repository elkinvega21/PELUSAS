package com.pelusas.game.repository;

import com.pelusas.game.domain.EstadoPartida;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<PartidaEntity, UUID> {

    /** Trae la partida junto con sus jugadores en una sola consulta. */
    @Override
    @EntityGraph(attributePaths = "jugadores")
    Optional<PartidaEntity> findById(UUID id);

    /** Partidas en un estado dado, de la más reciente a la más antigua (el histórico usa FINALIZADA). */
    @EntityGraph(attributePaths = "jugadores")
    List<PartidaEntity> findByEstadoOrderByFechaCreacionDesc(EstadoPartida estado);
}

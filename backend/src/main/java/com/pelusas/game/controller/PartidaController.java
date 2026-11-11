package com.pelusas.game.controller;

import com.pelusas.game.domain.EstadoActual;
import com.pelusas.game.domain.EstadoPartida;
import com.pelusas.game.domain.Partida;
import com.pelusas.game.domain.ResultadoSacarCarta;
import com.pelusas.game.repository.PartidaEntity;
import com.pelusas.game.repository.PartidaMapper;
import com.pelusas.game.repository.PartidaRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/partidas")
@CrossOrigin(origins = "*")
public class PartidaController {

    private final PartidaRepository partidaRepository;

    public PartidaController(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    public record CrearPartidaRequest(List<String> jugadores) {}
    public record AccionRequest(String jugador) {}

    /** Crea una nueva partida con los jugadores indicados y la persiste. */
    @PostMapping
    @Transactional
    public ResponseEntity<EstadoActual> crearPartida(@RequestBody CrearPartidaRequest request) {
        if (request == null || request.jugadores() == null || request.jugadores().size() < Partida.MIN_JUGADORES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requieren al menos 2 jugadores");
        }

        Partida partida = Partida.crear(request.jugadores());
        PartidaEntity entity = PartidaMapper.aEntidad(partida.snapshot());
        partidaRepository.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(partida.getEstadoActual());
    }

    /** Consulta el estado actual de una partida por su ID. */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<EstadoActual> obtenerPartida(@PathVariable UUID id) {
        PartidaEntity entity = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        Partida partida = Partida.restaurar(PartidaMapper.aSnapshot(entity));
        return ResponseEntity.ok(partida.getEstadoActual());
    }

    /** Realiza la acción de sacar carta. */
    @PostMapping("/{id}/sacar-carta")
    @Transactional
    public ResponseEntity<ResultadoSacarCarta> sacarCarta(@PathVariable UUID id, @RequestBody AccionRequest req) {
        PartidaEntity entity = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        Partida partida = Partida.restaurar(PartidaMapper.aSnapshot(entity));
        ResultadoSacarCarta resultado = partida.sacarCarta(req.jugador());
        PartidaMapper.actualizar(entity, partida.snapshot());
        partidaRepository.save(entity);

        return ResponseEntity.ok(resultado);
    }

    /** Realiza la acción de guardar pelusas. */
    @PostMapping("/{id}/guardar")
    @Transactional
    public ResponseEntity<Map<String, Object>> guardarPelusas(@PathVariable UUID id, @RequestBody AccionRequest req) {
        PartidaEntity entity = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        Partida partida = Partida.restaurar(PartidaMapper.aSnapshot(entity));
        int guardadas = partida.guardarPelusas(req.jugador());
        PartidaMapper.actualizar(entity, partida.snapshot());
        partidaRepository.save(entity);

        return ResponseEntity.ok(Map.of("guardadas", guardadas, "estado", partida.getEstadoActual()));
    }

    /** Realiza la acción de plantarse. */
    @PostMapping("/{id}/plantarse")
    @Transactional
    public ResponseEntity<EstadoActual> plantarse(@PathVariable UUID id, @RequestBody AccionRequest req) {
        PartidaEntity entity = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        Partida partida = Partida.restaurar(PartidaMapper.aSnapshot(entity));
        partida.plantarse(req.jugador());
        PartidaMapper.actualizar(entity, partida.snapshot());
        partidaRepository.save(entity);

        return ResponseEntity.ok(partida.getEstadoActual());
    }

    /** Consulta el histórico de partidas finalizadas. */
    @GetMapping("/historico")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EstadoActual>> obtenerHistorico() {
        List<PartidaEntity> finalizadas = partidaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoPartida.FINALIZADA);
        List<EstadoActual> estados = finalizadas.stream()
                .map(e -> Partida.restaurar(PartidaMapper.aSnapshot(e)).getEstadoActual())
                .toList();

        return ResponseEntity.ok(estados);
    }
}


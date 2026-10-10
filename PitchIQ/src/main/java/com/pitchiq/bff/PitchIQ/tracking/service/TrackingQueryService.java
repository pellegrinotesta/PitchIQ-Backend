package com.pitchiq.bff.PitchIQ.tracking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.dto.*;
import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingFrame;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingPartita;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingSequenza;
import com.pitchiq.bff.PitchIQ.tracking.repository.TrackingFrameRepository;
import com.pitchiq.bff.PitchIQ.tracking.repository.TrackingPartitaRepository;
import com.pitchiq.bff.PitchIQ.tracking.repository.TrackingSequenzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingQueryService {

    private final TrackingPartitaRepository partitaRepository;
    private final TrackingSequenzaRepository sequenzaRepository;
    private final TrackingFrameRepository frameRepository;
    private final ObjectMapper objectMapper;

    public List<PartitaDto> partite() {
        Map<Long, long[]> conteggi = new HashMap<>(); // [possessi, superate]
        for (Object[] r : sequenzaRepository.conteggiPerPartita()) {
            long[] c = conteggi.computeIfAbsent((Long) r[0], k -> new long[2]);
            long n = ((Number) r[2]).longValue();
            c[0] += n;
            if (r[1] == EsitoPressione.SUPERATA) c[1] += n;
        }
        return partitaRepository.findAllByOrderByDataPartitaDesc().stream()
                .map(p -> {
                    long[] c = conteggi.getOrDefault(p.getMatchId(), new long[2]);
                    return PartitaDto.from(p, c[0], c[1]);
                })
                .toList();
    }

    public PartitaDettaglioDto dettaglio(Long matchId) {
        TrackingPartita p = partitaRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partita " + matchId + " non trovata"));
        List<AggregatoEsitoDto> aggregati = aggregati(matchId);
        long possessi = aggregati.stream().mapToLong(AggregatoEsitoDto::possessi).sum();
        long superate = aggregati.stream().filter(a -> a.esito() == EsitoPressione.SUPERATA)
                .mapToLong(AggregatoEsitoDto::possessi).sum();

        // le due squadre come compaiono nelle sequenze (id SkillCorner + nome)
        Map<Long, String> squadre = new LinkedHashMap<>();
        sequenzaRepository.cerca(matchId, null, null, false)
                .forEach(s -> squadre.putIfAbsent(s.getTeamId(), s.getSquadra()));

        return new PartitaDettaglioDto(
                PartitaDto.from(p, possessi, superate),
                squadre.entrySet().stream().map(e -> new PartitaDettaglioDto.SquadraDto(e.getKey(), e.getValue())).toList(),
                json(p.getGiocatoriJson()),
                aggregati);
    }

    public List<SequenzaDto> sequenze(Long matchId, EsitoPressione esito, Long teamId, boolean soloAnimate) {
        return sequenzaRepository.cerca(matchId, esito, teamId, soloAnimate).stream().map(SequenzaDto::from).toList();
    }

    /** matchId null = tutte le partite caricate. */
    public List<AggregatoEsitoDto> aggregati(Long matchId) {
        return sequenzaRepository.aggregatiPerEsito(matchId).stream()
                .map(AggregatoEsitoDto::fromRow)
                .sorted(Comparator.comparing(AggregatoEsitoDto::esito))
                .toList();
    }

    public AnimazioneDto animazione(Long sequenzaId) {
        TrackingSequenza s = sequenzaRepository.findById(sequenzaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sequenza " + sequenzaId + " non trovata"));
        TrackingFrame f = frameRepository.findById(sequenzaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Per questa sequenza non c'è l'animazione: la partita è stata caricata senza il file di tracking."));
        return new AnimazioneDto(SequenzaDto.from(s), json(f.getPressoriJson()), f.getPressInizio(),
                f.getPressFine(), json(f.getFramesJson()));
    }

    @Transactional
    public void elimina(Long matchId) {
        if (partitaRepository.eliminaPartita(matchId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partita " + matchId + " non trovata");
        }
    }

    private JsonNode json(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON non valido in tracking", e);
        }
    }
}

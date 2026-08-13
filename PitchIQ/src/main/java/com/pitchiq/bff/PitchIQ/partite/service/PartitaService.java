package com.pitchiq.bff.PitchIQ.partite.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.partite.dto.*;
import com.pitchiq.bff.PitchIQ.partite.model.*;
import com.pitchiq.bff.PitchIQ.partite.repository.HeatmapRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.ScoutingRepository;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartitaService {

    private final PartitaRepository partitaRepository;
    private final HeatmapRepository heatmapRepository;
    private final ScoutingRepository scoutingRepository;
    private final GiocatoreRepository giocatoreRepository;

    // ===== PARTITE =====

    public List<PartitaDto> findAll() {
        return partitaRepository.findAllOrdered().stream()
                .map(p -> toDto(p, List.of(), null))
                .toList();
    }

    public PartitaDto findById(Long id) {
        Partita p = partitaRepository.findByIdWithEventi(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partita non trovata: " + id));

        List<Long> gIds = p.getEventi().stream()
                .filter(e -> e.getGiocatoreId() != null)
                .map(EventoPartita::getGiocatoreId)
                .distinct().toList();

        Map<Long, Giocatore> gMap = giocatoreRepository.findAllById(gIds).stream()
                .collect(Collectors.toMap(Giocatore::getId, g -> g));

        List<EventoDto> eventi = p.getEventi().stream()
                .sorted((a, b) -> a.getMinuto().compareTo(b.getMinuto()))
                .map(e -> toEventoDto(e, gMap.get(e.getGiocatoreId())))
                .toList();

        StatistichePartitaDto stats = p.getStatistiche() != null
                ? toStatisticheDto(p.getStatistiche()) : null;

        return toDto(p, eventi, stats);
    }

    @Transactional
    public PartitaDto create(PartitaRequest req) {
        Partita p = new Partita();
        applyPartitaRequest(p, req);
        return toDto(partitaRepository.save(p), List.of(), null);
    }

    @Transactional
    public PartitaDto update(Long id, PartitaRequest req) {
        Partita p = getPartitaOrThrow(id);
        applyPartitaRequest(p, req);
        partitaRepository.save(p);
        return findById(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!partitaRepository.existsById(id))
            throw new ResourceNotFoundException("Partita non trovata: " + id);
        partitaRepository.deleteById(id);
    }

    // ===== EVENTI =====

    @Transactional
    public PartitaDto addEvento(Long partitaId, EventoRequest req) {
        Partita p = getPartitaOrThrow(partitaId);
        EventoPartita e = new EventoPartita();
        e.setPartita(p);
        e.setMinuto(req.minuto());
        e.setTipo(req.tipo());
        e.setGiocatoreId(req.giocatoreId());
        e.setNota(req.nota());
        p.getEventi().add(e);
        partitaRepository.save(p);
        return findById(partitaId);
    }

    @Transactional
    public PartitaDto removeEvento(Long partitaId, Long eventoId) {
        Partita p = getPartitaOrThrow(partitaId);
        p.getEventi().removeIf(e -> e.getId().equals(eventoId));
        partitaRepository.save(p);
        return findById(partitaId);
    }

    // ===== STATISTICHE =====

    @Transactional
    public PartitaDto upsertStatistiche(Long partitaId, StatisticheRequest req) {
        Partita p = getPartitaOrThrow(partitaId);

        StatistichePartita stats = p.getStatistiche();
        if (stats == null) {
            stats = new StatistichePartita();
            stats.setPartita(p);
            p.setStatistiche(stats);
        }

        stats.setPossessoPct(req.possessoPct());
        stats.setTiriTotali(req.tiriTotali());
        stats.setTiriInPorta(req.tiriInPorta());
        stats.setPassaggi(req.passaggi());
        stats.setPassaggiRiusciti(req.passaggiRiusciti());
        stats.setDuelliVinti(req.duelliVinti());
        stats.setDuelliTotali(req.duelliTotali());
        stats.setCorner(req.corner());
        stats.setFalli(req.falli());
        stats.setFuorigioco(req.fuorigioco());
        stats.setXg(req.xg());

        partitaRepository.save(p);
        return findById(partitaId);
    }

    // ===== HEATMAP =====

    public List<HeatmapZonaDto> getHeatmap(Long partitaId, Long giocatoreId) {
        return heatmapRepository
                .findAllByPartitaIdAndGiocatoreId(partitaId, giocatoreId)
                .stream().map(this::toHeatmapDto).toList();
    }

    @Transactional
    public void saveHeatmap(Long partitaId, Long giocatoreId, List<HeatmapRequest> zone) {
        heatmapRepository.deleteAllByPartitaIdAndGiocatoreId(partitaId, giocatoreId);
        zone.forEach(req -> {
            HeatmapZona z = new HeatmapZona();
            z.setPartitaId(partitaId);
            z.setGiocatoreId(giocatoreId);
            z.setCoordX(req.coordX());
            z.setCoordY(req.coordY());
            z.setIntensita(req.intensita() != null ? req.intensita() : 1);
            heatmapRepository.save(z);
        });
    }

    // ===== SCOUTING =====

    public List<ScoutingDto> findAllScouting() {
        return scoutingRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toScoutingDto).toList();
    }

    public ScoutingDto findScoutingById(Long id) {
        return toScoutingDto(scoutingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scouting non trovato: " + id)));
    }

    @Transactional
    public ScoutingDto createScouting(ScoutingRequest req) {
        ScoutingAvversario s = new ScoutingAvversario();
        applyScoutingRequest(s, req);
        return toScoutingDto(scoutingRepository.save(s));
    }

    @Transactional
    public ScoutingDto updateScouting(Long id, ScoutingRequest req) {
        ScoutingAvversario s = scoutingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scouting non trovato: " + id));
        applyScoutingRequest(s, req);
        return toScoutingDto(scoutingRepository.save(s));
    }

    @Transactional
    public void deleteScouting(Long id) {
        if (!scoutingRepository.existsById(id))
            throw new ResourceNotFoundException("Scouting non trovato: " + id);
        scoutingRepository.deleteById(id);
    }

    // ===== MAPPING =====

    private void applyPartitaRequest(Partita p, PartitaRequest req) {
        p.setData(req.data());
        p.setAvversario(req.avversario());
        p.setCompetizione(req.competizione());
        p.setCasaTrasferta(req.casaTrasferta() != null ? req.casaTrasferta() : CasaTrasferta.CASA);
        p.setGolFatti(req.golFatti() != null ? req.golFatti() : 0);
        p.setGolSubiti(req.golSubiti() != null ? req.golSubiti() : 0);
        p.setModulo(req.modulo());
        p.setNote(req.note());
    }

    private void applyScoutingRequest(ScoutingAvversario s, ScoutingRequest req) {
        s.setNome(req.nome());
        s.setModulo(req.modulo());
        s.setPuntiForza(req.puntiForza());
        s.setDebolezze(req.debolezze());
        s.setGiocatoriChiave(req.giocatoriChiave());
        s.setNote(req.note());
    }

    private String calcolaRisultato(Partita p) {
        if (p.getGolFatti() > p.getGolSubiti()) return "V";
        if (p.getGolFatti() < p.getGolSubiti()) return "P";
        return "N";
    }

    private PartitaDto toDto(Partita p, List<EventoDto> eventi, StatistichePartitaDto stats) {
        return PartitaDto.builder()
                .id(p.getId())
                .data(p.getData())
                .avversario(p.getAvversario())
                .competizione(p.getCompetizione())
                .casaTrasferta(p.getCasaTrasferta())
                .golFatti(p.getGolFatti())
                .golSubiti(p.getGolSubiti())
                .risultato(calcolaRisultato(p))
                .modulo(p.getModulo())
                .note(p.getNote())
                .eventi(eventi)
                .statistiche(stats)
                .build();
    }

    private EventoDto toEventoDto(EventoPartita e, Giocatore g) {
        return EventoDto.builder()
                .id(e.getId())
                .minuto(e.getMinuto())
                .tipo(e.getTipo())
                .giocatoreId(e.getGiocatoreId())
                .nomeGiocatore(g != null ? g.getNome() : null)
                .cognomeGiocatore(g != null ? g.getCognome() : null)
                .nota(e.getNota())
                .build();
    }

    private StatistichePartitaDto toStatisticheDto(StatistichePartita s) {
        return StatistichePartitaDto.builder()
                .possessoPct(s.getPossessoPct())
                .tiriTotali(s.getTiriTotali())
                .tiriInPorta(s.getTiriInPorta())
                .passaggi(s.getPassaggi())
                .passaggiRiusciti(s.getPassaggiRiusciti())
                .duelliVinti(s.getDuelliVinti())
                .duelliTotali(s.getDuelliTotali())
                .corner(s.getCorner())
                .falli(s.getFalli())
                .fuorigioco(s.getFuorigioco())
                .xg(s.getXg())
                .build();
    }

    private HeatmapZonaDto toHeatmapDto(HeatmapZona z) {
        return HeatmapZonaDto.builder()
                .id(z.getId())
                .giocatoreId(z.getGiocatoreId())
                .coordX(z.getCoordX())
                .coordY(z.getCoordY())
                .intensita(z.getIntensita())
                .build();
    }

    private ScoutingDto toScoutingDto(ScoutingAvversario s) {
        return ScoutingDto.builder()
                .id(s.getId())
                .nome(s.getNome())
                .modulo(s.getModulo())
                .puntiForza(s.getPuntiForza())
                .debolezze(s.getDebolezze())
                .giocatoriChiave(s.getGiocatoriChiave())
                .note(s.getNote())
                .build();
    }

    private Partita getPartitaOrThrow(Long id) {
        return partitaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partita non trovata: " + id));
    }
}

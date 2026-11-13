package com.pitchiq.bff.PitchIQ.partite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.partite.dto.*;
import com.pitchiq.bff.PitchIQ.partite.model.*;
import com.pitchiq.bff.PitchIQ.partite.repository.HeatmapRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.ScoutingRepository;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tracking.service.StatsBombService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartitaService {

    private final PartitaRepository partitaRepository;
    private final HeatmapRepository heatmapRepository;
    private final ScoutingRepository scoutingRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final XgCalcolatoreService xgCalcolatoreService;
    private final StatsBombService statsBombService;

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
                .sorted(Comparator.comparingInt((EventoPartita e) -> e.getPeriodo() != null
                                ? e.getPeriodo() : (e.getMinuto() <= 45 ? 1 : 2))
                        .thenComparing(EventoPartita::getMinuto)
                        .thenComparingInt(e -> e.getMinutoAggiuntivo() != null ? e.getMinutoAggiuntivo() : 0))
                .map(e -> toEventoDto(e, gMap.get(e.getGiocatoreId())))
                .toList();

        StatistichePartitaDto stats = p.getStatistiche() != null
                ? toStatisticheDto(p.getStatistiche()) : null;

        return toDto(p, eventi, stats);
    }

    public List<TitolareDto> findTitolari(Long partitaId) {
        if (!partitaRepository.existsById(partitaId)) {
            throw new ResourceNotFoundException("Partita non trovata: " + partitaId);
        }
        return giocatoreRepository.findTitolariByPartitaId(partitaId).stream()
                .map(g -> new TitolareDto(g.getId(), g.getNome(), g.getCognome(),
                        g.getPosizione(), g.getNumeroMaglia(), g.getModuloFonte()))
                .toList();
    }

    @Transactional
    public PartitaDto create(PartitaRequest req) {
        Partita p = new Partita();
        applyPartitaRequest(p, req);
        return toDto(partitaRepository.save(p), List.of(), null);
    }

    @Transactional
    public PartitaDto importaDaStatsBomb(StatsBombImportRequest req) {
        if (partitaRepository.existsByStatsBombMatchId(req.matchId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partita StatsBomb già importata");
        }

        JsonNode match = null;
        for (JsonNode candidate : statsBombService.partite(req.competitionId(), req.seasonId())) {
            if (candidate.path("match_id").asInt(-1) == req.matchId()) {
                match = candidate;
                break;
            }
        }
        if (match == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partita non trovata nel catalogo StatsBomb");
        }

        JsonNode home = match.path("home_team");
        JsonNode away = match.path("away_team");
        JsonNode analyzedTeam = req.lato() == StatsBombImportRequest.Lato.CASA ? home : away;
        String homeName = teamName(home);
        String awayName = teamName(away);
        String analyzedName = teamName(analyzedTeam);
        String opponent = req.lato() == StatsBombImportRequest.Lato.CASA ? awayName : homeName;

        LocalDate date;
        try {
            date = LocalDate.parse(match.path("match_date").asText());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Data partita StatsBomb non valida");
        }

        Partita partita = new Partita();
        partita.setStatsBombMatchId(req.matchId());
        partita.setData(date);
        partita.setAvversario(opponent);
        partita.setCompetizione(match.path("competition").path("competition_name").asText(null));
        partita.setCasaTrasferta(req.lato() == StatsBombImportRequest.Lato.CASA
                ? CasaTrasferta.CASA : CasaTrasferta.TRASFERTA);
        partita.setGolFatti(req.lato() == StatsBombImportRequest.Lato.CASA
                ? match.path("home_score").asInt(0) : match.path("away_score").asInt(0));
        partita.setGolSubiti(req.lato() == StatsBombImportRequest.Lato.CASA
                ? match.path("away_score").asInt(0) : match.path("home_score").asInt(0));
        partita.setNote("Importata da StatsBomb Open Data. Possesso palla non disponibile.");

        JsonNode events = statsBombService.eventi(req.matchId());
        int shots = 0;
        int shotsOnTarget = 0;
        int passes = 0;
        int completedPasses = 0;
        int duels = 0;
        int wonDuels = 0;
        int corners = 0;
        int fouls = 0;
        int offsides = 0;
        BigDecimal xg = BigDecimal.ZERO;

        for (JsonNode event : events) {
            String type = event.path("type").path("name").asText();
            String eventTeam = teamName(event.path("team"));
            boolean ours = analyzedName.equals(eventTeam);
            JsonNode pass = event.path("pass");
            JsonNode shot = event.path("shot");
            int minute = Math.max(0, Math.min(255, event.path("minute").asInt(0)));
            String player = event.path("player").path("name").asText("");
            String outcome = shot.path("outcome").path("name").asText();

            if (ours && "Shot".equals(type)) {
                shots++;
                if (Set.of("Goal", "Saved", "Saved to Post").contains(outcome)) shotsOnTarget++;
                xg = xg.add(BigDecimal.valueOf(shot.path("statsbomb_xg").asDouble(0)));
            }
            if (ours && "Pass".equals(type)) {
                passes++;
                if (!pass.hasNonNull("outcome")) completedPasses++;
                if ("Corner".equals(pass.path("type").path("name").asText())) corners++;
            }
            if (ours && "Duel".equals(type)) {
                duels++;
                String duelOutcome = event.path("duel").path("outcome").path("name").asText();
                if (duelOutcome.contains("Won") || duelOutcome.contains("Success")) wonDuels++;
            }
            if (ours && "Foul Committed".equals(type)) fouls++;
            if (ours && "Offside".equals(type)) offsides++;

            TipoEvento timelineType = timelineType(type, outcome, event, ours);
            if (timelineType != null) {
                String note = player.isBlank() ? eventTeam : player + " - " + eventTeam;
                if ("Shot".equals(type) && !outcome.isBlank()) note += " · " + outcome;
                aggiungiEventoImportato(partita, timelineType, minute, note);
                if ("Substitution".equals(type)) {
                    String replacement = event.path("substitution").path("replacement").path("name").asText("");
                    if (!replacement.isBlank()) {
                        aggiungiEventoImportato(partita, TipoEvento.SOSTITUZIONE_IN, minute,
                                replacement + " - " + eventTeam);
                    }
                }
            }
        }

        StatistichePartita stats = new StatistichePartita();
        stats.setPartita(partita);
        stats.setPossessoPct(null);
        stats.setTiriTotali(shots);
        stats.setTiriInPorta(shotsOnTarget);
        stats.setPassaggi(passes);
        stats.setPassaggiRiusciti(completedPasses);
        stats.setDuelliVinti(wonDuels);
        stats.setDuelliTotali(duels);
        stats.setCorner(corners);
        stats.setFalli(fouls);
        stats.setFuorigioco(offsides);
        stats.setXg(xg.setScale(2, java.math.RoundingMode.HALF_UP));
        partita.setStatistiche(stats);

        Partita saved = partitaRepository.save(partita);
        return findById(saved.getId());
    }

    private String teamName(JsonNode team) {
        return team.path("home_team_name").asText(team.path("away_team_name").asText(team.path("name").asText("")));
    }

    private void aggiungiEventoImportato(Partita partita, TipoEvento tipo, int minuto, String nota) {
        EventoPartita evento = new EventoPartita();
        evento.setPartita(partita);
        evento.setMinuto(minuto);
        evento.setTipo(tipo);
        evento.setNota(nota.length() > 200 ? nota.substring(0, 200) : nota);
        partita.getEventi().add(evento);
    }

    private TipoEvento timelineType(String type, String shotOutcome, JsonNode event, boolean ours) {
        if ("Shot".equals(type)) {
            if ("Goal".equals(shotOutcome)) return TipoEvento.GOL;
            if (!ours && Set.of("Saved", "Saved to Post").contains(shotOutcome)) return TipoEvento.PARATA;
            return ours ? TipoEvento.TIRO : null;
        }
        if ("Pass".equals(type) && ours && event.path("pass").path("goal_assist").asBoolean()) {
            return TipoEvento.ASSIST;
        }
        if ("Ball Recovery".equals(type) && ours) return TipoEvento.RECUPERO;
        if ("Foul Committed".equals(type)) {
            String card = event.path("foul_committed").path("card").path("name").asText();
            if (card.contains("Red")) return TipoEvento.ESPULSIONE;
            if (card.contains("Yellow")) return TipoEvento.AMMONIZIONE;
            if (ours) return TipoEvento.FALLO;
        }
        if ("Substitution".equals(type)) return TipoEvento.SOSTITUZIONE_OUT;
        return null;
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
        e.setCoordX(req.coordX());
        e.setCoordY(req.coordY());

        // Calcola xG automaticamente per tiri e gol con posizione
        if ((req.tipo() == TipoEvento.TIRO || req.tipo() == TipoEvento.GOL)
                && req.coordX() != null && req.coordY() != null) {
            double xg = xgCalcolatoreService.calcolaXgTiro(
                    req.coordX().doubleValue(),
                    req.coordY().doubleValue()
            );
            e.setXg(BigDecimal.valueOf(xg));
        }

        p.getEventi().add(e);
        partitaRepository.save(p);

        // Ricalcola xG totale della partita sommando tutti i tiri
        aggiornaXgTotale(p);

        return findById(partitaId);
    }

    private void aggiornaXgTotale(Partita p) {
        List<Double> xgTiri = p.getEventi().stream()
                .filter(e -> e.getXg() != null)
                .map(e -> e.getXg().doubleValue())
                .toList();

        if (!xgTiri.isEmpty()) {
            StatistichePartita stats = p.getStatistiche();
            if (stats == null) {
                stats = new StatistichePartita();
                stats.setPartita(p);
                p.setStatistiche(stats);
            }
            stats.setXg(xgCalcolatoreService.sommaXg(xgTiri));
            partitaRepository.save(p);
        }
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
        stats.setFonte(null);

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
                .minutoAggiuntivo(e.getMinutoAggiuntivo())
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
                .fonte(s.getFonte())
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

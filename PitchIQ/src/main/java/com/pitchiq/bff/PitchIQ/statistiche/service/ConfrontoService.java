package com.pitchiq.bff.PitchIQ.statistiche.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.statistiche.dto.ConfrontoDto;
import com.pitchiq.bff.PitchIQ.statistiche.dto.GiocatoreRadarDto;
import com.pitchiq.bff.PitchIQ.statistiche.dto.PuntoRadarDto;
import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import com.pitchiq.bff.PitchIQ.statistiche.model.StatisticaPeriodica;
import com.pitchiq.bff.PitchIQ.statistiche.repository.StatisticaPeriodicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfrontoService {

    private final StatisticaPeriodicaRepository repository;
    private final GiocatoreRepository giocatoreRepository;

    // Range max per categoria — usato per normalizzare 0-100
    private static final Map<CategoriaMetrica, Double> MAX_VALORI = Map.of(
            CategoriaMetrica.VALUTAZIONE_MEDIA, 10.0,
            CategoriaMetrica.PRESENZE_PCT,      100.0,
            CategoriaMetrica.VELOCITA,          40.0,
            CategoriaMetrica.RESISTENZA,        15.0,
            CategoriaMetrica.FORZA,             100.0,
            CategoriaMetrica.MINUTI_GIOCATI,    400.0,
            CategoriaMetrica.GOL,               10.0,
            CategoriaMetrica.ASSIST,            10.0,
            CategoriaMetrica.AMMONIZIONI,       5.0
    );

    private static final Map<CategoriaMetrica, String> LABEL_MAP = Map.of(
            CategoriaMetrica.VALUTAZIONE_MEDIA, "Valutazione",
            CategoriaMetrica.PRESENZE_PCT,      "Presenze %",
            CategoriaMetrica.VELOCITA,          "Velocità",
            CategoriaMetrica.RESISTENZA,        "Resistenza",
            CategoriaMetrica.FORZA,             "Forza",
            CategoriaMetrica.MINUTI_GIOCATI,    "Minuti",
            CategoriaMetrica.GOL,               "Gol",
            CategoriaMetrica.ASSIST,            "Assist",
            CategoriaMetrica.AMMONIZIONI,       "Ammonizioni"
    );

    public ConfrontoDto confronta(Long id1, Long id2) {
        Giocatore g1 = giocatoreRepository.findById(id1)
                .orElseThrow(() -> new ResourceNotFoundException("Giocatore non trovato: " + id1));
        Giocatore g2 = giocatoreRepository.findById(id2)
                .orElseThrow(() -> new ResourceNotFoundException("Giocatore non trovato: " + id2));

        return ConfrontoDto.builder()
                .giocatore1(buildRadar(g1))
                .giocatore2(buildRadar(g2))
                .build();
    }

    private GiocatoreRadarDto buildRadar(Giocatore g) {
        List<StatisticaPeriodica> stats =
                repository.findAllByGiocatoreIdOrderByPeriodoAsc(g.getId());

        // Raggruppa per categoria e calcola la media
        Map<CategoriaMetrica, Double> medie = stats.stream()
                .collect(Collectors.groupingBy(
                        StatisticaPeriodica::getCategoria,
                        Collectors.averagingDouble(StatisticaPeriodica::getValore)
                ));

        List<PuntoRadarDto> valori = Arrays.stream(CategoriaMetrica.values())
                .map(cat -> {
                    double media = medie.getOrDefault(cat, 0.0);
                    double max   = MAX_VALORI.getOrDefault(cat, 100.0);
                    double pct   = Math.min(100.0, (media / max) * 100.0);

                    // Per ammonizioni invertiamo: meno è meglio
                    if (cat == CategoriaMetrica.AMMONIZIONI) {
                        pct = Math.max(0, 100.0 - pct);
                    }

                    return PuntoRadarDto.builder()
                            .categoria(cat)
                            .label(LABEL_MAP.getOrDefault(cat, cat.name()))
                            .valore(Math.round(media * 100.0) / 100.0)
                            .valorePct(Math.round(pct * 10.0) / 10.0)
                            .build();
                })
                .toList();

        return GiocatoreRadarDto.builder()
                .giocatoreId(g.getId())
                .nomeGiocatore(g.getNome() + " " + g.getCognome())
                .valori(valori)
                .build();
    }
}
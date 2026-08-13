package com.pitchiq.bff.PitchIQ.statistiche.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.statistiche.dto.PuntoTrendDto;
import com.pitchiq.bff.PitchIQ.statistiche.dto.StatisticaDto;
import com.pitchiq.bff.PitchIQ.statistiche.dto.StatisticaRequest;
import com.pitchiq.bff.PitchIQ.statistiche.dto.TrendDto;
import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import com.pitchiq.bff.PitchIQ.statistiche.model.StatisticaPeriodica;
import com.pitchiq.bff.PitchIQ.statistiche.repository.StatisticaPeriodicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticaService {

    private final StatisticaPeriodicaRepository repository;
    private final GiocatoreRepository giocatoreRepository;
    private final ProiezioneService proiezioneService;

    // Inserisce o aggiorna una statistica per un giocatore/periodo/categoria
    @Transactional
    public StatisticaDto upsert(StatisticaRequest req) {
        StatisticaPeriodica stat = repository
                .findByGiocatoreIdAndPeriodoAndCategoria(
                        req.giocatoreId(), req.periodo(), req.categoria())
                .orElse(new StatisticaPeriodica());

        stat.setGiocatoreId(req.giocatoreId());
        stat.setPeriodo(req.periodo());
        stat.setCategoria(req.categoria());
        stat.setValore(req.valore());

        return toDto(repository.save(stat));
    }

    // Trend completo per un giocatore e una categoria
    public TrendDto getTrend(Long giocatoreId, CategoriaMetrica categoria, int mesiProiezione) {
        Giocatore g = giocatoreRepository.findById(giocatoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Giocatore non trovato: " + giocatoreId));

        List<PuntoTrendDto> storico = repository
                .findAllByGiocatoreIdAndCategoriaOrderByPeriodoAsc(giocatoreId, categoria)
                .stream()
                .map(s -> PuntoTrendDto.builder()
                        .periodo(s.getPeriodo())
                        .valore(s.getValore())
                        .build())
                .toList();

        List<PuntoTrendDto> proiezione = proiezioneService.calcola(storico, mesiProiezione);
        double indiceCrescita = proiezioneService.calcolaIndiceCrescita(storico);
        String tendenza = proiezioneService.calcolaTendenza(indiceCrescita);

        return TrendDto.builder()
                .giocatoreId(giocatoreId)
                .nomeGiocatore(g.getNome() + " " + g.getCognome())
                .categoria(categoria)
                .storico(storico)
                .proiezione(proiezione)
                .indiceCrescita(indiceCrescita)
                .tendenza(tendenza)
                .build();
    }

    // Tutti i trend di un giocatore (tutte le categorie)
    public List<TrendDto> getAllTrend(Long giocatoreId, int mesiProiezione) {
        return Arrays.stream(CategoriaMetrica.values())
                .map(cat -> getTrend(giocatoreId, cat, mesiProiezione))
                .filter(t -> !t.storico().isEmpty())
                .toList();
    }

    // Panoramica rosa: indice crescita per ogni giocatore (categoria VALUTAZIONE_MEDIA)
    public List<TrendDto> getPanoramicaRosa() {
        return repository.findDistinctGiocatoreIds().stream()
                .map(id -> getTrend(id, CategoriaMetrica.VALUTAZIONE_MEDIA, 3))
                .toList();
    }

    private StatisticaDto toDto(StatisticaPeriodica s) {
        return StatisticaDto.builder()
                .id(s.getId())
                .giocatoreId(s.getGiocatoreId())
                .periodo(s.getPeriodo())
                .categoria(s.getCategoria())
                .valore(s.getValore())
                .build();
    }
}

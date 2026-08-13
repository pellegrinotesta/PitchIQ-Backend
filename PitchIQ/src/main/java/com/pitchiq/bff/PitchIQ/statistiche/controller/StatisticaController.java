package com.pitchiq.bff.PitchIQ.statistiche.controller;

import com.pitchiq.bff.PitchIQ.statistiche.dto.StatisticaDto;
import com.pitchiq.bff.PitchIQ.statistiche.dto.StatisticaRequest;
import com.pitchiq.bff.PitchIQ.statistiche.dto.TrendDto;
import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import com.pitchiq.bff.PitchIQ.statistiche.service.StatisticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistiche")
@RequiredArgsConstructor
public class StatisticaController {

    private final StatisticaService service;

    // Inserisci/aggiorna un valore
    @PostMapping
    public StatisticaDto upsert(@Valid @RequestBody StatisticaRequest req) {
        return service.upsert(req);
    }

    // Trend singola categoria per un giocatore
    // GET /api/statistiche/giocatori/1/trend?categoria=VALUTAZIONE_MEDIA&mesi=6
    @GetMapping("/giocatori/{id}/trend")
    public TrendDto getTrend(
            @PathVariable Long id,
            @RequestParam CategoriaMetrica categoria,
            @RequestParam(defaultValue = "6") int mesi) {
        return service.getTrend(id, categoria, mesi);
    }

    // Tutti i trend di un giocatore
    @GetMapping("/giocatori/{id}/trend/tutti")
    public List<TrendDto> getAllTrend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int mesi) {
        return service.getAllTrend(id, mesi);
    }

    // Panoramica crescita tutta la rosa
    @GetMapping("/rosa/panoramica")
    public List<TrendDto> getPanoramicaRosa() {
        return service.getPanoramicaRosa();
    }
}
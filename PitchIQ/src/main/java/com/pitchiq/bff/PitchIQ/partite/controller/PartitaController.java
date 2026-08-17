package com.pitchiq.bff.PitchIQ.partite.controller;

import com.pitchiq.bff.PitchIQ.partite.dto.*;
import com.pitchiq.bff.PitchIQ.partite.service.PartitaService;
import com.pitchiq.bff.PitchIQ.partite.service.XgCalcolatoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PartitaController {

    private final PartitaService service;
    private final XgCalcolatoreService xgCalcolatoreService;

    // ===== PARTITE =====
    @GetMapping("/api/partite")
    public List<PartitaDto> getAll() { return service.findAll(); }

    @GetMapping("/api/partite/{id}")
    public PartitaDto getById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping("/api/partite")
    public ResponseEntity<PartitaDto> create(@Valid @RequestBody PartitaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/api/partite/{id}")
    public PartitaDto update(@PathVariable Long id, @Valid @RequestBody PartitaRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/api/partite/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== EVENTI =====
    @PostMapping("/api/partite/{id}/eventi")
    public PartitaDto addEvento(@PathVariable Long id, @Valid @RequestBody EventoRequest req) {
        return service.addEvento(id, req);
    }

    @DeleteMapping("/api/partite/{id}/eventi/{eventoId}")
    public PartitaDto removeEvento(@PathVariable Long id, @PathVariable Long eventoId) {
        return service.removeEvento(id, eventoId);
    }

    // GET /api/partite/xg-preview?x=55&y=25
    @GetMapping("/api/partite/xg-preview")
    public Map<String, Double> xgPreview(
            @RequestParam double x,
            @RequestParam double y) {
        return Map.of("xg", xgCalcolatoreService.calcolaXgTiro(x, y));
    }

    // ===== STATISTICHE =====
    @PostMapping("/api/partite/{id}/statistiche")
    public PartitaDto upsertStatistiche(@PathVariable Long id, @RequestBody StatisticheRequest req) {
        return service.upsertStatistiche(id, req);
    }

    // ===== HEATMAP =====
    @GetMapping("/api/partite/{id}/heatmap/{giocatoreId}")
    public List<HeatmapZonaDto> getHeatmap(@PathVariable Long id, @PathVariable Long giocatoreId) {
        return service.getHeatmap(id, giocatoreId);
    }

    @PostMapping("/api/partite/{id}/heatmap/{giocatoreId}")
    public ResponseEntity<Void> saveHeatmap(
            @PathVariable Long id,
            @PathVariable Long giocatoreId,
            @RequestBody List<HeatmapRequest> zone) {
        service.saveHeatmap(id, giocatoreId, zone);
        return ResponseEntity.ok().build();
    }

    // ===== SCOUTING =====
    @GetMapping("/api/scouting")
    public List<ScoutingDto> getAllScouting() { return service.findAllScouting(); }

    @GetMapping("/api/scouting/{id}")
    public ScoutingDto getScoutingById(@PathVariable Long id) { return service.findScoutingById(id); }

    @PostMapping("/api/scouting")
    public ResponseEntity<ScoutingDto> createScouting(@Valid @RequestBody ScoutingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createScouting(req));
    }

    @PutMapping("/api/scouting/{id}")
    public ScoutingDto updateScouting(@PathVariable Long id, @Valid @RequestBody ScoutingRequest req) {
        return service.updateScouting(id, req);
    }

    @DeleteMapping("/api/scouting/{id}")
    public ResponseEntity<Void> deleteScouting(@PathVariable Long id) {
        service.deleteScouting(id);
        return ResponseEntity.noContent().build();
    }
}

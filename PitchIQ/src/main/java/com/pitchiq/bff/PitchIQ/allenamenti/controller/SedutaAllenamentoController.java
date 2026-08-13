package com.pitchiq.bff.PitchIQ.allenamenti.controller;

import com.pitchiq.bff.PitchIQ.allenamenti.dto.SedutaDto;
import com.pitchiq.bff.PitchIQ.allenamenti.dto.SedutaRequest;
import com.pitchiq.bff.PitchIQ.allenamenti.service.SedutaAllenamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/allenamenti")
@RequiredArgsConstructor
public class SedutaAllenamentoController {

    private final SedutaAllenamentoService service;

    @GetMapping
    public List<SedutaDto> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) return service.findByPeriodo(from, to);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SedutaDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SedutaDto> create(@Valid @RequestBody SedutaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public SedutaDto update(@PathVariable Long id, @Valid @RequestBody SedutaRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Genera automaticamente le presenze per tutta la rosa attiva
    @PostMapping("/{id}/genera-presenze")
    public SedutaDto generaPresenze(@PathVariable Long id) {
        return service.generaPresenze(id);
    }
}

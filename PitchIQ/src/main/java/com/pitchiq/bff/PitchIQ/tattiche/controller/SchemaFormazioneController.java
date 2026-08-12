package com.pitchiq.bff.PitchIQ.tattiche.controller;

import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneDto;
import com.pitchiq.bff.PitchIQ.tattiche.dto.SchemaFormazioneDto;
import com.pitchiq.bff.PitchIQ.tattiche.dto.SchemaFormazioneRequest;
import com.pitchiq.bff.PitchIQ.tattiche.service.SchemaFormazioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tattiche/schemi")
@RequiredArgsConstructor
public class SchemaFormazioneController {

    private final SchemaFormazioneService service;

    @GetMapping
    public List<SchemaFormazioneDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SchemaFormazioneDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SchemaFormazioneDto> create(@Valid @RequestBody SchemaFormazioneRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public SchemaFormazioneDto update(
            @PathVariable Long id,
            @Valid @RequestBody SchemaFormazioneRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/tattiche/schemi/{schemaId}/applica/{formazioneId}
    @PostMapping("/{schemaId}/applica/{formazioneId}")
    public FormazioneDto applicaAFormazione(
            @PathVariable Long schemaId,
            @PathVariable Long formazioneId) {
        return service.applicaAFormazione(schemaId, formazioneId);
    }
}
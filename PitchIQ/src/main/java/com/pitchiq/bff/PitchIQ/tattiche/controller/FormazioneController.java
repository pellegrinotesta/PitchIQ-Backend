package com.pitchiq.bff.PitchIQ.tattiche.controller;

import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneDto;
import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneRequest;
import com.pitchiq.bff.PitchIQ.tattiche.service.FormazioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tattiche/formazioni")
@RequiredArgsConstructor
public class FormazioneController {

    private final FormazioneService service;

    @GetMapping
    public List<FormazioneDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public FormazioneDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<FormazioneDto> create(@Valid @RequestBody FormazioneRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public FormazioneDto update(@PathVariable Long id, @Valid @RequestBody FormazioneRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

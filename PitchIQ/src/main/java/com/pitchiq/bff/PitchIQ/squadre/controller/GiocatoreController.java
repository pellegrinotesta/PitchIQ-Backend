package com.pitchiq.bff.PitchIQ.squadre.controller;

import com.pitchiq.bff.PitchIQ.squadre.dto.GiocatoreCreateRequest;
import com.pitchiq.bff.PitchIQ.squadre.dto.GiocatoreDto;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.service.GiocatoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/squadre/giocatori")
@RequiredArgsConstructor
public class GiocatoreController {

    private final GiocatoreService service;

    // GET /api/squadre/giocatori
    // GET /api/squadre/giocatori?ruolo=PORTIERE
    // GET /api/squadre/giocatori?stato=ATTIVO
    // GET /api/squadre/giocatori?cognome=ros
    @GetMapping
    public List<GiocatoreDto> getAll(
            @RequestParam(required = false) RuoloGiocatore ruolo,
            @RequestParam(required = false) StatoGiocatore stato,
            @RequestParam(required = false) String cognome) {

        if (ruolo != null) return service.findByRuolo(ruolo);
        if (stato != null) return service.findByStato(stato);
        if (cognome != null && !cognome.isBlank()) return service.searchByCognome(cognome);
        return service.findAll();
    }

    // GET /api/squadre/giocatori/{id}
    @GetMapping("/{id}")
    public GiocatoreDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // POST /api/squadre/giocatori
    @PostMapping
    public ResponseEntity<GiocatoreDto> create(@Valid @RequestBody GiocatoreCreateRequest req) {
        GiocatoreDto created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/squadre/giocatori/{id}
    @PutMapping("/{id}")
    public GiocatoreDto update(
            @PathVariable Long id,
            @Valid @RequestBody GiocatoreCreateRequest req) {
        return service.update(id, req);
    }

    // DELETE /api/squadre/giocatori/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

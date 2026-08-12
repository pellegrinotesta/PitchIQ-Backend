package com.pitchiq.bff.PitchIQ.squadre.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.dto.GiocatoreCreateRequest;
import com.pitchiq.bff.PitchIQ.squadre.dto.GiocatoreDto;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiocatoreService {

    private final GiocatoreRepository repository;

    // --- Query ---

    public List<GiocatoreDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<GiocatoreDto> findByRuolo(RuoloGiocatore ruolo) {
        return repository.findAllByRuolo(ruolo).stream()
                .map(this::toDto)
                .toList();
    }

    public List<GiocatoreDto> findByStato(StatoGiocatore stato) {
        return repository.findAllByStato(stato).stream()
                .map(this::toDto)
                .toList();
    }

    public List<GiocatoreDto> searchByCognome(String cognome) {
        return repository.findAllByCognomeContainingIgnoreCase(cognome).stream()
                .map(this::toDto)
                .toList();
    }

    public GiocatoreDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    // --- Comandi ---

    @Transactional
    public GiocatoreDto create(GiocatoreCreateRequest req) {
        Giocatore g = fromRequest(new Giocatore(), req);
        return toDto(repository.save(g));
    }

    @Transactional
    public GiocatoreDto update(Long id, GiocatoreCreateRequest req) {
        Giocatore g = getOrThrow(id);
        fromRequest(g, req);
        return toDto(repository.save(g));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Giocatore non trovato: " + id);
        }
        repository.deleteById(id);
    }

    // --- Mapping ---

    private GiocatoreDto toDto(Giocatore g) {
        return GiocatoreDto.builder()
                .id(g.getId())
                .nome(g.getNome())
                .cognome(g.getCognome())
                .dataNascita(g.getDataNascita())
                .ruolo(g.getRuolo())
                .numeroMaglia(g.getNumeroMaglia())
                .piedePreferito(g.getPiedePreferito())
                .contrattoInizio(g.getContrattoInizio())
                .contrattoFine(g.getContrattoFine())
                .stato(g.getStato())
                .build();
    }

    private Giocatore fromRequest(Giocatore g, GiocatoreCreateRequest req) {
        g.setNome(req.nome());
        g.setCognome(req.cognome());
        g.setDataNascita(req.dataNascita());
        g.setRuolo(req.ruolo());
        g.setNumeroMaglia(req.numeroMaglia());
        g.setPiedePreferito(req.piedePreferito());
        g.setContrattoInizio(req.contrattoInizio());
        g.setContrattoFine(req.contrattoFine());
        g.setStato(req.stato() != null ? req.stato() : StatoGiocatore.ATTIVO);
        return g;
    }

    private Giocatore getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giocatore non trovato: " + id));
    }
}

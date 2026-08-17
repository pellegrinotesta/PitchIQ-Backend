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

import java.time.LocalDate;
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
        int eta = g.getDataNascita() != null
                ? (int) java.time.temporal.ChronoUnit.YEARS.between(g.getDataNascita(), LocalDate.now())
                : 0;

        return GiocatoreDto.builder()
                .id(g.getId())
                .nome(g.getNome())
                .cognome(g.getCognome())
                .dataNascita(g.getDataNascita())
                .eta(eta)
                .ruolo(g.getRuolo())
                .ruoliSecondari(g.getRuoliSecondari())
                .numeroMaglia(g.getNumeroMaglia())
                .piedePreferito(g.getPiedePreferito())
                .contrattoInizio(g.getContrattoInizio())
                .contrattoFine(g.getContrattoFine())
                .stato(g.getStato())
                .nazionalita(g.getNazionalita())
                .luogoNascita(g.getLuogoNascita())
                .altezzaCm(g.getAltezzaCm())
                .pesoKg(g.getPesoKg())
                .stipendioAnnuo(g.getStipendioAnnuo())
                .valoreMercato(g.getValoreMercato())
                .clausola(g.getClausola())
                .noteMediche(g.getNoteMediche())
                .agente(g.getAgente())
                .build();
    }

    private Giocatore fromRequest(Giocatore g, GiocatoreCreateRequest req) {
        g.setNome(req.nome());
        g.setCognome(req.cognome());
        g.setDataNascita(req.dataNascita());
        g.setRuolo(req.ruolo());
        g.setRuoliSecondari(req.ruoliSecondari());
        g.setNumeroMaglia(req.numeroMaglia());
        g.setPiedePreferito(req.piedePreferito());
        g.setContrattoInizio(req.contrattoInizio());
        g.setContrattoFine(req.contrattoFine());
        g.setStato(req.stato() != null ? req.stato() : StatoGiocatore.ATTIVO);
        g.setNazionalita(req.nazionalita());
        g.setLuogoNascita(req.luogoNascita());
        g.setAltezzaCm(req.altezzaCm());
        g.setPesoKg(req.pesoKg());
        g.setStipendioAnnuo(req.stipendioAnnuo());
        g.setValoreMercato(req.valoreMercato());
        g.setClausola(req.clausola());
        g.setNoteMediche(req.noteMediche());
        g.setAgente(req.agente());
        return g;
    }

    private Giocatore getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giocatore non trovato: " + id));
    }
}

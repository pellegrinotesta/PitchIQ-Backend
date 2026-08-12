package com.pitchiq.bff.PitchIQ.tattiche.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.dto.*;
import com.pitchiq.bff.PitchIQ.tattiche.model.*;
import com.pitchiq.bff.PitchIQ.tattiche.repository.FormazioneRepository;
import com.pitchiq.bff.PitchIQ.tattiche.repository.SchemaFormazioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchemaFormazioneService {

    private final SchemaFormazioneRepository schemaRepository;
    private final FormazioneRepository formazioneRepository;
    private final GiocatoreRepository giocatoreRepository;

    public List<SchemaFormazioneDto> findAll() {
        return schemaRepository.findAllOrdered().stream()
                .map(s -> toDto(s))
                .toList();
    }

    public SchemaFormazioneDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional
    public SchemaFormazioneDto create(SchemaFormazioneRequest req) {
        SchemaFormazione s = new SchemaFormazione();
        applyRequest(s, req);
        return toDto(schemaRepository.save(s));
    }

    @Transactional
    public SchemaFormazioneDto update(Long id, SchemaFormazioneRequest req) {
        SchemaFormazione s = getOrThrow(id);
        applyRequest(s, req);
        return toDto(schemaRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        if (!schemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schema non trovato: " + id);
        }
        schemaRepository.deleteById(id);
    }

    // Applica uno schema a una formazione esistente:
    // pre-posiziona i giocatori della rosa sugli slot in base al ruolo
    @Transactional
    public FormazioneDto applicaAFormazione(Long schemaId, Long formazioneId) {
        SchemaFormazione schema = getOrThrow(schemaId);
        Formazione formazione = formazioneRepository.findByIdWithPosizioni(formazioneId)
                .orElseThrow(() -> new ResourceNotFoundException("Formazione non trovata: " + formazioneId));

        // Carica la rosa attiva
        List<Giocatore> rosa = giocatoreRepository.findAllByStato(
                StatoGiocatore.ATTIVO);

        // Raggruppa giocatori per ruolo
        Map<String, Queue<Giocatore>> perRuolo = rosa.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getRuolo().name(),
                        Collectors.toCollection(LinkedList::new)
                ));

        // Svuota le posizioni esistenti
        formazione.getPosizioni().clear();

        // Assegna un giocatore ad ogni slot in base al ruolo
        schema.getSlot().forEach(slot -> {
            Queue<Giocatore> candidati = perRuolo.get(slot.getRuolo());
            if (candidati == null || candidati.isEmpty()) return;

            Giocatore g = candidati.poll();
            PosizioneCampo pos = new PosizioneCampo();
            pos.setFormazione(formazione);
            pos.setGiocatoreId(g.getId());
            pos.setCoordX(slot.getCoordX());
            pos.setCoordY(slot.getCoordY());
            pos.setSlotRuolo(slot.getSlotRuolo());
            pos.setTitolare(true);
            formazione.getPosizioni().add(pos);
        });

        formazione.setModulo(schema.getModulo());
        formazioneRepository.save(formazione);

        // Ricarica con dati completi
        return buildFormazioneService().findById(formazioneId);
    }

    // --- Privato ---

    private SchemaFormazione getOrThrow(Long id) {
        return schemaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema non trovato: " + id));
    }

    private void applyRequest(SchemaFormazione s, SchemaFormazioneRequest req) {
        s.setNome(req.nome());
        s.setModulo(req.modulo());
        s.setDescrizione(req.descrizione());

        s.getSlot().clear();
        if (req.slot() != null) {
            req.slot().forEach(sr -> {
                SlotSchema slot = new SlotSchema();
                slot.setSchema(s);
                slot.setSlotRuolo(sr.slotRuolo());
                slot.setRuolo(sr.ruolo());
                slot.setCoordX(sr.coordX());
                slot.setCoordY(sr.coordY());
                slot.setNote(sr.note());
                s.getSlot().add(slot);
            });
        }

        s.getFrecce().clear();
        if (req.frecce() != null) {
            req.frecce().forEach(fr -> {
                FrecciaSchema freccia = new FrecciaSchema();
                freccia.setSchema(s);
                freccia.setStartX(fr.startX());
                freccia.setStartY(fr.startY());
                freccia.setEndX(fr.endX());
                freccia.setEndY(fr.endY());
                freccia.setColore(fr.colore() != null ? fr.colore() : "#00ff87");
                freccia.setEtichetta(fr.etichetta());
                s.getFrecce().add(freccia);
            });
        }
    }

    private SchemaFormazioneDto toDto(SchemaFormazione s) {
        return SchemaFormazioneDto.builder()
                .id(s.getId())
                .nome(s.getNome())
                .modulo(s.getModulo())
                .descrizione(s.getDescrizione())
                .slot(s.getSlot().stream().map(sl -> SlotSchemaDto.builder()
                        .id(sl.getId())
                        .slotRuolo(sl.getSlotRuolo())
                        .ruolo(sl.getRuolo())
                        .coordX(sl.getCoordX())
                        .coordY(sl.getCoordY())
                        .note(sl.getNote())
                        .build()).toList())
                .frecce(s.getFrecce().stream().map(fr -> FrecciaSchemaDto.builder()
                        .id(fr.getId())
                        .startX(fr.getStartX())
                        .startY(fr.getStartY())
                        .endX(fr.getEndX())
                        .endY(fr.getEndY())
                        .colore(fr.getColore())
                        .etichetta(fr.getEtichetta())
                        .build()).toList())
                .build();
    }

    // Referenza circolare evitata con metodo lazy
    private FormazioneService buildFormazioneService() {
        return new FormazioneService(formazioneRepository, giocatoreRepository);
    }
}

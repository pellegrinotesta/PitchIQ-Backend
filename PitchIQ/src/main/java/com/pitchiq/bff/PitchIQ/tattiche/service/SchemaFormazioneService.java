package com.pitchiq.bff.PitchIQ.tattiche.service;

import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloCategoria;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.dto.*;
import com.pitchiq.bff.PitchIQ.tattiche.model.*;
import com.pitchiq.bff.PitchIQ.tattiche.repository.FormazioneRepository;
import com.pitchiq.bff.PitchIQ.tattiche.repository.SchemaFormazioneRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchemaFormazioneService {

    private final SchemaFormazioneRepository schemaRepository;
    private final FormazioneRepository formazioneRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final PartitaRepository partitaRepository;

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

    /**
     * Applica uno schema a una formazione: mette un giocatore su ogni slot dello schema.
     *
     * <p>Lo slot indica una categoria (es. DIFENSORE) o un ruolo specifico (es. TSS). Hanno la precedenza
     * i titolari già in formazione, poi il resto della rosa disponibile; uno slot senza giocatori del ruolo
     * giusto viene occupato da un titolare rimasto fuori, così nessuno sparisce dal campo.
     * Se non si trova nessun giocatore la formazione resta com'era.
     */
    @Transactional
    public FormazioneDto applicaAFormazione(Long schemaId, Long formazioneId) {
        SchemaFormazione schema = getOrThrow(schemaId);
        Formazione formazione = formazioneRepository.findByIdWithPosizioni(formazioneId)
                .orElseThrow(() -> new ResourceNotFoundException("Formazione non trovata: " + formazioneId));

        List<Giocatore> disponibili = giocatoreRepository.findAllByStato(StatoGiocatore.ATTIVO);
        Map<Long, Giocatore> perId = disponibili.stream()
                .collect(Collectors.toMap(Giocatore::getId, g -> g, (a, b) -> a, LinkedHashMap::new));
        List<Giocatore> titolariAttuali = formazione.getPosizioni().stream()
                .filter(p -> Boolean.TRUE.equals(p.getTitolare()))
                .map(p -> perId.get(p.getGiocatoreId()))
                .filter(Objects::nonNull)
                .toList();
        List<Giocatore> candidati = new ArrayList<>(titolariAttuali);
        disponibili.stream().filter(g -> !candidati.contains(g)).forEach(candidati::add);

        Map<SlotSchema, Giocatore> assegnati = new LinkedHashMap<>();
        Set<Long> usati = new HashSet<>();
        for (SlotSchema slot : schema.getSlot()) {
            candidati.stream()
                    .filter(g -> !usati.contains(g.getId()) && compatibile(slot.getRuolo(), g))
                    .findFirst()
                    .ifPresent(g -> { assegnati.put(slot, g); usati.add(g.getId()); });
        }
        // slot rimasti vuoti: meglio un titolare fuori ruolo che un titolare tolto dal campo
        for (SlotSchema slot : schema.getSlot()) {
            if (assegnati.containsKey(slot)) continue;
            titolariAttuali.stream()
                    .filter(g -> !usati.contains(g.getId()))
                    .findFirst()
                    .ifPresent(g -> { assegnati.put(slot, g); usati.add(g.getId()); });
        }
        if (assegnati.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Nessun giocatore disponibile per i ruoli dello schema: la formazione non è stata modificata.");
        }

        // la panchina resta; i titolari vengono ridisposti secondo lo schema
        List<PosizioneCampo> panchina = formazione.getPosizioni().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getTitolare()) && !usati.contains(p.getGiocatoreId()))
                .map(p -> copia(p, formazione))
                .toList();
        formazione.getPosizioni().clear();
        formazioneRepository.saveAndFlush(formazione); // cancella prima di reinserire: vincolo unico formazione-giocatore

        assegnati.forEach((slot, g) -> {
            PosizioneCampo pos = new PosizioneCampo();
            pos.setFormazione(formazione);
            pos.setGiocatoreId(g.getId());
            pos.setCoordX(slot.getCoordX());
            pos.setCoordY(slot.getCoordY());
            pos.setSlotRuolo(slot.getSlotRuolo());
            pos.setTitolare(true);
            formazione.getPosizioni().add(pos);
        });
        formazione.getPosizioni().addAll(panchina);
        formazione.setModulo(schema.getModulo());
        // le fasi dello schema diventano quelle della formazione: ogni slot prende il giocatore assegnato
        if (schema.getFasi() != null && !schema.getFasi().isEmpty()) {
            Map<String, Long> giocatorePerSlot = new HashMap<>();
            assegnati.forEach((slot, g) -> giocatorePerSlot.put(slot.getSlotRuolo(), g.getId()));
            Map<FaseGioco, FaseFormazione> fasi = new EnumMap<>(FaseGioco.class);
            schema.getFasi().forEach((fase, contenuto) -> fasi.put(fase, new FaseFormazione(
                    contenuto.posizioni() == null ? List.of() : contenuto.posizioni().stream()
                            .filter(p -> giocatorePerSlot.containsKey(p.slot()))
                            .map(p -> new FaseFormazione.PosizioneFase(giocatorePerSlot.get(p.slot()), p.coordX(), p.coordY()))
                            .toList(),
                    contenuto.frecce() == null ? List.of() : contenuto.frecce())));
            formazione.setFasi(fasi);
        }
        formazioneRepository.save(formazione);

        return buildFormazioneService().findById(formazioneId);
    }

    /** Lo slot può indicare una categoria (DIFENSORE) o un ruolo specifico (TSS). */
    static boolean compatibile(String ruoloSlot, Giocatore g) {
        if (ruoloSlot == null || g.getRuolo() == null) return false;
        return ruoloSlot.equals(g.getRuolo().name()) || ruoloSlot.equals(RuoloCategoria.of(g.getRuolo()).name());
    }

    private static PosizioneCampo copia(PosizioneCampo origine, Formazione formazione) {
        PosizioneCampo p = new PosizioneCampo();
        p.setFormazione(formazione);
        p.setGiocatoreId(origine.getGiocatoreId());
        p.setCoordX(origine.getCoordX());
        p.setCoordY(origine.getCoordY());
        p.setSlotRuolo(origine.getSlotRuolo());
        p.setTitolare(false);
        return p;
    }

    // --- Privato ---

    private SchemaFormazione getOrThrow(Long id) {
        // Prima carica slot, poi frecce — Hibernate le unisce nell'entity cached
        schemaRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema non trovato: " + id));
        return schemaRepository.findByIdWithFrecce(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema non trovato: " + id));
    }

    static final int MAX_SLOT = 11;

    private void applyRequest(SchemaFormazione s, SchemaFormazioneRequest req) {
        List<SlotSchemaRequest> slotRichiesti = req.slot() == null ? List.of() : req.slot();
        if (slotRichiesti.size() > MAX_SLOT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno schema ha al massimo 11 slot.");
        }
        Set<String> nomi = new HashSet<>();
        for (SlotSchemaRequest sr : slotRichiesti) {
            // le fasi si riferiscono agli slot per nome: deve essere unico
            if (!nomi.add(sr.slotRuolo().trim().toUpperCase(Locale.ROOT))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome di slot ripetuto: " + sr.slotRuolo());
            }
        }
        s.setNome(req.nome());
        s.setModulo(req.modulo());
        s.setDescrizione(req.descrizione());

        s.getSlot().clear();
        if (req.slot() != null) {
            req.slot().forEach(sr -> {
                SlotSchema slot = new SlotSchema();
                slot.setSchema(s);
                slot.setSlotRuolo(sr.slotRuolo().trim());
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
        s.setFasi(fasiDegliSlot(req.fasi(), s.getSlot().stream().map(SlotSchema::getSlotRuolo).collect(Collectors.toSet())));
    }

    /** Tiene nelle fasi solo gli slot esistenti; le fasi vuote non vengono salvate. */
    static Map<FaseGioco, FaseSchema> fasiDegliSlot(Map<FaseGioco, FaseSchema> fasi, Set<String> slot) {
        if (fasi == null || fasi.isEmpty()) return null;
        Map<FaseGioco, FaseSchema> pulite = new EnumMap<>(FaseGioco.class);
        fasi.forEach((fase, contenuto) -> {
            if (fase == null || contenuto == null) return;
            List<FaseSchema.PosizioneSlot> posizioni = contenuto.posizioni() == null ? List.of()
                    : contenuto.posizioni().stream().filter(p -> slot.contains(p.slot())).toList();
            List<FaseFormazione.FrecciaFase> frecce = contenuto.frecce() == null ? List.of() : contenuto.frecce();
            if (!posizioni.isEmpty() || !frecce.isEmpty()) pulite.put(fase, new FaseSchema(posizioni, frecce));
        });
        return pulite.isEmpty() ? null : pulite;
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
                .fasi(s.getFasi() == null ? Map.of() : s.getFasi())
                .build();
    }

    // Referenza circolare evitata con metodo lazy
    private FormazioneService buildFormazioneService() {
        return new FormazioneService(formazioneRepository, giocatoreRepository, partitaRepository);
    }
}

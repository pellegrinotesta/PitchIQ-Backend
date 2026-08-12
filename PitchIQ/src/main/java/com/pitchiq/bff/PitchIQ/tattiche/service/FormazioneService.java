package com.pitchiq.bff.PitchIQ.tattiche.service;


import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneDto;
import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneRequest;
import com.pitchiq.bff.PitchIQ.tattiche.dto.PosizioneCampoDto;
import com.pitchiq.bff.PitchIQ.tattiche.model.Formazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.PosizioneCampo;
import com.pitchiq.bff.PitchIQ.tattiche.repository.FormazioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormazioneService {

    private final FormazioneRepository formazioneRepository;
    private final GiocatoreRepository giocatoreRepository;

    public List<FormazioneDto> findAll() {
        return formazioneRepository.findAllOrderByDataDesc().stream()
                .map(f -> toDto(f, List.of()))
                .toList();
    }

    public FormazioneDto findById(Long id) {
        Formazione f = formazioneRepository.findByIdWithPosizioni(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formazione non trovata: " + id));

        // Carica i giocatori coinvolti in un colpo solo
        List<Long> giocatoreIds = f.getPosizioni().stream()
                .map(PosizioneCampo::getGiocatoreId)
                .toList();

        Map<Long, Giocatore> giocatoriMap = giocatoreRepository.findAllById(giocatoreIds)
                .stream().collect(Collectors.toMap(Giocatore::getId, g -> g));

        List<PosizioneCampoDto> posizioni = f.getPosizioni().stream()
                .map(p -> toPosizione(p, giocatoriMap.get(p.getGiocatoreId())))
                .toList();

        return toDto(f, posizioni);
    }

    @Transactional
    public FormazioneDto create(FormazioneRequest req) {
        Formazione f = new Formazione();
        applyRequest(f, req);
        Formazione saved = formazioneRepository.save(f);
        return findById(saved.getId()); // ricarica con posizioni e dati giocatori
    }

    @Transactional
    public FormazioneDto update(Long id, FormazioneRequest req) {
        Formazione f = formazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formazione non trovata: " + id));
        applyRequest(f, req);
        formazioneRepository.save(f);
        return findById(id); // ricarica con posizioni e dati giocatori
    }

    @Transactional
    public void delete(Long id) {
        if (!formazioneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Formazione non trovata: " + id);
        }
        formazioneRepository.deleteById(id);
    }

    // --- Mapping ---

    private void applyRequest(Formazione f, FormazioneRequest req) {
        f.setNome(req.nome());
        f.setModulo(req.modulo());
        f.setData(req.data());
        f.setNote(req.note());
        f.getPosizioni().clear();

        if (req.posizioni() != null) {
            req.posizioni().forEach(p -> {
                PosizioneCampo pos = new PosizioneCampo();
                pos.setFormazione(f);
                pos.setGiocatoreId(p.giocatoreId());
                pos.setCoordX(p.coordX());
                pos.setCoordY(p.coordY());
                pos.setSlotRuolo(p.slotRuolo());
                pos.setTitolare(p.titolare() != null ? p.titolare() : true);
                f.getPosizioni().add(pos);
            });
        }
    }

    private FormazioneDto toDto(Formazione f, List<PosizioneCampoDto> posizioni) {
        return FormazioneDto.builder()
                .id(f.getId())
                .nome(f.getNome())
                .modulo(f.getModulo())
                .data(f.getData())
                .note(f.getNote())
                .posizioni(posizioni)
                .build();
    }

    private PosizioneCampoDto toPosizione(PosizioneCampo p, Giocatore g) {
        return PosizioneCampoDto.builder()
                .id(p.getId())
                .giocatoreId(p.getGiocatoreId())
                .nomeGiocatore(g != null ? g.getNome() : "–")
                .cognomeGiocatore(g != null ? g.getCognome() : "–")
                .numeroMaglia(g != null ? g.getNumeroMaglia() : null)
                .coordX(p.getCoordX())
                .coordY(p.getCoordY())
                .slotRuolo(p.getSlotRuolo())
                .titolare(p.getTitolare())
                .build();
    }
}
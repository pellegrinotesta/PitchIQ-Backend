package com.pitchiq.bff.PitchIQ.allenamenti.service;

import com.pitchiq.bff.PitchIQ.allenamenti.dto.PresenzaDto;
import com.pitchiq.bff.PitchIQ.allenamenti.dto.SedutaDto;
import com.pitchiq.bff.PitchIQ.allenamenti.dto.SedutaRequest;
import com.pitchiq.bff.PitchIQ.allenamenti.model.Presenza;
import com.pitchiq.bff.PitchIQ.allenamenti.model.SedutaAllenamento;
import com.pitchiq.bff.PitchIQ.allenamenti.repository.SedutaAllenamentoRepository;
import com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SedutaAllenamentoService {

    private final SedutaAllenamentoRepository repository;
    private final GiocatoreRepository giocatoreRepository;

    public List<SedutaDto> findAll() {
        return repository.findAllOrdered().stream()
                .map(s -> toDto(s, List.of()))
                .toList();
    }

    public List<SedutaDto> findByPeriodo(LocalDate from, LocalDate to) {
        return repository.findAllByDataBetweenOrderByDataAsc(from, to).stream()
                .map(s -> toDto(s, List.of()))
                .toList();
    }

    public SedutaDto findById(Long id) {
        SedutaAllenamento s = repository.findByIdWithPresenze(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seduta non trovata: " + id));

        List<Long> ids = s.getPresenze().stream()
                .map(Presenza::getGiocatoreId).toList();

        Map<Long, Giocatore> gMap = giocatoreRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Giocatore::getId, g -> g));

        List<PresenzaDto> presenze = s.getPresenze().stream()
                .map(p -> toPresenzaDto(p, gMap.get(p.getGiocatoreId())))
                .toList();

        return toDto(s, presenze);
    }

    @Transactional
    public SedutaDto create(SedutaRequest req) {
        SedutaAllenamento s = new SedutaAllenamento();
        applyRequest(s, req);
        SedutaAllenamento saved = repository.save(s);
        return findById(saved.getId());
    }

    @Transactional
    public SedutaDto update(Long id, SedutaRequest req) {
        SedutaAllenamento s = repository.findByIdWithPresenze(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seduta non trovata: " + id));
        applyRequest(s, req);
        repository.save(s);
        return findById(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Seduta non trovata: " + id);
        }
        repository.deleteById(id);
    }

    // Aggiunge automaticamente tutti i giocatori attivi come presenti
    // quando si crea una seduta senza specificare le presenze
    @Transactional
    public SedutaDto generaPresenze(Long id) {
        SedutaAllenamento s = repository.findByIdWithPresenze(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seduta non trovata: " + id));

        List<Giocatore> rosa = giocatoreRepository
                .findAllByStato(StatoGiocatore.ATTIVO);

        s.getPresenze().clear();
        rosa.forEach(g -> {
            Presenza p = new Presenza();
            p.setSeduta(s);
            p.setGiocatoreId(g.getId());
            p.setPresente(true);
            s.getPresenze().add(p);
        });

        repository.save(s);
        return findById(id);
    }

    // --- Mapping ---

    private void applyRequest(SedutaAllenamento s, SedutaRequest req) {
        s.setData(req.data());
        s.setOraInizio(req.oraInizio());
        s.setOraFine(req.oraFine());
        s.setTipo(req.tipo());
        s.setLuogo(req.luogo());
        s.setNote(req.note());

        s.getPresenze().clear();
        if (req.presenze() != null) {
            req.presenze().forEach(pr -> {
                Presenza p = new Presenza();
                p.setSeduta(s);
                p.setGiocatoreId(pr.giocatoreId());
                p.setPresente(pr.presente());
                p.setMotivoAssenza(pr.motivoAssenza());
                p.setValutazione(pr.valutazione());
                s.getPresenze().add(p);
            });
        }
    }

    private SedutaDto toDto(SedutaAllenamento s, List<PresenzaDto> presenze) {
        int presenti = (int) presenze.stream().filter(p -> Boolean.TRUE.equals(p.presente())).count();
        return SedutaDto.builder()
                .id(s.getId())
                .data(s.getData())
                .oraInizio(s.getOraInizio())
                .oraFine(s.getOraFine())
                .tipo(s.getTipo())
                .luogo(s.getLuogo())
                .note(s.getNote())
                .presenze(presenze)
                .totalePresenti(presenti)
                .totaleAssenti(presenze.size() - presenti)
                .build();
    }

    private PresenzaDto toPresenzaDto(Presenza p, Giocatore g) {
        return PresenzaDto.builder()
                .id(p.getId())
                .giocatoreId(p.getGiocatoreId())
                .nomeGiocatore(g != null ? g.getNome() : "–")
                .cognomeGiocatore(g != null ? g.getCognome() : "–")
                .numeroMaglia(g != null ? g.getNumeroMaglia() : null)
                .ruolo(g != null ? g.getRuolo().name() : "–")
                .presente(p.getPresente())
                .motivoAssenza(p.getMotivoAssenza())
                .valutazione(p.getValutazione())
                .build();
    }
}
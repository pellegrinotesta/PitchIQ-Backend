package com.pitchiq.bff.PitchIQ.allenamenti.service;

import com.pitchiq.bff.PitchIQ.allenamenti.dto.CaricoGiocatoreDto;
import com.pitchiq.bff.PitchIQ.allenamenti.model.Presenza;
import com.pitchiq.bff.PitchIQ.allenamenti.model.SedutaAllenamento;
import com.pitchiq.bff.PitchIQ.allenamenti.repository.PresenzaRepository;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaricoLavoroService {

    static final int GIORNI_ACUTO = 7;
    static final int GIORNI_CRONICO = 28;

    private final PresenzaRepository presenzaRepository;
    private final GiocatoreRepository giocatoreRepository;

    /** Carico di ogni giocatore con almeno una presenza nei 28 giorni che terminano in {@code al}. */
    public List<CaricoGiocatoreDto> carico(LocalDate al) {
        LocalDate inizioCronico = al.minusDays(GIORNI_CRONICO - 1);
        LocalDate inizioAcuto = al.minusDays(GIORNI_ACUTO - 1);
        // Prima data della metà più recente della finestra cronica
        LocalDate meta = al.minusDays(GIORNI_CRONICO / 2 - 1);

        Map<Long, List<Presenza>> perGiocatore = presenzaRepository.findPresentiTra(inizioCronico, al).stream()
                .collect(Collectors.groupingBy(Presenza::getGiocatoreId));
        Map<Long, Giocatore> giocatori = giocatoreRepository.findAllById(perGiocatore.keySet()).stream()
                .collect(Collectors.toMap(Giocatore::getId, g -> g));

        return perGiocatore.entrySet().stream().map(e -> {
            List<Presenza> conRpe = e.getValue().stream().filter(p -> p.getRpe() != null).toList();
            int acuto = conRpe.stream().filter(p -> !p.getSeduta().getData().isBefore(inizioAcuto))
                    .mapToInt(CaricoLavoroService::caricoSeduta).sum();
            int totale = conRpe.stream().mapToInt(CaricoLavoroService::caricoSeduta).sum();
            double cronico = totale / (GIORNI_CRONICO / (double) GIORNI_ACUTO);
            boolean storicoInsufficiente = conRpe.stream().noneMatch(p -> p.getSeduta().getData().isBefore(meta));
            Giocatore g = giocatori.get(e.getKey());
            return new CaricoGiocatoreDto(
                    e.getKey(),
                    g != null ? g.getNome() : "–",
                    g != null ? g.getCognome() : "–",
                    g != null ? g.getNumeroMaglia() : null,
                    g != null ? g.getRuolo().name() : "–",
                    acuto,
                    Math.round(cronico * 10) / 10.0,
                    cronico > 0 ? Math.round(acuto / cronico * 100) / 100.0 : null,
                    conRpe.size(),
                    e.getValue().size() - conRpe.size(),
                    storicoInsufficiente);
        }).sorted(Comparator.comparing(CaricoGiocatoreDto::cognomeGiocatore)
                .thenComparing(CaricoGiocatoreDto::nomeGiocatore)).toList();
    }

    /** RPE × minuti; senza minuti individuali vale la durata della seduta. */
    static int caricoSeduta(Presenza p) {
        return p.getRpe() * minutiEffettivi(p);
    }

    static int minutiEffettivi(Presenza p) {
        if (p.getMinuti() != null) return p.getMinuti();
        SedutaAllenamento s = p.getSeduta();
        if (s.getOraInizio() == null || s.getOraFine() == null) return 0;
        return (int) Math.max(0, Duration.between(s.getOraInizio(), s.getOraFine()).toMinutes());
    }
}

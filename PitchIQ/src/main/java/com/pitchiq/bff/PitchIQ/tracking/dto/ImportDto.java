package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.pitchiq.bff.PitchIQ.tracking.entity.StatoImport;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingImport;

import java.time.LocalDateTime;

public record ImportDto(
        Long id,
        StatoImport stato,
        String messaggio,
        Long matchId,
        boolean conTracking,
        String nomeFileEventi,
        LocalDateTime creatoIl,
        LocalDateTime aggiornatoIl) {

    public static ImportDto from(TrackingImport i) {
        return new ImportDto(i.getId(), i.getStato(), i.getMessaggio(), i.getMatchId(),
                i.isConTracking(), i.getNomeFileEventi(), i.getCreatoIl(), i.getAggiornatoIl());
    }
}

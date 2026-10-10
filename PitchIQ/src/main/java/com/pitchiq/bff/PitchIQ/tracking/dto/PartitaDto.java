package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingPartita;

import java.time.LocalDateTime;

/** Riga della lista partite, con i conteggi per esito. */
public record PartitaDto(
        Long matchId,
        LocalDateTime dataPartita,
        String squadraCasa,
        String squadraTrasferta,
        int golCasa,
        int golTrasferta,
        String competizione,
        long possessiPressati,
        long pressioniSuperate) {

    public static PartitaDto from(TrackingPartita p, long possessi, long superate) {
        return new PartitaDto(p.getMatchId(), p.getDataPartita(), p.getSquadraCasa(),
                p.getSquadraTrasferta(), p.getGolCasa(), p.getGolTrasferta(),
                p.getCompetizione(), possessi, superate);
    }
}

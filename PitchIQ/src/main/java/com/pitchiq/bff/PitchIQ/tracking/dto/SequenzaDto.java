package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingSequenza;

import java.math.BigDecimal;

public record SequenzaDto(
        Long id,
        Long matchId,
        EsitoPressione esito,
        int periodo,
        int minuto,
        int secondo,
        Long teamId,
        String squadra,
        Long portatoreId,
        String portatore,
        String terzo,
        int nPressori,
        int nOpzioni,
        BigDecimal valorePortatore,
        BigDecimal valoreMedio,
        BigDecimal valoreMigliore,
        String esitoPossesso,
        boolean portaAlTiro,
        boolean haFrame) {

    public static SequenzaDto from(TrackingSequenza s) {
        return new SequenzaDto(s.getId(), s.getMatchId(), s.getEsito(), s.getPeriodo(), s.getMinuto(),
                s.getSecondo(), s.getTeamId(), s.getSquadra(), s.getPortatoreId(), s.getPortatore(),
                s.getTerzo(), s.getNPressori(), s.getNOpzioni(), s.getValorePortatore(), s.getValoreMedio(),
                s.getValoreMigliore(), s.getEsitoPossesso(), s.isPortaAlTiro(), s.isHaFrame());
    }
}

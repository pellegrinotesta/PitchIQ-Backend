package com.pitchiq.bff.PitchIQ.statistiche.dto;

import lombok.Builder;

@Builder
public record ConfrontoDto(
        GiocatoreRadarDto giocatore1,
        GiocatoreRadarDto giocatore2
) {
}

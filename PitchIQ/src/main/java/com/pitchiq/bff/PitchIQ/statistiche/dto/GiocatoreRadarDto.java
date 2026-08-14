package com.pitchiq.bff.PitchIQ.statistiche.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GiocatoreRadarDto(
        Long giocatoreId,
        String nomeGiocatore,
        List<PuntoRadarDto> valori
) {
}

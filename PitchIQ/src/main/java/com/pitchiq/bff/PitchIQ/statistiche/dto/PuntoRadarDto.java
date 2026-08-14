package com.pitchiq.bff.PitchIQ.statistiche.dto;

import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import lombok.Builder;

@Builder
public record PuntoRadarDto(
        CategoriaMetrica categoria,
        String label,
        Double valore,        // valore reale medio
        Double valorePct
) {
}

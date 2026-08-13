package com.pitchiq.bff.PitchIQ.statistiche.dto;

import lombok.Builder;

@Builder
public record PuntoTrendDto(
        String periodo,
        Double valore
) {}
package com.pitchiq.bff.PitchIQ.partite.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record HeatmapZonaDto(
        Long id,
        Long giocatoreId,
        BigDecimal coordX,
        BigDecimal coordY,
        Integer intensita
) {}

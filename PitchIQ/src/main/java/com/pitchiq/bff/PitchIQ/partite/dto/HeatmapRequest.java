package com.pitchiq.bff.PitchIQ.partite.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record HeatmapRequest(
        @NotNull Long giocatoreId,
        @NotNull BigDecimal coordX,
        @NotNull BigDecimal coordY,
        Integer intensita
) {}

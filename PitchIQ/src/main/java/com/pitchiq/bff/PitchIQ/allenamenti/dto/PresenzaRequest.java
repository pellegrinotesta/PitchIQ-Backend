package com.pitchiq.bff.PitchIQ.allenamenti.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PresenzaRequest(
        @NotNull Long giocatoreId,
        @NotNull Boolean presente,
        String motivoAssenza,
        Integer valutazione,
        @Min(0) @Max(600) Integer minuti,
        @Min(1) @Max(10) Integer rpe
) {}

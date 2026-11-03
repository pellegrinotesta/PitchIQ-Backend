package com.pitchiq.bff.PitchIQ.partite.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StatsBombImportRequest(
        @Positive int competitionId,
        @Positive int seasonId,
        @Positive int matchId,
        @NotNull Lato lato
) {
    public enum Lato { CASA, TRASFERTA }
}
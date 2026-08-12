package com.pitchiq.bff.PitchIQ.tattiche.dto;

import jakarta.validation.constraints.NotNull;

public record FrecciaSchemaRequest(
        @NotNull Double startX,
        @NotNull Double startY,
        @NotNull Double endX,
        @NotNull Double endY,
        String colore,
        String etichetta
) {}

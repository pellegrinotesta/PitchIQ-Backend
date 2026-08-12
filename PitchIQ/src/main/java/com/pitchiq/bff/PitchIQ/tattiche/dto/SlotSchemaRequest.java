package com.pitchiq.bff.PitchIQ.tattiche.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlotSchemaRequest(
        @NotBlank String slotRuolo,
        @NotBlank String ruolo,
        @NotNull Double coordX,
        @NotNull Double coordY,
        String note
) {}

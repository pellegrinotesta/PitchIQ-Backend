package com.pitchiq.bff.PitchIQ.tattiche.dto;

import lombok.Builder;

@Builder
public record SlotSchemaDto(
        Long id,
        String slotRuolo,
        String ruolo,
        Double coordX,
        Double coordY,
        String note
) {
}

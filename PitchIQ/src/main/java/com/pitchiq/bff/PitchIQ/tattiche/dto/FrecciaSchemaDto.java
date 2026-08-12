package com.pitchiq.bff.PitchIQ.tattiche.dto;

import lombok.Builder;

@Builder
public record FrecciaSchemaDto(
        Long id,
        Double startX,
        Double startY,
        Double endX,
        Double endY,
        String colore,
        String etichetta
) {
}

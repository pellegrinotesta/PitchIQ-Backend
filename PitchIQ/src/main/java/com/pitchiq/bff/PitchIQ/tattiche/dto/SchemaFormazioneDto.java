package com.pitchiq.bff.PitchIQ.tattiche.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SchemaFormazioneDto(
        Long id,
        String nome,
        String modulo,
        String descrizione,
        List<SlotSchemaDto> slot,
        List<FrecciaSchemaDto> frecce
) {
}

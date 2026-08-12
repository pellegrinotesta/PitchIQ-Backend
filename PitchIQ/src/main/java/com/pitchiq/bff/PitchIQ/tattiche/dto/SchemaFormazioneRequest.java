package com.pitchiq.bff.PitchIQ.tattiche.dto;


import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SchemaFormazioneRequest(
        @NotBlank String nome,
        @NotBlank String modulo,
        String descrizione,
        List<SlotSchemaRequest> slot,
        List<FrecciaSchemaRequest> frecce
) {
}

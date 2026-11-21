package com.pitchiq.bff.PitchIQ.tattiche.dto;


import com.pitchiq.bff.PitchIQ.tattiche.model.FaseGioco;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

import java.util.List;

public record SchemaFormazioneRequest(
        @NotBlank String nome,
        @NotBlank String modulo,
        String descrizione,
        List<SlotSchemaRequest> slot,
        List<FrecciaSchemaRequest> frecce,
        Map<FaseGioco, @Valid FaseSchema> fasi
) {
}

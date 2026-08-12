package com.pitchiq.bff.PitchIQ.tattiche.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record FormazioneRequest(
        @NotBlank String nome,
        @NotBlank String modulo,
        LocalDate data,
        String note,
        List<PosizioneCampoRequest> posizioni
) {
}

package com.pitchiq.bff.PitchIQ.tattiche.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record FormazioneDto(
        Long id,
        String nome,
        String modulo,
        LocalDate data,
        String note,
        List<PosizioneCampoDto> posizioni
) {
}

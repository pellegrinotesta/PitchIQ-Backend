package com.pitchiq.bff.PitchIQ.tattiche.dto;

import com.pitchiq.bff.PitchIQ.tattiche.model.FaseFormazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseGioco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

import java.time.LocalDate;
import java.util.List;

public record FormazioneRequest(
        @NotBlank String nome,
        @NotBlank String modulo,
        LocalDate data,
        String note,
        Long partitaId,
        List<PosizioneCampoRequest> posizioni,
        Map<FaseGioco, @Valid FaseFormazione> fasi
) {
}

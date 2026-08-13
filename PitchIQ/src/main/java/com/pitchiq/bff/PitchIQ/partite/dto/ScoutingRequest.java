package com.pitchiq.bff.PitchIQ.partite.dto;

import jakarta.validation.constraints.NotBlank;

public record ScoutingRequest(
        @NotBlank String nome,
        String modulo,
        String puntiForza,
        String debolezze,
        String giocatoriChiave,
        String note
) {}

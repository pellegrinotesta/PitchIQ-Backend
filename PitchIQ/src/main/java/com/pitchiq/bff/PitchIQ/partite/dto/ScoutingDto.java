package com.pitchiq.bff.PitchIQ.partite.dto;

import lombok.Builder;

@Builder
public record ScoutingDto(
        Long id,
        String nome,
        String modulo,
        String puntiForza,
        String debolezze,
        String giocatoriChiave,
        String note
) {}

package com.pitchiq.bff.PitchIQ.tracking.dto;

public record PartitaSbDto(
        int matchId,
        String matchDate,
        String squadraCasa,
        String squadraTrasferta,
        int golCasa,
        int golTrasferta,
        String stadio,
        String arbitro
) {}

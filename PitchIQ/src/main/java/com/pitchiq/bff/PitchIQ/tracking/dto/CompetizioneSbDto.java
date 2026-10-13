package com.pitchiq.bff.PitchIQ.tracking.dto;

public record CompetizioneSbDto(
        int competitionId,
        String competitionName,
        int seasonId,
        String seasonName,
        String countryName
) {}
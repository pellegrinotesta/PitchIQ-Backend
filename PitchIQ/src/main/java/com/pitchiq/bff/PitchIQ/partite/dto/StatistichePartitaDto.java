package com.pitchiq.bff.PitchIQ.partite.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record StatistichePartitaDto(
        BigDecimal possessoPct,
        Integer tiriTotali,
        Integer tiriInPorta,
        Integer passaggi,
        Integer passaggiRiusciti,
        Integer duelliVinti,
        Integer duelliTotali,
        Integer corner,
        Integer falli,
        Integer fuorigioco,
        BigDecimal xg,
        String fonte
) {}

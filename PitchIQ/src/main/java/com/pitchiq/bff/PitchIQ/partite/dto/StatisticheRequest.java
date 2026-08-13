package com.pitchiq.bff.PitchIQ.partite.dto;

import java.math.BigDecimal;

public record StatisticheRequest(
        Integer possessoPct,
        Integer tiriTotali,
        Integer tiriInPorta,
        Integer passaggi,
        Integer passaggiRiusciti,
        Integer duelliVinti,
        Integer duelliTotali,
        Integer corner,
        Integer falli,
        Integer fuorigioco,
        BigDecimal xg
) {}
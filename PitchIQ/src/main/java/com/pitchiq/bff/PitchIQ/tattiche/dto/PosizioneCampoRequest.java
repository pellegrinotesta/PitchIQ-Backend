package com.pitchiq.bff.PitchIQ.tattiche.dto;

import jakarta.validation.constraints.NotNull;

public record PosizioneCampoRequest(
        @NotNull Long giocatoreId,
        @NotNull Double coordX,
        @NotNull Double coordY,
        String slotRuolo,
        Boolean titolare
) {
}

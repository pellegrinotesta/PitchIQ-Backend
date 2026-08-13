package com.pitchiq.bff.PitchIQ.allenamenti.dto;

import jakarta.validation.constraints.NotNull;

public record PresenzaRequest(
        @NotNull Long giocatoreId,
        @NotNull Boolean presente,
        String motivoAssenza,
        Integer valutazione
) {}

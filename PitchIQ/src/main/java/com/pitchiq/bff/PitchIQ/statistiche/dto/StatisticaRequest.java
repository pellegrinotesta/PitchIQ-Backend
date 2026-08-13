package com.pitchiq.bff.PitchIQ.statistiche.dto;

import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatisticaRequest(
        @NotNull Long giocatoreId,
        @NotBlank String periodo,        // "YYYY-MM"
        @NotNull CategoriaMetrica categoria,
        @NotNull Double valore
) {}
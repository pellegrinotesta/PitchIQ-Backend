package com.pitchiq.bff.PitchIQ.statistiche.dto;

import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import lombok.Builder;

@Builder
public record StatisticaDto(
        Long id,
        Long giocatoreId,
        String periodo,
        CategoriaMetrica categoria,
        Double valore
) {}

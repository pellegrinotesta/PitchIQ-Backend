package com.pitchiq.bff.PitchIQ.partite.dto;

import com.pitchiq.bff.PitchIQ.partite.model.TipoEvento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EventoRequest(
        @NotNull Integer minuto,
        @NotNull TipoEvento tipo,
        Long giocatoreId,
        String nota,
        BigDecimal coordX,
        BigDecimal coordY
) {}

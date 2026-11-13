package com.pitchiq.bff.PitchIQ.partite.dto;

import com.pitchiq.bff.PitchIQ.partite.model.TipoEvento;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record EventoDto(
        Long id,
        Integer minuto,
        Integer minutoAggiuntivo,
        TipoEvento tipo,
        Long giocatoreId,
        String nomeGiocatore,
        String cognomeGiocatore,
        String nota,
        BigDecimal coordX,
        BigDecimal coordY,
        BigDecimal xg
) {}

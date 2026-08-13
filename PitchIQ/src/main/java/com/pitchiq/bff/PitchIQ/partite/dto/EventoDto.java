package com.pitchiq.bff.PitchIQ.partite.dto;

import com.pitchiq.bff.PitchIQ.partite.model.TipoEvento;
import lombok.Builder;

@Builder
public record EventoDto(
        Long id,
        Integer minuto,
        TipoEvento tipo,
        Long giocatoreId,
        String nomeGiocatore,
        String cognomeGiocatore,
        String nota
) {}

package com.pitchiq.bff.PitchIQ.tattiche.dto;

import lombok.Builder;

@Builder
public record PosizioneCampoDto(
        Long id,
        Long giocatoreId,
        String nomeGiocatore,   // denormalizzato per evitare N+1 lato frontend
        String cognomeGiocatore,
        Integer numeroMaglia,
        Double coordX,
        Double coordY,
        String slotRuolo,
        Boolean titolare
) {
}

package com.pitchiq.bff.PitchIQ.allenamenti.dto;

import lombok.Builder;

@Builder
public record PresenzaDto(
        Long id,
        Long giocatoreId,
        String nomeGiocatore,
        String cognomeGiocatore,
        Integer numeroMaglia,
        String ruolo,
        Boolean presente,
        String motivoAssenza,
        Integer valutazione
) {}

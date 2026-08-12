package com.pitchiq.bff.PitchIQ.squadre.dto;

import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GiocatoreDto(
        Long id,
        String nome,
        String cognome,
        LocalDate dataNascita,
        RuoloGiocatore ruolo,
        Integer numeroMaglia,
        String piedePreferito,
        LocalDate contrattoInizio,
        LocalDate contrattoFine,
        StatoGiocatore stato
) {
}

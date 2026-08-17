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
        Integer eta,
        RuoloGiocatore ruolo,
        String ruoliSecondari,
        Integer numeroMaglia,
        String piedePreferito,
        LocalDate contrattoInizio,
        LocalDate contrattoFine,
        StatoGiocatore stato,
        String nazionalita,
        String luogoNascita,
        Integer altezzaCm,
        Integer pesoKg,
        java.math.BigDecimal stipendioAnnuo,
        java.math.BigDecimal valoreMercato,
        java.math.BigDecimal clausola,
        String noteMediche,
        String agente
) {}

package com.pitchiq.bff.PitchIQ.squadre.dto;

import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record GiocatoreCreateRequest(
        @NotBlank String nome,
        @NotBlank String cognome,
        LocalDate dataNascita,
        @NotNull RuoloGiocatore ruolo,
        String ruoliSecondari,
        @Min(1) @Max(99) Integer numeroMaglia,
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
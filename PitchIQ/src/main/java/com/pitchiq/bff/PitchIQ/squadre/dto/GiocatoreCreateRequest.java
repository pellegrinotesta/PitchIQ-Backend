package com.pitchiq.bff.PitchIQ.squadre.dto;

import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record GiocatoreCreateRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 100)
        String cognome,

        LocalDate dataNascita,

        @NotNull(message = "Il ruolo è obbligatorio")
        RuoloGiocatore ruolo,

        @Min(1) @Max(99)
        Integer numeroMaglia,

        String piedePreferito,

        LocalDate contrattoInizio,

        LocalDate contrattoFine,

        StatoGiocatore stato
) {}
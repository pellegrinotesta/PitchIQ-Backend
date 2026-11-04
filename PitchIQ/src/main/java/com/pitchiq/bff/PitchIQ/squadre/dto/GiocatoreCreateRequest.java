package com.pitchiq.bff.PitchIQ.squadre.dto;

import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.MovimentoTattico;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record GiocatoreCreateRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 100) String cognome,
        @PastOrPresent LocalDate dataNascita,
        @NotNull RuoloGiocatore ruolo,
        @Size(max = 100) String ruoliSecondari,
        @NotNull @Min(1) @Max(99) Integer numeroMaglia,
        @Pattern(regexp = "DESTRO|SINISTRO|AMBIDESTRO") String piedePreferito,
        LocalDate contrattoInizio,
        LocalDate contrattoFine,
        StatoGiocatore stato,
                @Size(max = 50) String nazionalita,
                @Size(max = 100) String luogoNascita,
                @Min(100) @Max(250) Integer altezzaCm,
                @Min(30) @Max(250) Integer pesoKg,
                @PositiveOrZero @Digits(integer = 10, fraction = 2) java.math.BigDecimal stipendioAnnuo,
                @PositiveOrZero @Digits(integer = 10, fraction = 2) java.math.BigDecimal valoreMercato,
                @PositiveOrZero @Digits(integer = 10, fraction = 2) java.math.BigDecimal clausola,
                @Size(max = 500) String noteMediche,
                @Size(max = 100) String agente,
                @Size(max = 6) java.util.List<@NotNull MovimentoTattico> movimentiConsentiti
) {
        @com.fasterxml.jackson.annotation.JsonIgnore
        @AssertTrue(message = "La fine del contratto non puo precedere l'inizio")
        public boolean isContrattoCoerente() {
                return contrattoInizio == null || contrattoFine == null || !contrattoFine.isBefore(contrattoInizio);
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        @AssertTrue(message = "Usare codici ruolo validi separati da virgola")
        public boolean isRuoliSecondariValidi() {
                if (ruoliSecondari == null || ruoliSecondari.isBlank()) return true;
                if (ruoliSecondari.length() > 100) return false;
                var consentiti = java.util.Arrays.stream(RuoloGiocatore.values())
                                .map(Enum::name).collect(java.util.stream.Collectors.toSet());
                return java.util.Arrays.stream(ruoliSecondari.split(",", -1))
                                .allMatch(codice -> consentiti.contains(codice.trim()));
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        @AssertTrue(message = "I movimenti tattici non possono essere duplicati")
        public boolean isMovimentiUnici() {
                return movimentiConsentiti == null || new java.util.HashSet<>(movimentiConsentiti).size() == movimentiConsentiti.size();
        }
}
package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Fase di gioco di uno schema: come {@link FaseFormazione}, ma le posizioni si riferiscono agli slot
 * (per nome, es. "CB1") invece che ai giocatori. Gli slot senza posizione restano dove sono nello schema.
 */
public record FaseSchema(
        @Valid @Size(max = 11) List<PosizioneSlot> posizioni,
        @Valid @Size(max = 40) List<FaseFormazione.FrecciaFase> frecce
) {
    public record PosizioneSlot(
            @NotBlank @Size(max = 20) String slot,
            @DecimalMin("0") @DecimalMax("100") double coordX,
            @DecimalMin("0") @DecimalMax("100") double coordY
    ) {}
}

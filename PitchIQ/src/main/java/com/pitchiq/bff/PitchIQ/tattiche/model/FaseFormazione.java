package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Disposizione dei titolari in una fase di gioco e frecce dei movimenti.
 * Coordinate in percentuale del campo (0-100), come per lo schieramento di base.
 * I titolari senza posizione nella fase restano dove sono nello schieramento.
 */
public record FaseFormazione(
        @Valid @Size(max = 11) List<PosizioneFase> posizioni,
        @Valid @Size(max = 40) List<FrecciaFase> frecce
) {
    public record PosizioneFase(
            @NotNull Long giocatoreId,
            @DecimalMin("0") @DecimalMax("100") double coordX,
            @DecimalMin("0") @DecimalMax("100") double coordY
    ) {}

    /** tipo: CORSA (movimento senza palla), PASSAGGIO, PRESSIONE. */
    public record FrecciaFase(
            @DecimalMin("0") @DecimalMax("100") double startX,
            @DecimalMin("0") @DecimalMax("100") double startY,
            @DecimalMin("0") @DecimalMax("100") double endX,
            @DecimalMin("0") @DecimalMax("100") double endY,
            @NotNull @Pattern(regexp = "CORSA|PASSAGGIO|PRESSIONE") String tipo
    ) {}
}

package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;

/** Valori medi per esito della pressione. quotaTiro è tra 0 e 1. */
public record AggregatoEsitoDto(
        EsitoPressione esito,
        long possessi,
        double valoreMedio,
        double valoreMigliore,
        double quotaTiro) {

    /** Da una riga di TrackingSequenzaRepository.aggregatiPerEsito. */
    public static AggregatoEsitoDto fromRow(Object[] r) {
        return new AggregatoEsitoDto((EsitoPressione) r[0], ((Number) r[1]).longValue(),
                num(r[2]), num(r[3]), num(r[4]));
    }

    private static double num(Object o) {
        return o == null ? 0d : ((Number) o).doubleValue();
    }
}

package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record PartitaDettaglioDto(
        PartitaDto partita,
        List<SquadraDto> squadre,
        /** {"playerId": {"nome": "...", "teamId": 123, "numero": 10}} */
        JsonNode giocatori,
        List<AggregatoEsitoDto> aggregati) {

    public record SquadraDto(Long teamId, String nome) {
    }
}

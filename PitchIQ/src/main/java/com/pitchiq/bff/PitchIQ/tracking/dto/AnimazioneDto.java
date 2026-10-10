package com.pitchiq.bff.PitchIQ.tracking.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record AnimazioneDto(
        SequenzaDto sequenza,
        /** [playerId, ...] */
        JsonNode pressori,
        int pressInizio,
        int pressFine,
        /** [{"i":0,"b":[x,y],"c":carrierId,"p":[[id,att,x,y]],"o":{"id":valore}}], 10 fps, attacco verso destra */
        JsonNode frames) {
}

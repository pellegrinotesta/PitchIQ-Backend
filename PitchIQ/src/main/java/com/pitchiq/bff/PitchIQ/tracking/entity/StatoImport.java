package com.pitchiq.bff.PitchIQ.tracking.entity;

public enum StatoImport {
    IN_CODA, IN_ELABORAZIONE, COMPLETATO, ERRORE;

    public boolean isConcluso() {
        return this == COMPLETATO || this == ERRORE;
    }
}

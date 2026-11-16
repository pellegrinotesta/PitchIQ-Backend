package com.pitchiq.bff.PitchIQ.squadre.model;

/** Dove il giocatore è stato formato, ai fini della lista dei 25 del campionato. */
public enum FormazioneVivaio {
    /** Formato nel club: tesserato per il club per tre stagioni tra i 15 e i 21 anni */
    CLUB,
    /** Formato in Italia: stesso requisito, in un altro club italiano */
    ITALIA,
    /** Nessuna formazione locale */
    ALTRO
}

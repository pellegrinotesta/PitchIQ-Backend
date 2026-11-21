package com.pitchiq.bff.PitchIQ.tracking.dto;

import java.util.Map;

/**
 * Stato del classificatore eventi addestrato sulle etichette dell'utente.
 *
 * @param attivo             true quando almeno due tipi di evento hanno abbastanza esempi per proporre etichette
 * @param accuratezzaStimata quota di esempi classificati correttamente dagli altri (leave-one-out), null se non attivo
 */
public record ModelloEventiDto(int esempi, Map<String, Integer> perClasse, boolean attivo,
                               Double accuratezzaStimata, int minimoPerClasse) {}

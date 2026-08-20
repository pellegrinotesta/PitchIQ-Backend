package com.pitchiq.bff.PitchIQ.squadre.model;

import java.util.Set;

public enum RuoloCategoria {
    PORTIERE(Set.of(RuoloGiocatore.POR)),
    DIFENSORE(Set.of(RuoloGiocatore.DC, RuoloGiocatore.TSD, RuoloGiocatore.TSS, RuoloGiocatore.LB)),
    CENTROCAMPISTA(Set.of(RuoloGiocatore.CDC, RuoloGiocatore.CC, RuoloGiocatore.MOC,
            RuoloGiocatore.ALD, RuoloGiocatore.ALS, RuoloGiocatore.W)),
    ATTACCANTE(Set.of(RuoloGiocatore.PC, RuoloGiocatore.SP, RuoloGiocatore.FW));

    private final Set<RuoloGiocatore> ruoli;

    RuoloCategoria(Set<RuoloGiocatore> ruoli) {
        this.ruoli = ruoli;
    }

    public static RuoloCategoria of(RuoloGiocatore ruolo) {
        for (RuoloCategoria cat : values()) {
            if (cat.ruoli.contains(ruolo)) return cat;
        }
        throw new IllegalArgumentException("Ruolo sconosciuto: " + ruolo);
    }

    public Set<RuoloGiocatore> getRuoli() {
        return ruoli;
    }
}

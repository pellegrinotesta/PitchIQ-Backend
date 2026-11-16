package com.pitchiq.bff.PitchIQ.allenamenti.dto;

/**
 * Carico di allenamento di un giocatore alla data di riferimento (metodo session-RPE:
 * carico di una seduta = RPE × minuti).
 */
public record CaricoGiocatoreDto(
        Long giocatoreId,
        String nomeGiocatore,
        String cognomeGiocatore,
        Integer numeroMaglia,
        String ruolo,
        /** Somma dei carichi degli ultimi 7 giorni */
        int caricoAcuto,
        /** Media settimanale dei carichi degli ultimi 28 giorni */
        double caricoCronico,
        /** Acuto / cronico; null se il cronico è zero */
        Double acwr,
        int seduteConRpe,
        /** Presenze degli ultimi 28 giorni senza RPE: il carico reale è sottostimato */
        int seduteSenzaRpe,
        /** true se mancano dati nelle due settimane più lontane: il rapporto non è affidabile */
        boolean storicoInsufficiente
) {}

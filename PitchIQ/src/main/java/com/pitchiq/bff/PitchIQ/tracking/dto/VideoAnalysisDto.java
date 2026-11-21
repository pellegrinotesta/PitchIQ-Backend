package com.pitchiq.bff.PitchIQ.tracking.dto;

import java.util.List;
import java.util.UUID;

public record VideoAnalysisDto(UUID id, String stato, String messaggio, Double durataSecondi,
                               List<Evento> eventi, Movimento movimento, String nomeFile) {

    /** nomeFile è valorizzato solo nella risposta di avvio: il servizio Python non lo conosce. */
    public VideoAnalysisDto(UUID id, String stato, String messaggio, Double durataSecondi, List<Evento> eventi) {
        this(id, stato, messaggio, durataSecondi, eventi, null, null);
    }

    /**
     * Punteggio campionato ogni 0,5 s e soglia usata per scegliere le clip.
     * metodo: "giocatori" (velocità dei giocatori riconosciuti) o "pixel" (differenza tra fotogrammi).
     */
    public record Movimento(String metodo, Double soglia, List<Campione> campioni) {}

    /** giocatori e palla sono valorizzati solo con il metodo "giocatori". */
    public record Campione(double secondo, double valore, Integer giocatori, Boolean palla) {}

    public record Evento(String id, String etichetta, double inizioSecondi, double fineSecondi,
                         double intensitaMovimento) {}
}

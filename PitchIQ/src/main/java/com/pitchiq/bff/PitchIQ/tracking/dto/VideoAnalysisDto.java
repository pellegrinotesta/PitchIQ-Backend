package com.pitchiq.bff.PitchIQ.tracking.dto;

import java.util.List;
import java.util.Map;
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

    /**
     * caratteristiche, indizi e proposta arrivano da pitchiq-analytics (assenti nelle analisi meno recenti);
     * etichettaUtente è aggiunta dal BFF dalle etichette salvate nel database.
     */
    public record Evento(String id, String etichetta, double inizioSecondi, double fineSecondi,
                         double intensitaMovimento, Map<String, Double> caratteristiche, List<String> indizi,
                         Proposta proposta, String etichettaUtente) {

        public Evento conEtichettaUtente(String valore) {
            return new Evento(id, etichetta, inizioSecondi, fineSecondi, intensitaMovimento,
                    caratteristiche, indizi, proposta, valore);
        }
    }

    public record Proposta(String etichetta, double confidenza) {}

    public VideoAnalysisDto conEventi(List<Evento> nuoviEventi) {
        return new VideoAnalysisDto(id, stato, messaggio, durataSecondi, nuoviEventi, movimento, nomeFile);
    }
}

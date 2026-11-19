package com.pitchiq.bff.PitchIQ.tracking.dto;

import java.util.List;
import java.util.UUID;

public record VideoAnalysisDto(UUID id, String stato, String messaggio, Double durataSecondi,
                               List<Evento> eventi) {
    public record Evento(String id, String etichetta, double inizioSecondi, double fineSecondi,
                         double intensitaMovimento) {}
}

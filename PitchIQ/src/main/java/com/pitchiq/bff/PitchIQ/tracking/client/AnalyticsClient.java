package com.pitchiq.bff.PitchIQ.tracking.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import com.pitchiq.bff.PitchIQ.tracking.dto.ModelloEventiDto;
import com.pitchiq.bff.PitchIQ.tracking.dto.VideoAnalysisDto;

import java.util.List;

/** Chiamata al servizio interno pitchiq-analytics (FastAPI). */
@Component
public class AnalyticsClient {

    private final RestClient restClient;

    public AnalyticsClient(@Qualifier("analyticsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Avvia l'elaborazione. I file sono già in uploadDir/{cartella}. */
    public void avviaElaborazione(long importId, String cartella) {
        restClient.post()
                .uri("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("importId", importId, "cartella", cartella))
                .retrieve()
                .toBodilessEntity();
    }

    public void avviaAnalisiVideo(UUID jobId) {
        restClient.post()
                .uri("/video-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("jobId", jobId))
                .retrieve()
                .toBodilessEntity();
    }

    /** esempi: [{"etichetta": "TIRO", "caratteristiche": {...}}] */
    public ModelloEventiDto addestraModelloEventi(List<Map<String, Object>> esempi) {
        return restClient.post()
                .uri("/event-model")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("esempi", esempi))
                .retrieve()
                .body(ModelloEventiDto.class);
    }

    public ModelloEventiDto statoModelloEventi() {
        return restClient.get()
                .uri("/event-model")
                .retrieve()
                .body(ModelloEventiDto.class);
    }

    public VideoAnalysisDto statoAnalisiVideo(UUID jobId) {
        return restClient.get()
                .uri("/video-jobs/{id}", jobId)
                .retrieve()
                .body(VideoAnalysisDto.class);
    }
}

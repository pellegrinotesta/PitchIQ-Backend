package com.pitchiq.bff.PitchIQ.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

/**
 * pitchiq.tracking.* in application.yml.
 *
 * @param uploadDir      cartella condivisa con il servizio pitchiq-analytics
 * @param analyticsUrl   URL interno del servizio Python
 * @param timeoutImport  dopo quanto un import non concluso viene chiuso in ERRORE
 */
@ConfigurationProperties(prefix = "pitchiq.tracking")
public record TrackingProperties(Path uploadDir, String analyticsUrl, Duration timeoutImport) {

    public TrackingProperties {
        if (uploadDir == null) uploadDir = Path.of("./tracking-data");
        if (analyticsUrl == null) analyticsUrl = "http://localhost:8000";
        if (timeoutImport == null) timeoutImport = Duration.ofMinutes(15);
    }
}

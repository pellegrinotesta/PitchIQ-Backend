package com.pitchiq.bff.PitchIQ.tracking.service;

import com.pitchiq.bff.PitchIQ.tracking.client.AnalyticsClient;
import com.pitchiq.bff.PitchIQ.tracking.dto.VideoAnalysisDto;
import com.pitchiq.bff.PitchIQ.tracking.config.TrackingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingVideoService {

    private static final long MAX_VIDEO_SIZE = 250L * 1024 * 1024;
    private static final String VIDEO_JOBS_DIR = "video-jobs";

    private final AnalyticsClient analyticsClient;
    private final TrackingProperties props;
    private final VideoUrlDownloader downloader;

    public VideoAnalysisDto carica(MultipartFile video) {
        valida(video);
        UUID jobId = UUID.randomUUID();
        Path jobDir = cartella(jobId);
        try {
            Files.createDirectories(jobDir);
            video.transferTo(jobDir.resolve("source"));
        } catch (IOException e) {
            cancella(jobDir);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Non è stato possibile salvare il video sul server.", e);
        }
        return avvia(jobId, jobDir, video.getOriginalFilename());
    }

    public VideoAnalysisDto caricaDaUrl(String url) {
        UUID jobId = UUID.randomUUID();
        Path jobDir = cartella(jobId);
        VideoUrlDownloader.Risultato scaricato;
        try {
            Files.createDirectories(jobDir);
            scaricato = downloader.scarica(url, jobDir.resolve("source"));
        } catch (IOException e) {
            cancella(jobDir);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Non è stato possibile salvare il video sul server.", e);
        } catch (RuntimeException e) {
            cancella(jobDir);
            throw e;
        }
        log.info("Video {} scaricato da link ({} byte)", jobId, scaricato.byteScaricati());
        return avvia(jobId, jobDir, scaricato.nomeFile());
    }

    private VideoAnalysisDto avvia(UUID jobId, Path jobDir, String nomeFile) {
        try {
            analyticsClient.avviaAnalisiVideo(jobId);
            return new VideoAnalysisDto(jobId, "IN_CODA", null, null, List.of(), null, nomeFile);
        } catch (RestClientException e) {
            cancella(jobDir);
            log.error("Avvio analisi video {} fallito", jobId, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Il servizio di analisi video non è raggiungibile.", e);
        }
    }

    public VideoAnalysisDto stato(UUID jobId) {
        try {
            return analyticsClient.statoAnalisiVideo(jobId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analisi video non trovata.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Il servizio di analisi video non è raggiungibile.", e);
        }
    }

    public Resource clip(UUID jobId, String clipId) {
        if (clipId == null || !clipId.matches("clip-[0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip non trovata.");
        }
        Path clipsDir = cartella(jobId).resolve("clips").normalize();
        Path path = clipsDir.resolve(clipId + ".mp4").normalize();
        if (!path.getParent().equals(clipsDir) || !Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip non trovata.");
        }
        return new FileSystemResource(path);
    }

    private Path cartella(UUID jobId) {
        return props.uploadDir().resolve(VIDEO_JOBS_DIR).resolve(jobId.toString())
                .toAbsolutePath().normalize();
    }

    private static void valida(MultipartFile video) {
        if (video == null || video.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleziona un video.");
        }
        String nome = video.getOriginalFilename() == null
                ? "" : video.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!(nome.endsWith(".mp4") || nome.endsWith(".mov") || nome.endsWith(".webm"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato non supportato. Carica un video MP4, MOV o WebM.");
        }
        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Il video supera il limite di 250 MB.");
        }
    }

    private static void cancella(Path dir) {
        try {
            FileSystemUtils.deleteRecursively(dir);
        } catch (IOException e) {
            log.warn("Pulizia dei file dell'analisi video {} fallita", dir, e);
        }
    }
}

package com.pitchiq.bff.PitchIQ.tracking.service;

import com.pitchiq.bff.PitchIQ.tracking.config.TrackingProperties;
import com.pitchiq.bff.PitchIQ.tracking.dto.GhEntry;
import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.dto.PartitaGithubDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GithubTrackingService {

    private final RestClient restClient;
    private final TrackingImportService importService;
    private final TrackingProperties props;
    private final HttpClient httpClient;

    private static final String GH_API = "https://api.github.com/repos/SkillCorner/opendata/contents/data/matches";
    private static final String GH_LFS = "https://github.com/SkillCorner/opendata.git/info/lfs/objects/batch";

    public GithubTrackingService(RestClient.Builder builder,
                                 TrackingImportService importService,
                                 TrackingProperties props) {
        this.restClient = builder
                .defaultHeader("User-Agent", "PitchIQ/1.0")
                .build();
        this.httpClient = HttpClient.newHttpClient();
        this.importService = importService;
        this.props = props;
    }

    public List<PartitaGithubDto> listaPartite() {
        GhEntry[] cartelle = restClient.get()
                .uri(GH_API)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(GhEntry[].class);

        return Arrays.stream(cartelle)
                .filter(e -> "dir".equals(e.type()))
                .map(e -> new PartitaGithubDto(e.name()))
                .toList();
    }

    public ImportDto importa(String matchId) {
        Path dir = props.uploadDir().resolve(matchId).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);

            GhEntry[] files = restClient.get()
                    .uri(GH_API + "/" + matchId)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(GhEntry[].class);

            for (GhEntry file : files) {
                String nome = file.name().toLowerCase();
                if (nome.endsWith("match.json") ||
                        nome.endsWith("dynamic_events.csv") ||
                        nome.endsWith(".jsonl")) {
                    scaricaFile(file, dir, matchId);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel download da GitHub: " + e.getMessage());
        }

        rinominaFile(dir);
        return importService.avviaJobDaCartella(matchId, dir);
    }

    private void scaricaFile(GhEntry entry, Path dir, String matchId) throws IOException {
        String url = entry.download_url();

        if (url == null) {
            url = "https://raw.githubusercontent.com/SkillCorner/opendata/master/data/matches/"
                    + matchId + "/" + entry.name();
        }

        // Scarica tramite HttpClient Java puro per evitare header Spring
        byte[] contenuto = scaricaUrl(url);
        if (contenuto == null) return;

        String testo = new String(contenuto, StandardCharsets.UTF_8);
        if (testo.startsWith("version https://git-lfs.github.com")) {
            log.info("File LFS rilevato: {} — avvio download diretto", entry.name());
            contenuto = scaricaLfs(testo);
        }

        Files.write(dir.resolve(entry.name()), contenuto);
        log.info("File salvato: {} ({} bytes)", entry.name(), contenuto.length);
    }

    private byte[] scaricaUrl(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " per " + url);
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrotto", e);
        }
    }

    @SuppressWarnings("unchecked")
    private byte[] scaricaLfs(String puntatore) throws IOException {
        String oid = puntatore.lines()
                .filter(l -> l.startsWith("oid sha256:"))
                .map(l -> l.substring("oid sha256:".length()).trim())
                .findFirst().orElseThrow();
        long size = puntatore.lines()
                .filter(l -> l.startsWith("size "))
                .map(l -> Long.parseLong(l.substring(5).trim()))
                .findFirst().orElseThrow();

        log.info("Richiesta LFS batch per oid={} size={}", oid, size);

        Map<String, Object> body = Map.of(
                "operation", "download",
                "transfers", List.of("basic"),
                "objects",   List.of(Map.of("oid", oid, "size", size))
        );

        // LFS batch via RestClient — solo verso GitHub, nessun problema
        Map<String, Object> res = restClient.post()
                .uri(GH_LFS)
                .header("Accept", "application/vnd.git-lfs+json")
                .header("Content-Type", "application/vnd.git-lfs+json")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        List<Map<String, Object>> objects =
                (List<Map<String, Object>>) res.get("objects");
        Map<String, Object> actions =
                (Map<String, Object>) objects.get(0).get("actions");
        Map<String, Object> download =
                (Map<String, Object>) actions.get("download");
        String href = (String) download.get("href");

        log.info("Download LFS da CDN: {}", href.substring(0, Math.min(80, href.length())));

        // Download finale via HttpClient Java puro — nessun interceptor Spring, nessun header aggiunto
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(href))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Download LFS fallito: HTTP " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download LFS interrotto", e);
        }
    }

    private void rinominaFile(Path dir) {
        try (var stream = Files.list(dir)) {
            stream.forEach(f -> {
                String nome = f.getFileName().toString().toLowerCase();
                try {
                    if (nome.contains("match") && nome.endsWith(".json")) {
                        Files.move(f, dir.resolve("match.json"),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else if (nome.contains("dynamic_events")) {
                        Files.move(f, dir.resolve("dynamic_events.csv"),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else if (nome.endsWith(".jsonl")) {
                        Files.move(f, dir.resolve("tracking.jsonl"),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    log.warn("Rinomina file fallita: {}", f);
                }
            });
        } catch (IOException e) {
            log.warn("Errore listing dir: {}", dir);
        }
    }
}
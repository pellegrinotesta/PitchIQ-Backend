package com.pitchiq.bff.PitchIQ.tracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Scarica un video da un link pubblico: file diretti (MP4, MOV, WebM) e pagine di Wikimedia Commons.
 * Il server esegue la richiesta per conto dell'utente, quindi ogni hop viene validato contro
 * indirizzi interni (SSRF) e il download è limitato in dimensione e durata.
 */
@Component
public class VideoUrlDownloader {

    static final long MAX_BYTES = 250L * 1024 * 1024;
    private static final int MAX_REDIRECTS = 5;
    private static final Duration DOWNLOAD_DEADLINE = Duration.ofMinutes(5);
    private static final String USER_AGENT = "PitchIQ/1.0 (video analysis)";
    private static final Set<String> ESTENSIONI = Set.of("mp4", "mov", "webm");
    private static final Set<String> HOST_STREAMING = Set.of(
            "youtube.com", "youtu.be", "youtube-nocookie.com", "tiktok.com", "instagram.com",
            "facebook.com", "fb.watch", "dazn.com", "twitch.tv");

    public record Risultato(String nomeFile, long byteScaricati) {}

    private final HttpClient http;
    private final ObjectMapper json;
    private final Predicate<InetAddress> indirizzoConsentito;

    @Autowired // due costruttori: Spring deve sapere quale usare
    public VideoUrlDownloader(ObjectMapper json) {
        this(json, VideoUrlDownloader::pubblico);
    }

    VideoUrlDownloader(ObjectMapper json, Predicate<InetAddress> indirizzoConsentito) {
        this.json = json;
        this.indirizzoConsentito = indirizzoConsentito;
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER) // i redirect si seguono a mano, validando ogni hop
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Risultato scarica(String link, Path destinazione) {
        URI uri = valida(link);
        String nome = nomeDaUri(uri);
        if (isCommonsFilePage(uri)) {
            Sorgente sorgente = risolviCommons(uri);
            uri = valida(sorgente.uri().toString());
            nome = sorgente.nome();
        }
        return new Risultato(nome, download(uri, destinazione));
    }

    // ---------- validazione ----------

    URI valida(String link) {
        if (link == null || link.isBlank()) throw errore(HttpStatus.BAD_REQUEST, "Inserisci il link di un video.");
        URI uri;
        try {
            uri = new URI(link.trim()).normalize();
        } catch (URISyntaxException e) {
            throw errore(HttpStatus.BAD_REQUEST, "Il link non è valido.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
            throw errore(HttpStatus.BAD_REQUEST, "Sono supportati solo link http o https.");
        }
        if (uri.getUserInfo() != null) throw errore(HttpStatus.BAD_REQUEST, "Il link non è valido.");
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (HOST_STREAMING.stream().anyMatch(h -> host.equals(h) || host.endsWith("." + h))) {
            throw errore(HttpStatus.UNPROCESSABLE_ENTITY,
                    "I video di piattaforme come YouTube non si possono scaricare: lo vietano i loro termini "
                            + "e i contenuti sono protetti. Usa un link diretto a un file MP4/WebM o una pagina Wikimedia Commons.");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (!indirizzoConsentito.test(address)) {
                    throw errore(HttpStatus.BAD_REQUEST, "Il link punta a un indirizzo non consentito.");
                }
            }
        } catch (UnknownHostException e) {
            throw errore(HttpStatus.BAD_REQUEST, "Il sito del link non esiste o non è raggiungibile.");
        }
        return uri;
    }

    static boolean pubblico(InetAddress a) {
        if (a.isAnyLocalAddress() || a.isLoopbackAddress() || a.isLinkLocalAddress()
                || a.isSiteLocalAddress() || a.isMulticastAddress()) return false;
        byte[] b = a.getAddress();
        if (a instanceof Inet6Address) {
            return (b[0] & 0xfe) != 0xfc; // fc00::/7 unique local
        }
        int primo = b[0] & 0xff, secondo = b[1] & 0xff;
        return !(primo == 0 || primo == 100 && secondo >= 64 && secondo <= 127 // CGNAT
                || primo >= 240);
    }

    // ---------- Wikimedia Commons ----------

    private record Sorgente(URI uri, String nome) {}

    static boolean isCommonsFilePage(URI uri) {
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        return uri.getHost().toLowerCase(Locale.ROOT).endsWith("commons.wikimedia.org") && path.startsWith("/wiki/File:");
    }

    private Sorgente risolviCommons(URI pagina) {
        String titolo = URLDecoder.decode(pagina.getRawPath().substring("/wiki/".length()), StandardCharsets.UTF_8);
        URI api = URI.create("https://commons.wikimedia.org/w/api.php?action=query&format=json&prop=videoinfo"
                + "&viprop=url%7Cmime%7Csize%7Cderivatives&titles="
                + java.net.URLEncoder.encode(titolo, StandardCharsets.UTF_8));
        JsonNode info;
        try {
            HttpResponse<String> risposta = http.send(richiesta(api).build(), HttpResponse.BodyHandlers.ofString());
            if (risposta.statusCode() != 200) throw errore(HttpStatus.BAD_GATEWAY, "Wikimedia Commons non ha risposto.");
            JsonNode pagine = json.readTree(risposta.body()).path("query").path("pages");
            info = pagine.elements().hasNext() ? pagine.elements().next().path("videoinfo").path(0) : null;
        } catch (IOException e) {
            throw errore(HttpStatus.BAD_GATEWAY, "Wikimedia Commons non ha risposto.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw errore(HttpStatus.SERVICE_UNAVAILABLE, "Download interrotto.");
        }
        if (info == null || info.isMissingNode() || !info.hasNonNull("url")) {
            throw errore(HttpStatus.NOT_FOUND, "File non trovato su Wikimedia Commons.");
        }

        String nomeBase = titolo.substring("File:".length());
        String originale = info.path("url").asText();
        if (ESTENSIONI.contains(estensione(originale))) return new Sorgente(URI.create(originale), nomeBase);

        // formato non supportato (es. .ogv): si usa la conversione WebM generata da Commons, la più vicina a 720p
        String migliore = null;
        int distanza = Integer.MAX_VALUE;
        for (JsonNode d : info.path("derivatives")) {
            String src = d.path("src").asText("");
            if (!src.endsWith(".webm")) continue;
            int h = d.path("height").asInt(0);
            int dist = h <= 0 ? 10_000 : Math.abs(h - 720);
            if (dist < distanza) { distanza = dist; migliore = src; }
        }
        if (migliore == null) {
            throw errore(HttpStatus.UNPROCESSABLE_ENTITY, "Questo file di Commons non ha una versione MP4 o WebM.");
        }
        return new Sorgente(URI.create(migliore), nomeBase.replaceAll("\\.[^.]+$", "") + ".webm");
    }

    // ---------- download ----------

    private long download(URI uri, Path destinazione) {
        Instant scadenza = Instant.now().plus(DOWNLOAD_DEADLINE);
        URI corrente = uri;
        try {
            for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
                HttpResponse<InputStream> risposta = http.send(richiesta(corrente).build(),
                        HttpResponse.BodyHandlers.ofInputStream());
                int status = risposta.statusCode();
                if (status >= 300 && status < 400) {
                    risposta.body().close();
                    String location = risposta.headers().firstValue("Location")
                            .orElseThrow(() -> errore(HttpStatus.BAD_GATEWAY, "Redirect senza destinazione."));
                    corrente = valida(corrente.resolve(location).toString());
                    continue;
                }
                try (InputStream body = risposta.body()) {
                    if (status == 429) {
                        throw errore(HttpStatus.TOO_MANY_REQUESTS,
                                "Il sito sta limitando i download: riprova tra qualche minuto.");
                    }
                    if (status != 200) {
                        throw errore(HttpStatus.BAD_GATEWAY, "Il sito ha risposto con errore " + status + ".");
                    }
                    controllaTipo(risposta, corrente);
                    long dichiarati = risposta.headers().firstValueAsLong("Content-Length").orElse(-1);
                    if (dichiarati > MAX_BYTES) throw errore(HttpStatus.PAYLOAD_TOO_LARGE, "Il video supera il limite di 250 MB.");
                    return copia(body, destinazione, scadenza);
                }
            }
            throw errore(HttpStatus.BAD_GATEWAY, "Troppi redirect.");
        } catch (IOException e) {
            throw errore(HttpStatus.BAD_GATEWAY, "Download del video non riuscito.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw errore(HttpStatus.SERVICE_UNAVAILABLE, "Download interrotto.");
        }
    }

    private static void controllaTipo(HttpResponse<?> risposta, URI uri) {
        String tipo = risposta.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        boolean video = tipo.startsWith("video/mp4") || tipo.startsWith("video/quicktime") || tipo.startsWith("video/webm");
        boolean generico = tipo.isEmpty() || tipo.startsWith("application/octet-stream");
        if (video || generico && ESTENSIONI.contains(estensione(uri.getPath()))) return;
        if (tipo.startsWith("text/html")) {
            throw errore(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Il link apre una pagina web, non un file video. Usa il link diretto al file (di solito finisce con .mp4 o .webm).");
        }
        throw errore(HttpStatus.UNPROCESSABLE_ENTITY, "Formato non supportato. Il link deve puntare a un video MP4, MOV o WebM.");
    }

    private static long copia(InputStream in, Path destinazione, Instant scadenza) throws IOException {
        long totale = 0;
        byte[] buffer = new byte[64 * 1024];
        try (OutputStream out = Files.newOutputStream(destinazione)) {
            int letti;
            while ((letti = in.read(buffer)) != -1) {
                totale += letti;
                if (totale > MAX_BYTES) throw errore(HttpStatus.PAYLOAD_TOO_LARGE, "Il video supera il limite di 250 MB.");
                if (Instant.now().isAfter(scadenza)) throw errore(HttpStatus.GATEWAY_TIMEOUT, "Download troppo lento: riprova o carica il file.");
                out.write(buffer, 0, letti);
            }
        }
        if (totale == 0) throw errore(HttpStatus.BAD_GATEWAY, "Il file scaricato è vuoto.");
        return totale;
    }

    private static HttpRequest.Builder richiesta(URI uri) {
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).header("User-Agent", USER_AGENT).GET();
    }

    // ---------- utilità ----------

    static String estensione(String path) {
        if (path == null) return "";
        String p = path.replaceAll("[?#].*$", "");
        int punto = p.lastIndexOf('.');
        return punto < 0 || punto < p.lastIndexOf('/') ? "" : p.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    static String nomeDaUri(URI uri) {
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String ultimo = path.substring(path.lastIndexOf('/') + 1);
        String nome = URLDecoder.decode(ultimo, StandardCharsets.UTF_8).replaceFirst("^File:", "");
        return nome.isBlank() ? uri.getHost() : nome;
    }

    private static ResponseStatusException errore(HttpStatus status, String messaggio) {
        return new ResponseStatusException(status, messaggio);
    }
}

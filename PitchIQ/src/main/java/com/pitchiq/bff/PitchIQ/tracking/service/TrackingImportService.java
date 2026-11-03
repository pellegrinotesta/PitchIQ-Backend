package com.pitchiq.bff.PitchIQ.tracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.client.AnalyticsClient;
import com.pitchiq.bff.PitchIQ.tracking.config.TrackingProperties;
import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.StatoImport;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingImport;
import com.pitchiq.bff.PitchIQ.tracking.repository.TrackingPartitaRepository;
import com.pitchiq.bff.PitchIQ.tracking.repository.TrackingImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

/**
 * Riceve i file SkillCorner dall'app, li salva nella cartella condivisa e
 * chiede al servizio pitchiq-analytics di elaborarli. Lo stato dell'import
 * viene poi aggiornato dal servizio Python direttamente su tracking_import.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingImportService {

    static final String FILE_PARTITA = "match.json";
    static final String FILE_EVENTI = "dynamic_events.csv";
    static final String FILE_TRACKING = "tracking.jsonl";

    private final TrackingImportRepository importRepository;
    private final TrackingPartitaRepository partitaRepository;
    private final AnalyticsClient analyticsClient;
    private final TrackingProperties props;
    private final ObjectMapper objectMapper;

    public ImportDto carica(MultipartFile partita, MultipartFile eventi, MultipartFile tracking) {
        valida(partita, ".json", "della partita");
        valida(eventi, ".csv", "degli eventi");
        boolean conTracking = tracking != null && !tracking.isEmpty();
        if (conTracking) valida(tracking, ".jsonl", "di tracking");
        long matchId = matchId(partita);
        validaPrefisso(matchId, eventi, "degli eventi");
        if (conTracking) validaPrefisso(matchId, tracking, "di tracking");
        rifiutaDuplicato(matchId);

        TrackingImport imp = new TrackingImport();
        imp.setStato(StatoImport.IN_CODA);
        imp.setConTracking(conTracking);
        imp.setNomeFileEventi(nomeSicuro(eventi.getOriginalFilename()));
        imp = importRepository.saveAndFlush(imp);

        String cartella = String.valueOf(imp.getId());
        Path dir = props.uploadDir().resolve(cartella).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            partita.transferTo(dir.resolve(FILE_PARTITA));
            eventi.transferTo(dir.resolve(FILE_EVENTI));
            if (conTracking) tracking.transferTo(dir.resolve(FILE_TRACKING));
        } catch (IOException e) {
            log.error("Salvataggio file import {} fallito", imp.getId(), e);
            cancellaCartella(dir);
            return chiudiInErrore(imp, "Non è stato possibile salvare i file sul server.");
        }

        try {
            analyticsClient.avviaElaborazione(imp.getId(), cartella);
        } catch (RestClientException e) {
            log.error("Servizio pitchiq-analytics non raggiungibile per import {}", imp.getId(), e);
            return chiudiInErrore(imp, "Il servizio di analisi non risponde. Controlla che il container pitchiq-analytics sia avviato.");
        }
        return rileggi(imp);
    }

    public ImportDto stato(Long id) {
        chiudiScaduti();
        return importRepository.leggi(id).map(this::conRetry)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import " + id + " non trovato"));
    }

    public List<ImportDto> recenti(String query) {
        chiudiScaduti();
        return importRepository.cercaTutti(query == null ? "" : query.trim()).stream()
            .map(ImportDto::from).map(this::conRetry).toList();
    }

    public ImportDto ritenta(Long id) {
        TrackingImport imp = importRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import " + id + " non trovato"));
        if (imp.getStato() != StatoImport.ERRORE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Si possono ritentare solo gli import in errore.");
        }

        Path dir = cartellaImport(id);
        if (!Files.isRegularFile(dir.resolve(FILE_PARTITA)) || !Files.isRegularFile(dir.resolve(FILE_EVENTI))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "I file originali non sono disponibili per questo import. Carica nuovamente la partita.");
        }

        imp.setStato(StatoImport.IN_CODA);
        imp.setMessaggio(null);
        imp = importRepository.saveAndFlush(imp);
        try {
            analyticsClient.avviaElaborazione(id, String.valueOf(id));
        } catch (RestClientException e) {
            log.error("Servizio pitchiq-analytics non raggiungibile per retry import {}", id, e);
            return chiudiInErrore(imp, "Il servizio di analisi non risponde. Riprova quando il container è disponibile.");
        }
        return rileggi(imp);
    }

    /** Un import che non si conclude entro il timeout (es. container riavviato) non deve restare appeso. */
    private void chiudiScaduti() {
        importRepository.chiudiScaduti(Math.max(1, props.timeoutImport().toMinutes()),
                "Elaborazione interrotta: nessun aggiornamento dal servizio di analisi. Riprova il caricamento.");
    }

    private ImportDto chiudiInErrore(TrackingImport imp, String messaggio) {
        imp.setStato(StatoImport.ERRORE);
        imp.setMessaggio(messaggio);
        imp = importRepository.saveAndFlush(imp);
        return rileggi(imp);
    }

    /**
     * Riletto dal DB come DTO: prende gli orari valorizzati dal database e lo stato
     * eventualmente già cambiato dal servizio Python (con open-in-view un findById
     * restituirebbe l'istanza già in memoria).
     */
    private ImportDto rileggi(TrackingImport imp) {
        return conRetry(importRepository.leggi(imp.getId()).orElseThrow());
    }

    private ImportDto conRetry(ImportDto dto) {
        Path dir = cartellaImport(dto.id());
        boolean ritentabile = dto.stato() == StatoImport.ERRORE
                && Files.isRegularFile(dir.resolve(FILE_PARTITA))
                && Files.isRegularFile(dir.resolve(FILE_EVENTI));
        return new ImportDto(dto.id(), dto.stato(), dto.messaggio(), dto.matchId(), dto.conTracking(),
                dto.nomeFileEventi(), dto.creatoIl(), dto.aggiornatoIl(), ritentabile);
    }

    private static void valida(MultipartFile f, String estensione, String etichetta) {
        if (f == null || f.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manca il file " + etichetta + ".");
        }
        String nome = f.getOriginalFilename() == null ? "" : f.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!nome.endsWith(estensione)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il file " + etichetta + " deve essere " + estensione + " (ricevuto: " + f.getOriginalFilename() + ").");
        }
    }

    private long matchId(MultipartFile file) {
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());
            JsonNode id = root == null ? null : root.get("id");
            if (id == null || !id.canConvertToLong() || id.longValue() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Il file della partita non contiene un id SkillCorner valido.");
            }
            return id.longValue();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il file della partita non è un JSON valido.", e);
        }
    }

    private void rifiutaDuplicato(long matchId) {
        if (partitaRepository.existsById(matchId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La partita " + matchId + " è già presente. Eliminala prima di importarla di nuovo.");
        }
    }

    private static void validaPrefisso(long matchId, MultipartFile file, String etichetta) {
        String nome = file.getOriginalFilename();
        if (nome == null) return;
        String percorso = nome.replace('\\', '/');
        String base = percorso.substring(percorso.lastIndexOf('/') + 1);
        int separatore = base.indexOf('_');
        if (separatore <= 0) return;
        String prefisso = base.substring(0, separatore);
        if (prefisso.matches("\\d+") && !prefisso.equals(String.valueOf(matchId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il file " + etichetta + " sembra appartenere alla partita " + prefisso
                            + ", ma il file partita ha ID " + matchId + ".");
        }
    }

    private static String nomeSicuro(String nome) {
        if (nome == null) return "eventi.csv";
        String base = Path.of(nome).getFileName().toString();
        return base.length() > 255 ? base.substring(0, 255) : base;
    }

    private static void cancellaCartella(Path dir) {
        try {
            FileSystemUtils.deleteRecursively(dir);
        } catch (IOException ignored) {
            // pulizia best effort
        }
    }

    public ImportDto avviaJobDaCartella(String matchId, Path dir) {
        long id;
        try {
            id = Long.parseLong(matchId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id partita SkillCorner non valido.");
        }
        rifiutaDuplicato(id);
        boolean conTracking = Files.exists(dir.resolve(FILE_TRACKING));

        TrackingImport imp = new TrackingImport();
        imp.setStato(StatoImport.IN_CODA);
        imp.setConTracking(conTracking);
        imp.setNomeFileEventi(matchId + "_dynamic_events.csv");
        imp = importRepository.saveAndFlush(imp);

        Path jobDir = cartellaImport(imp.getId());
        try {
            Files.createDirectories(jobDir);
            Files.copy(dir.resolve(FILE_PARTITA), jobDir.resolve(FILE_PARTITA), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(dir.resolve(FILE_EVENTI), jobDir.resolve(FILE_EVENTI), StandardCopyOption.REPLACE_EXISTING);
            if (conTracking) {
                Files.copy(dir.resolve(FILE_TRACKING), jobDir.resolve(FILE_TRACKING), StandardCopyOption.REPLACE_EXISTING);
            }
            if (!dir.toAbsolutePath().normalize().equals(jobDir)) cancellaCartella(dir);
            analyticsClient.avviaElaborazione(imp.getId(), String.valueOf(imp.getId()));
        } catch (RestClientException e) {
            return chiudiInErrore(imp, "Il servizio di analisi non risponde.");
        } catch (IOException e) {
            cancellaCartella(jobDir);
            return chiudiInErrore(imp, "Non è stato possibile preparare i file scaricati da GitHub.");
        }
        return rileggi(imp);
    }

    private Path cartellaImport(Long id) {
        return props.uploadDir().resolve(String.valueOf(id)).toAbsolutePath().normalize();
    }
}

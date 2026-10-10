package com.pitchiq.bff.PitchIQ.tracking.service;

import com.pitchiq.bff.PitchIQ.tracking.client.AnalyticsClient;
import com.pitchiq.bff.PitchIQ.tracking.config.TrackingProperties;
import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.StatoImport;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingImport;
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
    private final AnalyticsClient analyticsClient;
    private final TrackingProperties props;

    public ImportDto carica(MultipartFile partita, MultipartFile eventi, MultipartFile tracking) {
        valida(partita, ".json", "della partita");
        valida(eventi, ".csv", "degli eventi");
        boolean conTracking = tracking != null && !tracking.isEmpty();
        if (conTracking) valida(tracking, ".jsonl", "di tracking");

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
            cancellaCartella(dir);
            return chiudiInErrore(imp, "Il servizio di analisi non risponde. Controlla che il container pitchiq-analytics sia avviato.");
        }
        return rileggi(imp);
    }

    public ImportDto stato(Long id) {
        chiudiScaduti();
        return importRepository.leggi(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import " + id + " non trovato"));
    }

    public List<ImportDto> recenti() {
        chiudiScaduti();
        return importRepository.findTop10ByOrderByCreatoIlDesc().stream().map(ImportDto::from).toList();
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
        return importRepository.leggi(imp.getId()).orElseThrow();
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
}

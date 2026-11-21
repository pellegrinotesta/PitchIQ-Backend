package com.pitchiq.bff.PitchIQ.tracking.controller;

import com.pitchiq.bff.PitchIQ.tracking.dto.*;
import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingImportService;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingQueryService;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Analisi pressione da dati SkillCorner")
public class TrackingController {

    private final TrackingImportService importService;
    private final TrackingQueryService queryService;
    private final TrackingVideoService videoService;
    private final com.pitchiq.bff.PitchIQ.tracking.service.EtichetteClipService etichetteService;

    @Operation(summary = "Carica un video per l'estrazione sperimentale di clip candidate")
    @PostMapping(path = "/video-analyses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoAnalysisDto> caricaVideo(@RequestPart("video") MultipartFile video) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(videoService.carica(video));
    }

    public record VideoUrlRequest(@jakarta.validation.constraints.NotBlank
                                  @jakarta.validation.constraints.Size(max = 2048) String url) {}

    @Operation(summary = "Scarica un video da un link pubblico (file diretto o Wikimedia Commons) e lo analizza")
    @PostMapping(path = "/video-analyses/da-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoAnalysisDto> caricaVideoDaUrl(
            @jakarta.validation.Valid @RequestBody VideoUrlRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(videoService.caricaDaUrl(request.url()));
    }

    @Operation(summary = "Stato e clip candidate di un'analisi video")
    @GetMapping("/video-analyses/{id}")
    public VideoAnalysisDto statoVideo(@PathVariable UUID id) {
        return etichetteService.conEtichette(videoService.stato(id));
    }

    public record EtichettaRequest(@jakarta.validation.constraints.NotBlank String etichetta) {}

    @Operation(summary = "Etichetta una clip (esempio per il classificatore eventi) e riaddestra il modello")
    @PutMapping("/video-analyses/{id}/clips/{clipId}/etichetta")
    public ModelloEventiDto etichettaClip(@PathVariable UUID id, @PathVariable String clipId,
                                          @jakarta.validation.Valid @RequestBody EtichettaRequest request) {
        return etichetteService.etichetta(id, clipId, request.etichetta());
    }

    @Operation(summary = "Rimuove l'etichetta di una clip e riaddestra il modello")
    @DeleteMapping("/video-analyses/{id}/clips/{clipId}/etichetta")
    public ModelloEventiDto rimuoviEtichettaClip(@PathVariable UUID id, @PathVariable String clipId) {
        return etichetteService.rimuovi(id, clipId);
    }

    @Operation(summary = "Stato del classificatore eventi (esempi per tipo, accuratezza stimata)")
    @GetMapping("/modello-eventi")
    public ModelloEventiDto modelloEventi() {
        return etichetteService.stato();
    }

    @GetMapping("/video-analyses/{id}/clips/{clipId}")
    public ResponseEntity<Resource> clipVideo(@PathVariable UUID id, @PathVariable String clipId) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoService.clip(id, clipId));
    }

    @Operation(summary = "Carica i file SkillCorner di una partita e avvia l'elaborazione")
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportDto> carica(@RequestPart("partita") MultipartFile partita,
                                            @RequestPart("eventi") MultipartFile eventi,
                                            @RequestPart(value = "tracking", required = false) MultipartFile tracking) {
        ImportDto dto = importService.carica(partita, eventi, tracking);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    @Operation(summary = "Stato di un caricamento")
    @GetMapping("/import/{id}")
    public ImportDto stato(@PathVariable Long id) {
        return importService.stato(id);
    }

    @Operation(summary = "Ultimi 10 caricamenti")
    @GetMapping("/import")
    public List<ImportDto> recenti(@RequestParam(defaultValue = "") String q) {
        return importService.recenti(q);
    }

    @PostMapping("/import/{id}/retry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ImportDto ritenta(@PathVariable Long id) {
        return importService.ritenta(id);
    }

    @GetMapping("/partite")
    public List<PartitaDto> partite() {
        return queryService.partite();
    }

    @GetMapping("/partite/{matchId}")
    public PartitaDettaglioDto dettaglio(@PathVariable Long matchId) {
        return queryService.dettaglio(matchId);
    }

    @DeleteMapping("/partite/{matchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long matchId) {
        queryService.elimina(matchId);
    }

    @GetMapping("/partite/{matchId}/sequenze")
    public List<SequenzaDto> sequenze(@PathVariable Long matchId,
                                      @RequestParam(required = false) EsitoPressione esito,
                                      @RequestParam(required = false) Long teamId,
                                      @RequestParam(defaultValue = "false") boolean soloAnimate) {
        return queryService.sequenze(matchId, esito, teamId, soloAnimate);
    }

    @Operation(summary = "Valori medi per esito su tutte le partite caricate")
    @GetMapping("/aggregati")
    public List<AggregatoEsitoDto> aggregati() {
        return queryService.aggregati(null);
    }

    @GetMapping("/sequenze/{id}/animazione")
    public AnimazioneDto animazione(@PathVariable Long id) {
        return queryService.animazione(id);
    }
}

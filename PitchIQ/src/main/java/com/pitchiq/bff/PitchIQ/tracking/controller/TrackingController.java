package com.pitchiq.bff.PitchIQ.tracking.controller;

import com.pitchiq.bff.PitchIQ.tracking.dto.*;
import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingImportService;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Analisi pressione da dati SkillCorner")
public class TrackingController {

    private final TrackingImportService importService;
    private final TrackingQueryService queryService;

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
    public List<ImportDto> recenti() {
        return importService.recenti();
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

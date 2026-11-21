package com.pitchiq.bff.PitchIQ.tracking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.client.AnalyticsClient;
import com.pitchiq.bff.PitchIQ.tracking.dto.ModelloEventiDto;
import com.pitchiq.bff.PitchIQ.tracking.dto.VideoAnalysisDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.EtichettaClip;
import com.pitchiq.bff.PitchIQ.tracking.repository.EtichettaClipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Etichette dell'utente sulle clip e addestramento del classificatore eventi.
 * Il database è la fonte di verità: pitchiq-analytics tiene solo una copia del modello,
 * che il BFF ricostruisce quando non è allineata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtichetteClipService {

    public static final Set<String> ETICHETTE = Set.of(
            "TIRO", "PASSAGGIO_LUNGO", "CONTRASTO", "RIPARTENZA", "CALCIO_PIAZZATO", "NIENTE");

    private final EtichettaClipRepository repository;
    private final AnalyticsClient analyticsClient;
    private final TrackingVideoService videoService;
    private final ObjectMapper json;

    /** Se il riaddestramento fallisce l'etichetta resta salvata: il modello si riallinea alla prossima lettura. */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public ModelloEventiDto etichetta(UUID analisiId, String clipId, String etichetta) {
        if (etichetta == null || !ETICHETTE.contains(etichetta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Etichetta non valida.");
        }
        VideoAnalysisDto.Evento evento = videoService.stato(analisiId).eventi().stream()
                .filter(e -> e.id().equals(clipId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip non trovata."));
        if (evento.caratteristiche() == null || evento.caratteristiche().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Questa analisi è precedente al classificatore: rianalizza il video per poter etichettare le clip.");
        }

        EtichettaClip riga = repository.findByAnalisiIdAndClipId(analisiId.toString(), clipId)
                .orElseGet(EtichettaClip::new);
        riga.setAnalisiId(analisiId.toString());
        riga.setClipId(clipId);
        riga.setEtichetta(etichetta);
        riga.setCaratteristicheJson(scrivi(evento.caratteristiche()));
        riga.setInizioSecondi(evento.inizioSecondi());
        riga.setFineSecondi(evento.fineSecondi());
        repository.saveAndFlush(riga);
        return riaddestra();
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public ModelloEventiDto rimuovi(UUID analisiId, String clipId) {
        repository.findByAnalisiIdAndClipId(analisiId.toString(), clipId).ifPresent(repository::delete);
        repository.flush();
        return riaddestra();
    }

    /** Stato del modello; se il servizio Python ha perso il modello (es. container ricreato) lo ricostruisce. */
    public ModelloEventiDto stato() {
        try {
            ModelloEventiDto stato = analyticsClient.statoModelloEventi();
            if (stato != null && stato.esempi() == repository.count()) return stato;
            return riaddestra();
        } catch (RestClientException e) {
            throw nonRaggiungibile(e);
        }
    }

    /** Aggiunge alle clip l'etichetta già data dall'utente. */
    public VideoAnalysisDto conEtichette(VideoAnalysisDto analisi) {
        if (analisi.eventi() == null || analisi.eventi().isEmpty()) return analisi;
        Map<String, String> perClip = repository.findByAnalisiId(analisi.id().toString()).stream()
                .collect(Collectors.toMap(EtichettaClip::getClipId, EtichettaClip::getEtichetta));
        if (perClip.isEmpty()) return analisi;
        return analisi.conEventi(analisi.eventi().stream()
                .map(e -> e.conEtichettaUtente(perClip.get(e.id())))
                .toList());
    }

    private ModelloEventiDto riaddestra() {
        List<Map<String, Object>> esempi = repository.findAll().stream()
                .map(r -> Map.<String, Object>of("etichetta", r.getEtichetta(), "caratteristiche", leggi(r)))
                .toList();
        try {
            return analyticsClient.addestraModelloEventi(esempi);
        } catch (RestClientException e) {
            throw nonRaggiungibile(e);
        }
    }

    private ResponseStatusException nonRaggiungibile(RestClientException e) {
        log.warn("Classificatore eventi non aggiornato: servizio di analisi non raggiungibile", e);
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Etichetta salvata, ma il modello non è stato aggiornato: il servizio di analisi non è raggiungibile.", e);
    }

    private String scrivi(Map<String, Double> caratteristiche) {
        try {
            return json.writeValueAsString(caratteristiche);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Double> leggi(EtichettaClip riga) {
        try {
            return json.readValue(riga.getCaratteristicheJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Caratteristiche non leggibili per l'etichetta " + riga.getId(), e);
        }
    }
}

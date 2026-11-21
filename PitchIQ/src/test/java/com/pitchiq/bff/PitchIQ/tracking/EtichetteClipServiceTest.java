package com.pitchiq.bff.PitchIQ.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.client.AnalyticsClient;
import com.pitchiq.bff.PitchIQ.tracking.dto.ModelloEventiDto;
import com.pitchiq.bff.PitchIQ.tracking.dto.VideoAnalysisDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.EtichettaClip;
import com.pitchiq.bff.PitchIQ.tracking.repository.EtichettaClipRepository;
import com.pitchiq.bff.PitchIQ.tracking.service.EtichetteClipService;
import com.pitchiq.bff.PitchIQ.tracking.service.TrackingVideoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class EtichetteClipServiceTest {

    private static final UUID ANALISI = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final ModelloEventiDto MODELLO = new ModelloEventiDto(1, Map.of("TIRO", 1), false, null, 3);

    private final EtichettaClipRepository repository = mock(EtichettaClipRepository.class);
    private final AnalyticsClient analytics = mock(AnalyticsClient.class);
    private final TrackingVideoService video = mock(TrackingVideoService.class);
    private final EtichetteClipService service =
            new EtichetteClipService(repository, analytics, video, new ObjectMapper());

    private static VideoAnalysisDto analisi(Map<String, Double> caratteristiche) {
        var evento = new VideoAnalysisDto.Evento("clip-001", "Azione", 2.0, 6.0, 1.0,
                caratteristiche, List.of(), null, null);
        return new VideoAnalysisDto(ANALISI, "COMPLETATO", null, 17.0, List.of(evento));
    }

    @Test
    void salvaLEtichettaConLeCaratteristicheERiaddestra() {
        when(video.stato(ANALISI)).thenReturn(analisi(Map.of("velocita_max_palla", 18.5)));
        when(repository.findByAnalisiIdAndClipId(ANALISI.toString(), "clip-001")).thenReturn(Optional.empty());
        when(analytics.addestraModelloEventi(anyList())).thenReturn(MODELLO);

        var risultato = service.etichetta(ANALISI, "clip-001", "TIRO");

        var salvata = ArgumentCaptor.forClass(EtichettaClip.class);
        verify(repository).saveAndFlush(salvata.capture());
        assertThat(salvata.getValue().getEtichetta()).isEqualTo("TIRO");
        assertThat(salvata.getValue().getCaratteristicheJson()).contains("velocita_max_palla");
        assertThat(salvata.getValue().getInizioSecondi()).isEqualTo(2.0);
        assertThat(risultato).isEqualTo(MODELLO);
    }

    @Test
    void rifiutaEtichetteSconosciute() {
        assertThatThrownBy(() -> service.etichetta(ANALISI, "clip-001", "GOL_IN_ROVESCIATA"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("non valida");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void chiedeDiRianalizzareLeAnalisiSenzaCaratteristiche() {
        when(video.stato(ANALISI)).thenReturn(analisi(null));

        assertThatThrownBy(() -> service.etichetta(ANALISI, "clip-001", "TIRO"))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("rianalizza");
    }

    @Test
    void seIlServizioPythonEGiuLEtichettaVieneComunqueSalvata() {
        when(video.stato(ANALISI)).thenReturn(analisi(Map.of("velocita_max_palla", 3.0)));
        when(repository.findByAnalisiIdAndClipId(any(), any())).thenReturn(Optional.empty());
        when(analytics.addestraModelloEventi(anyList())).thenThrow(new ResourceAccessException("down"));

        assertThatThrownBy(() -> service.etichetta(ANALISI, "clip-001", "CONTRASTO"))
                .hasMessageContaining("Etichetta salvata");
        verify(repository).saveAndFlush(any());
    }

    @Test
    void aggiungeAlleClipLEtichettaGiaData() {
        var riga = new EtichettaClip();
        riga.setClipId("clip-001");
        riga.setEtichetta("RIPARTENZA");
        when(repository.findByAnalisiId(ANALISI.toString())).thenReturn(List.of(riga));

        var conEtichette = service.conEtichette(analisi(Map.of("x", 1.0)));

        assertThat(conEtichette.eventi().getFirst().etichettaUtente()).isEqualTo("RIPARTENZA");
    }

    @Test
    void ricostruisceIlModelloSeIlServizioNonEAllineato() {
        when(analytics.statoModelloEventi()).thenReturn(new ModelloEventiDto(0, Map.of(), false, null, 3));
        when(repository.count()).thenReturn(1L);
        var riga = new EtichettaClip();
        riga.setEtichetta("TIRO");
        riga.setCaratteristicheJson("{\"velocita_max_palla\": 20.0}");
        when(repository.findAll()).thenReturn(List.of(riga));
        when(analytics.addestraModelloEventi(anyList())).thenReturn(MODELLO);

        assertThat(service.stato()).isEqualTo(MODELLO);
        verify(analytics).addestraModelloEventi(anyList());
    }
}

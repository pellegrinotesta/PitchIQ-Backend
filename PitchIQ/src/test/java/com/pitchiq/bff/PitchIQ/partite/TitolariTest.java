package com.pitchiq.bff.PitchIQ.partite;

import com.pitchiq.bff.PitchIQ.common.exception.GlobalExceptionHandler;
import com.pitchiq.bff.PitchIQ.partite.controller.PartitaController;
import com.pitchiq.bff.PitchIQ.partite.model.Partita;
import com.pitchiq.bff.PitchIQ.partite.model.StatistichePartita;
import com.pitchiq.bff.PitchIQ.partite.repository.HeatmapRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.ScoutingRepository;
import com.pitchiq.bff.PitchIQ.partite.service.PartitaService;
import com.pitchiq.bff.PitchIQ.partite.service.XgCalcolatoreService;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.squadre.repository.TitolareProjection;
import com.pitchiq.bff.PitchIQ.tracking.service.StatsBombService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TitolariTest {
    private final PartitaRepository partite = mock(PartitaRepository.class);
    private final GiocatoreRepository giocatori = mock(GiocatoreRepository.class);
    private final XgCalcolatoreService xg = mock(XgCalcolatoreService.class);
    private final PartitaService service = new PartitaService(partite, mock(HeatmapRepository.class),
            mock(ScoutingRepository.class), giocatori, xg, mock(StatsBombService.class));
    private final org.springframework.test.web.servlet.MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new PartitaController(service, xg))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void restituisceSoloDatiPubbliciDeiTitolari() throws Exception {
        TitolareProjection giocatore = mock(TitolareProjection.class);
        when(giocatore.getId()).thenReturn(7L);
        when(giocatore.getNome()).thenReturn("Raffaele");
        when(giocatore.getCognome()).thenReturn("Russo");
        when(giocatore.getPosizione()).thenReturn("CF-L");
        when(giocatore.getNumeroMaglia()).thenReturn(10);
        when(giocatore.getModuloFonte()).thenReturn("3-5-2");
        when(partite.existsById(1L)).thenReturn(true);
        when(giocatori.findTitolariByPartitaId(1L)).thenReturn(List.of(giocatore));

        mvc.perform(get("/api/partite/1/titolari"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":7,\"nome\":\"Raffaele\",\"cognome\":\"Russo\",\"posizione\":\"CF-L\",\"numeroMaglia\":10,\"moduloFonte\":\"3-5-2\"}]", true));
    }

    @Test
    void distinguePartitaSenzaTitolariDaPartitaInesistente() throws Exception {
        when(partite.existsById(1L)).thenReturn(true);
        when(giocatori.findTitolariByPartitaId(1L)).thenReturn(List.of());
        mvc.perform(get("/api/partite/1/titolari"))
                .andExpect(status().isOk()).andExpect(content().json("[]", true));
        mvc.perform(get("/api/partite/99/titolari")).andExpect(status().isNotFound());
        verify(giocatori, never()).findTitolariByPartitaId(99L);
    }

    @Test
    void salvaPossessoDecimaleEDatiParzialiSenzaAttribuirliAllaFontePrecedente() throws Exception {
        Partita partita = new Partita();
        partita.setId(1L);
        StatistichePartita statistiche = new StatistichePartita();
        statistiche.setPartita(partita);
        statistiche.setFonte("ESPN");
        partita.setStatistiche(statistiche);
        when(partite.findById(1L)).thenReturn(Optional.of(partita));
        when(partite.findByIdWithEventi(1L)).thenReturn(Optional.of(partita));
        when(giocatori.findAllById(any())).thenReturn(List.of());

        mvc.perform(post("/api/partite/1/statistiche")
                        .contentType("application/json")
                        .content("{\"possessoPct\":37.3,\"tiriTotali\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistiche.possessoPct").value(37.3))
                .andExpect(jsonPath("$.statistiche.tiriTotali").value(12));
        assertThat(statistiche.getPossessoPct()).isEqualByComparingTo(new BigDecimal("37.3"));
        assertThat(statistiche.getDuelliVinti()).isNull();
        assertThat(statistiche.getFonte()).isNull();
    }
}

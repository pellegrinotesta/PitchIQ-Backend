package com.pitchiq.bff.PitchIQ.partite;

import com.pitchiq.bff.PitchIQ.common.exception.GlobalExceptionHandler;
import com.pitchiq.bff.PitchIQ.partite.controller.PartitaController;
import com.pitchiq.bff.PitchIQ.partite.repository.HeatmapRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import com.pitchiq.bff.PitchIQ.partite.repository.ScoutingRepository;
import com.pitchiq.bff.PitchIQ.partite.service.PartitaService;
import com.pitchiq.bff.PitchIQ.partite.service.XgCalcolatoreService;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tracking.service.StatsBombService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        Giocatore giocatore = new Giocatore();
        giocatore.setId(7L);
        giocatore.setNome("Raffaele");
        giocatore.setCognome("Russo");
        when(partite.existsById(1L)).thenReturn(true);
        when(giocatori.findTitolariByPartitaId(1L)).thenReturn(List.of(giocatore));

        mvc.perform(get("/api/partite/1/titolari"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":7,\"nome\":\"Raffaele\",\"cognome\":\"Russo\"}]", true));
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
}

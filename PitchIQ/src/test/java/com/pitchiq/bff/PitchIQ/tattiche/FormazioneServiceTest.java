package com.pitchiq.bff.PitchIQ.tattiche;

import com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.dto.FormazioneRequest;
import com.pitchiq.bff.PitchIQ.tattiche.dto.PosizioneCampoRequest;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseFormazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseFormazione.FrecciaFase;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseFormazione.PosizioneFase;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseGioco;
import com.pitchiq.bff.PitchIQ.tattiche.model.Formazione;
import com.pitchiq.bff.PitchIQ.tattiche.repository.FormazioneRepository;
import com.pitchiq.bff.PitchIQ.tattiche.service.FormazioneService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FormazioneServiceTest {

    private final FormazioneRepository formazioni = mock(FormazioneRepository.class);
    private final GiocatoreRepository giocatori = mock(GiocatoreRepository.class);
    private final PartitaRepository partite = mock(PartitaRepository.class);
    private final FormazioneService service = new FormazioneService(formazioni, giocatori, partite);

    private final Formazione formazione = new Formazione();

    private FormazioneRequest richiesta(Long partitaId) {
        return new FormazioneRequest("vs Pisa", "3-5-2", null, null, partitaId, List.of(), null);
    }

    @Test
    void collegaLaFormazioneAllaPartita() {
        formazione.setId(1L);
        when(formazioni.findById(1L)).thenReturn(Optional.of(formazione));
        when(formazioni.findByIdWithPosizioni(1L)).thenReturn(Optional.of(formazione));
        when(formazioni.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(partite.existsById(17L)).thenReturn(true);

        var dto = service.update(1L, richiesta(17L));

        assertThat(formazione.getPartitaId()).isEqualTo(17L);
        assertThat(dto.partitaId()).isEqualTo(17L);
    }

    @Test
    void rifiutaUnaPartitaInesistente() {
        formazione.setId(1L);
        when(formazioni.findById(1L)).thenReturn(Optional.of(formazione));
        when(partite.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.update(1L, richiesta(999L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(formazioni, never()).saveAndFlush(any());
    }

    @Test
    void laPartitaEFacoltativa() {
        formazione.setId(1L);
        formazione.setPartitaId(17L);
        when(formazioni.findById(1L)).thenReturn(Optional.of(formazione));
        when(formazioni.findByIdWithPosizioni(1L)).thenReturn(Optional.of(formazione));

        service.update(1L, richiesta(null));

        assertThat(formazione.getPartitaId()).isNull();
        verify(partite, never()).existsById(any());
    }

    @Test
    void leFasiTengonoSoloITitolariESaltanoQuelleVuote() {
        formazione.setId(1L);
        when(formazioni.findById(1L)).thenReturn(Optional.of(formazione));
        when(formazioni.findByIdWithPosizioni(1L)).thenReturn(Optional.of(formazione));
        var titolare = new PosizioneCampoRequest(53L, 50.0, 75.0, "CB", true);
        var riserva = new PosizioneCampoRequest(76L, 0.0, 0.0, null, false);
        var possesso = new FaseFormazione(
                List.of(new PosizioneFase(53L, 40, 60), new PosizioneFase(76L, 10, 10), new PosizioneFase(99L, 5, 5)),
                List.of(new FrecciaFase(40, 60, 40, 40, "CORSA")));
        var vuota = new FaseFormazione(List.of(new PosizioneFase(76L, 1, 1)), List.of());
        var req = new FormazioneRequest("Base", "4-3-3", null, null, null, List.of(titolare, riserva),
                Map.of(FaseGioco.POSSESSO, possesso, FaseGioco.NON_POSSESSO, vuota));

        var dto = service.update(1L, req);

        assertThat(formazione.getFasi()).containsOnlyKeys(FaseGioco.POSSESSO);
        assertThat(formazione.getFasi().get(FaseGioco.POSSESSO).posizioni())
                .extracting(PosizioneFase::giocatoreId).containsExactly(53L);
        assertThat(formazione.getFasi().get(FaseGioco.POSSESSO).frecce()).hasSize(1);
        assertThat(dto.fasi()).containsOnlyKeys(FaseGioco.POSSESSO);
    }
}

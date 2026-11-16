package com.pitchiq.bff.PitchIQ.allenamenti;

import com.pitchiq.bff.PitchIQ.allenamenti.dto.CaricoGiocatoreDto;
import com.pitchiq.bff.PitchIQ.allenamenti.model.Presenza;
import com.pitchiq.bff.PitchIQ.allenamenti.model.SedutaAllenamento;
import com.pitchiq.bff.PitchIQ.allenamenti.repository.PresenzaRepository;
import com.pitchiq.bff.PitchIQ.allenamenti.service.CaricoLavoroService;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaricoLavoroServiceTest {
    private static final LocalDate OGGI = LocalDate.of(2026, 11, 16);

    private final PresenzaRepository presenze = mock(PresenzaRepository.class);
    private final GiocatoreRepository giocatori = mock(GiocatoreRepository.class);
    private final CaricoLavoroService service = new CaricoLavoroService(presenze, giocatori);

    private Presenza presenza(long giocatoreId, int giorniFa, Integer rpe, Integer minuti) {
        SedutaAllenamento seduta = new SedutaAllenamento();
        seduta.setData(OGGI.minusDays(giorniFa));
        seduta.setOraInizio(LocalTime.of(10, 0));
        seduta.setOraFine(LocalTime.of(11, 30));
        Presenza p = new Presenza();
        p.setSeduta(seduta);
        p.setGiocatoreId(giocatoreId);
        p.setRpe(rpe);
        p.setMinuti(minuti);
        return p;
    }

    private CaricoGiocatoreDto caricoDi(Presenza... dati) {
        Giocatore g = new Giocatore();
        g.setId(1L); g.setNome("Marco"); g.setCognome("Rossi"); g.setRuolo(RuoloGiocatore.DC);
        when(presenze.findPresentiTra(OGGI.minusDays(27), OGGI)).thenReturn(List.of(dati));
        when(giocatori.findAllById(any())).thenReturn(List.of(g));
        return service.carico(OGGI).getFirst();
    }

    @Test
    void caricoCostanteDaRapportoUno() {
        // 400 a settimana per quattro settimane
        var c = caricoDi(presenza(1, 2, 5, 80), presenza(1, 9, 5, 80), presenza(1, 16, 5, 80), presenza(1, 23, 5, 80));
        assertThat(c.caricoAcuto()).isEqualTo(400);
        assertThat(c.caricoCronico()).isEqualTo(400.0);
        assertThat(c.acwr()).isEqualTo(1.0);
        assertThat(c.storicoInsufficiente()).isFalse();
        assertThat(c.cognomeGiocatore()).isEqualTo("Rossi");
    }

    @Test
    void piccoDiCaricoNellUltimaSettimanaAlzaIlRapporto() {
        var c = caricoDi(presenza(1, 0, 8, 100), presenza(1, 6, 8, 100), presenza(1, 10, 4, 50), presenza(1, 20, 4, 50));
        assertThat(c.caricoAcuto()).isEqualTo(1600);
        assertThat(c.caricoCronico()).isEqualTo(500.0);
        assertThat(c.acwr()).isEqualTo(3.2);
    }

    @Test
    void laFinestraAcutaComprendeSetteGiorniConOggi() {
        var c = caricoDi(presenza(1, 6, 5, 60), presenza(1, 7, 5, 60));
        assertThat(c.caricoAcuto()).isEqualTo(300);
    }

    @Test
    void senzaMinutiUsaLaDurataDellaSeduta() {
        assertThat(caricoDi(presenza(1, 1, 6, null)).caricoAcuto()).isEqualTo(6 * 90);
    }

    @Test
    void presenzeSenzaRpeNonGeneranoCaricoMaVengonoContate() {
        var c = caricoDi(presenza(1, 1, null, 90), presenza(1, 20, null, null));
        assertThat(c.caricoAcuto()).isZero();
        assertThat(c.acwr()).isNull();
        assertThat(c.seduteConRpe()).isZero();
        assertThat(c.seduteSenzaRpe()).isEqualTo(2);
    }

    @Test
    void segnalaStoricoInsufficienteSeMancanoLeSettimanePiuLontane() {
        assertThat(caricoDi(presenza(1, 1, 5, 60), presenza(1, 13, 5, 60)).storicoInsufficiente()).isTrue();
        assertThat(caricoDi(presenza(1, 1, 5, 60), presenza(1, 14, 5, 60)).storicoInsufficiente()).isFalse();
    }
}

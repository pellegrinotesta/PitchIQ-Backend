package com.pitchiq.bff.PitchIQ.tattiche;

import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.model.Formazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.PosizioneCampo;
import com.pitchiq.bff.PitchIQ.tattiche.model.SchemaFormazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.SlotSchema;
import com.pitchiq.bff.PitchIQ.tattiche.repository.FormazioneRepository;
import com.pitchiq.bff.PitchIQ.tattiche.repository.SchemaFormazioneRepository;
import com.pitchiq.bff.PitchIQ.tattiche.service.SchemaFormazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SchemaFormazioneServiceTest {

    private final SchemaFormazioneRepository schemi = mock(SchemaFormazioneRepository.class);
    private final FormazioneRepository formazioni = mock(FormazioneRepository.class);
    private final GiocatoreRepository giocatori = mock(GiocatoreRepository.class);
    private final SchemaFormazioneService service = new SchemaFormazioneService(schemi, formazioni, giocatori,
            mock(com.pitchiq.bff.PitchIQ.partite.repository.PartitaRepository.class));

    private final Formazione formazione = new Formazione();
    private final SchemaFormazione schema = new SchemaFormazione();

    @BeforeEach
    void setUp() {
        formazione.setId(1L);
        formazione.setNome("Titolari");
        formazione.setModulo("4-3-3");
        schema.setId(9L);
        schema.setModulo("3-5-2");
        when(schemi.findByIdWithSlot(9L)).thenReturn(Optional.of(schema));
        when(schemi.findByIdWithFrecce(9L)).thenReturn(Optional.of(schema));
        when(formazioni.findByIdWithPosizioni(1L)).thenReturn(Optional.of(formazione));
        when(formazioni.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(formazioni.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private static Giocatore giocatore(long id, String cognome, RuoloGiocatore ruolo) {
        Giocatore g = new Giocatore();
        g.setId(id);
        g.setCognome(cognome);
        g.setRuolo(ruolo);
        g.setStato(StatoGiocatore.ATTIVO);
        return g;
    }

    private void slot(String slotRuolo, String ruolo, double x, double y) {
        SlotSchema s = new SlotSchema();
        s.setSchema(schema);
        s.setSlotRuolo(slotRuolo);
        s.setRuolo(ruolo);
        s.setCoordX(x);
        s.setCoordY(y);
        schema.getSlot().add(s);
    }

    private void inCampo(Giocatore g, boolean titolare) {
        PosizioneCampo p = new PosizioneCampo();
        p.setFormazione(formazione);
        p.setGiocatoreId(g.getId());
        p.setCoordX(50.0);
        p.setCoordY(50.0);
        p.setTitolare(titolare);
        formazione.getPosizioni().add(p);
    }

    private void rosa(Giocatore... lista) {
        when(giocatori.findAllByStato(StatoGiocatore.ATTIVO)).thenReturn(List.of(lista));
        when(giocatori.findAllById(any())).thenReturn(List.of(lista));
    }

    private Long giocatoreSu(String slotRuolo) {
        return formazione.getPosizioni().stream()
                .filter(p -> slotRuolo.equals(p.getSlotRuolo()))
                .map(PosizioneCampo::getGiocatoreId)
                .findFirst().orElse(null);
    }

    @Test
    void gliSlotPerCategoriaVengonoRiempitiConIRuoliSpecifici() {
        // prima del fix: slot "DIFENSORE" contro ruolo "DC" -> nessun abbinamento e formazione svuotata
        var izzo = giocatore(53, "Izzo", RuoloGiocatore.DC);
        var palmiero = giocatore(63, "Palmiero", RuoloGiocatore.CDC);
        var biasci = giocatore(72, "Biasci", RuoloGiocatore.SP);
        rosa(izzo, palmiero, biasci);
        slot("CB2", "DIFENSORE", 50, 76);
        slot("CM2", "CENTROCAMPISTA", 50, 44);
        slot("ST1", "ATTACCANTE", 35, 18);

        var risultato = service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("CB2")).isEqualTo(53L);
        assertThat(giocatoreSu("CM2")).isEqualTo(63L);
        assertThat(giocatoreSu("ST1")).isEqualTo(72L);
        assertThat(risultato.modulo()).isEqualTo("3-5-2");
    }

    @Test
    void lUnSlotPuoChiedereUnRuoloSpecifico() {
        var sala = giocatore(59, "Sala", RuoloGiocatore.TSS);
        var izzo = giocatore(53, "Izzo", RuoloGiocatore.DC);
        rosa(izzo, sala);
        slot("LWB", "TSS", 10, 52);

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("LWB")).isEqualTo(59L);
    }

    @Test
    void iTitolariAttualiHannoLaPrecedenzaSulRestoDellaRosa() {
        var simic = giocatore(55, "Simic", RuoloGiocatore.DC);
        var izzo = giocatore(53, "Izzo", RuoloGiocatore.DC);
        rosa(izzo, simic);
        inCampo(simic, true);
        slot("CB1", "DIFENSORE", 30, 74);

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("CB1")).isEqualTo(55L);
    }

    @Test
    void nessunTitolareSparisceSeMancaIlRuoloGiusto() {
        var russo = giocatore(70, "Russo", RuoloGiocatore.ALS);
        rosa(russo);
        inCampo(russo, true);
        slot("ST1", "ATTACCANTE", 35, 18); // nessun attaccante disponibile

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("ST1")).isEqualTo(70L);
    }

    @Test
    void laPanchinaRestaInFormazione() {
        var izzo = giocatore(53, "Izzo", RuoloGiocatore.DC);
        var favilli = giocatore(76, "Favilli", RuoloGiocatore.PC);
        rosa(izzo, favilli);
        inCampo(favilli, false);
        slot("CB1", "DIFENSORE", 30, 74);

        service.applicaAFormazione(9L, 1L);

        assertThat(formazione.getPosizioni())
                .anySatisfy(p -> {
                    assertThat(p.getGiocatoreId()).isEqualTo(76L);
                    assertThat(p.getTitolare()).isFalse();
                });
    }

    @Test
    void seNonCeNessunoLaFormazioneNonVieneToccata() {
        var izzo = giocatore(53, "Izzo", RuoloGiocatore.DC);
        rosa(izzo);
        inCampo(izzo, false);
        slot("GK", "PORTIERE", 50, 88);

        assertThatThrownBy(() -> service.applicaAFormazione(9L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("non è stata modificata");
        assertThat(formazione.getPosizioni()).hasSize(1);
        verify(formazioni, never()).saveAndFlush(any());
    }
}

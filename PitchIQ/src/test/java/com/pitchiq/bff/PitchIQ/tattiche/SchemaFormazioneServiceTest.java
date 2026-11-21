package com.pitchiq.bff.PitchIQ.tattiche;

import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.tattiche.dto.SchemaFormazioneRequest;
import com.pitchiq.bff.PitchIQ.tattiche.dto.SlotSchemaRequest;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseFormazione;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseGioco;
import com.pitchiq.bff.PitchIQ.tattiche.model.FaseSchema;
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
import java.util.Map;
import java.util.stream.IntStream;
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
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        var tedeschini = giocatore(63, "Tedeschini", RuoloGiocatore.CDC);
        var antonelli = giocatore(72, "Antonelli", RuoloGiocatore.SP);
        rosa(valsecchi, tedeschini, antonelli);
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
        var brambati = giocatore(59, "Brambati", RuoloGiocatore.TSS);
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        rosa(valsecchi, brambati);
        slot("LWB", "TSS", 10, 52);

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("LWB")).isEqualTo(59L);
    }

    @Test
    void iTitolariAttualiHannoLaPrecedenzaSulRestoDellaRosa() {
        var kremer = giocatore(55, "Kremer", RuoloGiocatore.DC);
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        rosa(valsecchi, kremer);
        inCampo(kremer, true);
        slot("CB1", "DIFENSORE", 30, 74);

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("CB1")).isEqualTo(55L);
    }

    @Test
    void nessunTitolareSparisceSeMancaIlRuoloGiusto() {
        var bettinelli = giocatore(70, "Bettinelli", RuoloGiocatore.ALS);
        rosa(bettinelli);
        inCampo(bettinelli, true);
        slot("ST1", "ATTACCANTE", 35, 18); // nessun attaccante disponibile

        service.applicaAFormazione(9L, 1L);

        assertThat(giocatoreSu("ST1")).isEqualTo(70L);
    }

    @Test
    void laPanchinaRestaInFormazione() {
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        var gervasoni = giocatore(76, "Gervasoni", RuoloGiocatore.PC);
        rosa(valsecchi, gervasoni);
        inCampo(gervasoni, false);
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
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        rosa(valsecchi);
        inCampo(valsecchi, false);
        slot("GK", "PORTIERE", 50, 88);

        assertThatThrownBy(() -> service.applicaAFormazione(9L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("non è stata modificata");
        assertThat(formazione.getPosizioni()).hasSize(1);
        verify(formazioni, never()).saveAndFlush(any());
    }

    @Test
    void applicandoLoSchemaLeSueFasiPassanoAiGiocatori() {
        var valsecchi = giocatore(53, "Valsecchi", RuoloGiocatore.DC);
        var antonelli = giocatore(72, "Antonelli", RuoloGiocatore.SP);
        rosa(valsecchi, antonelli);
        slot("CB1", "DIFENSORE", 50, 76);
        slot("ST", "ATTACCANTE", 50, 14);
        schema.setFasi(Map.of(FaseGioco.NON_POSSESSO, new FaseSchema(
                List.of(new FaseSchema.PosizioneSlot("ST", 50, 35), new FaseSchema.PosizioneSlot("XX", 1, 1)),
                List.of(new FaseFormazione.FrecciaFase(50, 35, 50, 45, "PRESSIONE")))));

        service.applicaAFormazione(9L, 1L);

        var nonPossesso = formazione.getFasi().get(FaseGioco.NON_POSSESSO);
        assertThat(nonPossesso.posizioni()).containsExactly(new FaseFormazione.PosizioneFase(72L, 50, 35));
        assertThat(nonPossesso.frecce()).hasSize(1);
    }

    private SchemaFormazioneRequest richiesta(List<SlotSchemaRequest> slot, Map<FaseGioco, FaseSchema> fasi) {
        return new SchemaFormazioneRequest("Pressing alto", "4-3-3", null, slot, List.of(), fasi);
    }

    @Test
    void iNomiDegliSlotDevonoEssereUnici() {
        var doppio = List.of(new SlotSchemaRequest("CB1", "DIFENSORE", 40.0, 75.0, null),
                new SlotSchemaRequest("cb1 ", "DIFENSORE", 60.0, 75.0, null));

        assertThatThrownBy(() -> service.create(richiesta(doppio, null))).hasMessageContaining("ripetuto");
        verify(schemi, never()).save(any());
    }

    @Test
    void alMassimoUndiciSlot() {
        var dodici = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new SlotSchemaRequest("S" + i, "CENTROCAMPISTA", 50.0, 50.0, null)).toList();

        assertThatThrownBy(() -> service.create(richiesta(dodici, null))).hasMessageContaining("11 slot");
    }

    @Test
    void leFasiTengonoSoloGliSlotEsistenti() {
        when(schemi.save(any())).thenAnswer(i -> i.getArgument(0));
        var slot = List.of(new SlotSchemaRequest("CB1", "DIFENSORE", 40.0, 75.0, null));
        var fasi = Map.of(
                FaseGioco.POSSESSO, new FaseSchema(List.of(new FaseSchema.PosizioneSlot("CB1", 40, 60),
                        new FaseSchema.PosizioneSlot("ST", 50, 10)), List.of()),
                FaseGioco.NON_POSSESSO, new FaseSchema(List.of(new FaseSchema.PosizioneSlot("ST", 50, 30)), List.of()));

        var dto = service.create(richiesta(slot, fasi));

        assertThat(dto.fasi()).containsOnlyKeys(FaseGioco.POSSESSO);
        assertThat(dto.fasi().get(FaseGioco.POSSESSO).posizioni()).extracting(FaseSchema.PosizioneSlot::slot).containsExactly("CB1");
    }
}

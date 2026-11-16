package com.pitchiq.bff.PitchIQ.squadre;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.squadre.dto.GiocatoreCreateRequest;
import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.repository.GiocatoreRepository;
import com.pitchiq.bff.PitchIQ.squadre.service.GiocatoreService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiocatoreServiceTest {
    private final GiocatoreRepository repository = mock(GiocatoreRepository.class);
        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        private final GiocatoreService service = new GiocatoreService(repository, objectMapper,
            Validation.buildDefaultValidatorFactory().getValidator(),
            new com.pitchiq.bff.PitchIQ.squadre.service.AccessoDatiRiservati());

    @org.junit.jupiter.api.BeforeEach
    void accedeComeAdmin() {
        accedeCome("ADMIN");
    }

    @org.junit.jupiter.api.AfterEach
    void esce() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private void accedeCome(String ruolo) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("utente", null,
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + ruolo))));
    }

    private Giocatore profiloRiservato() {
        Giocatore giocatore = profilo();
        giocatore.setStipendioAnnuo(new java.math.BigDecimal("900000"));
        giocatore.setClausola(new java.math.BigDecimal("5000000"));
        giocatore.setAgente("Agente");
        giocatore.setValoreMercato(new java.math.BigDecimal("2000000"));
        giocatore.setNoteMediche("Lesione al flessore");
        return giocatore;
    }

    @Test
    void ogniRuoloVedeSoloIDatiRiservatiDiCompetenza() {
        when(repository.findById(1L)).thenReturn(Optional.of(profiloRiservato()));

        accedeCome("ALLENATORE");
        var allenatore = service.findById(1L);
        assertThat(allenatore.stipendioAnnuo()).isNull();
        assertThat(allenatore.clausola()).isNull();
        assertThat(allenatore.agente()).isNull();
        assertThat(allenatore.noteMediche()).isNull();
        assertThat(allenatore.valoreMercato()).isEqualByComparingTo("2000000");
        assertThat(allenatore.datiEconomiciVisibili()).isFalse();
        assertThat(allenatore.datiMediciVisibili()).isFalse();

        accedeCome("DIRIGENTE");
        var dirigente = service.findById(1L);
        assertThat(dirigente.stipendioAnnuo()).isEqualByComparingTo("900000");
        assertThat(dirigente.agente()).isEqualTo("Agente");
        assertThat(dirigente.noteMediche()).isNull();
        assertThat(dirigente.datiEconomiciVisibili()).isTrue();

        accedeCome("MEDICO");
        var medico = service.findById(1L);
        assertThat(medico.noteMediche()).isEqualTo("Lesione al flessore");
        assertThat(medico.stipendioAnnuo()).isNull();
        assertThat(medico.datiMediciVisibili()).isTrue();

        accedeCome("ADMIN");
        var admin = service.findById(1L);
        assertThat(admin.stipendioAnnuo()).isNotNull();
        assertThat(admin.noteMediche()).isNotNull();
    }

    @Test
    void senzaAutenticazioneNessunDatoRiservatoEsce() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        when(repository.findAll()).thenReturn(java.util.List.of(profiloRiservato()));
        assertThat(service.findAll()).singleElement().satisfies(g -> {
            assertThat(g.stipendioAnnuo()).isNull();
            assertThat(g.noteMediche()).isNull();
        });
    }

    @Test
    void chiNonVedeUnDatoRiservatoNonPuoModificarloNeCancellarlo() throws Exception {
        Giocatore giocatore = profiloRiservato();
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        when(repository.save(giocatore)).thenReturn(giocatore);
        accedeCome("ALLENATORE");

        for (String vietato : java.util.List.of("{\"stipendioAnnuo\":1}", "{\"clausola\":null}",
                "{\"agente\":\"Altro\"}", "{\"noteMediche\":null}")) {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L, objectMapper.readTree(vietato)))
                    .isInstanceOfSatisfying(org.springframework.web.server.ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode().value()).isEqualTo(403));
        }
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());

        // Modifica consentita e sostituzione completa: i dati riservati restano intatti
        service.patch(1L, objectMapper.readTree("{\"nome\":\"Nuovo\"}"));
        service.update(1L, objectMapper.readValue(
                "{\"nome\":\"Nuovo\",\"cognome\":\"Rossi\",\"ruolo\":\"DC\",\"numeroMaglia\":5}", GiocatoreCreateRequest.class));
        assertThat(giocatore.getNome()).isEqualTo("Nuovo");
        assertThat(giocatore.getStipendioAnnuo()).isEqualByComparingTo("900000");
        assertThat(giocatore.getClausola()).isEqualByComparingTo("5000000");
        assertThat(giocatore.getAgente()).isEqualTo("Agente");
        assertThat(giocatore.getNoteMediche()).isEqualTo("Lesione al flessore");
        assertThat(giocatore.getValoreMercato()).isNull();
    }

    @Test
    void salvaFormazioneEFuoriListaEPreservaIlValoreSeOmesso() throws Exception {
        Giocatore giocatore = profilo();
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        when(repository.save(giocatore)).thenReturn(giocatore);
        assertThat(service.findById(1L).formazione()).isNull();
        assertThat(service.findById(1L).fuoriLista()).isFalse();

        var aggiornato = service.patch(1L, objectMapper.readTree("{\"formazione\":\"CLUB\",\"fuoriLista\":true}"));
        assertThat(aggiornato.formazione()).isEqualTo(com.pitchiq.bff.PitchIQ.squadre.model.FormazioneVivaio.CLUB);
        assertThat(aggiornato.fuoriLista()).isTrue();

        var dopo = service.patch(1L, objectMapper.readTree("{\"nome\":\"Nuovo\"}"));
        assertThat(dopo.formazione()).isEqualTo(com.pitchiq.bff.PitchIQ.squadre.model.FormazioneVivaio.CLUB);
        assertThat(dopo.fuoriLista()).isTrue();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L,
                objectMapper.readTree("{\"formazione\":\"IGNOTA\"}")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void ilMedicoAggiornaLeNoteMaNonIDatiEconomici() throws Exception {
        Giocatore giocatore = profiloRiservato();
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        when(repository.save(giocatore)).thenReturn(giocatore);
        accedeCome("MEDICO");
        assertThat(service.patch(1L, objectMapper.readTree("{\"noteMediche\":\"Guarito\"}")).noteMediche()).isEqualTo("Guarito");
        assertThat(giocatore.getStipendioAnnuo()).isEqualByComparingTo("900000");
    }

    @Test
    void etaSconosciutaNonDiventaZero() {
        Giocatore giocatore = new Giocatore();
        giocatore.setRuolo(RuoloGiocatore.DC);
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        assertThat(service.findById(1L).eta()).isNull();
    }

    @Test
    void etaCalcolataDallaDataDiNascita() {
        Giocatore giocatore = new Giocatore();
        giocatore.setRuolo(RuoloGiocatore.POR);
        giocatore.setDataNascita(LocalDate.now().minusYears(25));
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        assertThat(service.findById(1L).eta()).isEqualTo(25);
    }

    @Test
    void validaProfiloMinimoConDatiOpzionaliAssenti() throws Exception {
        assertThat(errori("{\"nome\":\"Marco\",\"cognome\":\"Rossi\",\"ruolo\":\"DC\",\"numeroMaglia\":5}")).isEmpty();
    }

    @Test
    void rifiutaNascitaFuturaEContrattoInvertito() throws Exception {
        assertThat(errori("""
                {"nome":"Marco","cognome":"Rossi","ruolo":"DC","dataNascita":"2999-01-01",
                 "contrattoInizio":"2027-01-01","contrattoFine":"2026-01-01"}
                """)).contains("dataNascita", "contrattoCoerente");
    }

    @Test
    void rifiutaImportiNegativiMisureErrateEPiedeIgnoto() throws Exception {
        assertThat(errori("""
                {"nome":"Marco","cognome":"Rossi","ruolo":"DC","altezzaCm":12,
                 "pesoKg":999,"valoreMercato":-1,"stipendioAnnuo":-1,"clausola":-1,"piedePreferito":"ALTRO"}
                """)).contains("altezzaCm", "pesoKg", "valoreMercato", "stipendioAnnuo", "clausola", "piedePreferito");
    }

    @Test
    void zeroEconomicoEContrattoInGiornataSonoValidi() throws Exception {
        assertThat(errori("""
                {"nome":"Marco","cognome":"Rossi","ruolo":"DC","numeroMaglia":5,"valoreMercato":0,
                 "contrattoInizio":"2026-01-01","contrattoFine":"2026-01-01"}
                """)).isEmpty();
    }

    @Test
    void patchPreservaOmissioniECancellaSoloNullEspliciti() throws Exception {
        Giocatore giocatore = profilo();
        giocatore.setAgente("Agente attuale");
        giocatore.setNoteMediche("Riservato");
        when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
        when(repository.save(giocatore)).thenReturn(giocatore);
        var aggiornato = service.patch(1L, objectMapper.readTree("{\"nome\":\"Nuovo\",\"agente\":null}"));
        assertThat(aggiornato.nome()).isEqualTo("Nuovo");
        assertThat(aggiornato.noteMediche()).isEqualTo("Riservato");
        assertThat(aggiornato.agente()).isNull();
        assertThat(aggiornato.numeroMaglia()).isEqualTo(5);
    }

    @Test
    void patchRifiutaCampiIgnoratiValoriInvalidiEFrazioni() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(profilo()));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L, objectMapper.readTree("{\"id\":2}")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L, objectMapper.readTree("{\"numeroMaglia\":1.5}")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L, objectMapper.readTree("{\"valoreMercato\":-1}")))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Giocatore profilo() {
        Giocatore giocatore = new Giocatore();
        giocatore.setNome("Marco"); giocatore.setCognome("Rossi");
        giocatore.setRuolo(RuoloGiocatore.DC); giocatore.setNumeroMaglia(5);
        return giocatore;
    }

    @Test
    void storicoRestituisceSoloDatiOsservatiSenzaProiezioni() {
        var statistiche = mock(com.pitchiq.bff.PitchIQ.statistiche.repository.StatisticaPeriodicaRepository.class);
        var proiezioni = mock(com.pitchiq.bff.PitchIQ.statistiche.service.ProiezioneService.class);
        var serviceStatistiche = new com.pitchiq.bff.PitchIQ.statistiche.service.StatisticaService(statistiche, repository, proiezioni);
        var dato = new com.pitchiq.bff.PitchIQ.statistiche.model.StatisticaPeriodica();
        dato.setGiocatoreId(1L); dato.setPeriodo("2026-10"); dato.setValore(0.0);
        dato.setCategoria(com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica.GOL);
        when(repository.existsById(1L)).thenReturn(true);
        when(statistiche.findAllByGiocatoreIdOrderByPeriodoAsc(1L)).thenReturn(java.util.List.of(dato));
        assertThat(serviceStatistiche.getStorico(1L)).singleElement().satisfies(statistica -> {
            assertThat(statistica.valore()).isZero();
            assertThat(statistica.periodo()).isEqualTo("2026-10");
        });
        org.mockito.Mockito.verifyNoInteractions(proiezioni);
    }

    @Test
    void storicoDistingueGiocatoreAssenteDaDatiAssenti() {
        var statistiche = mock(com.pitchiq.bff.PitchIQ.statistiche.repository.StatisticaPeriodicaRepository.class);
        var serviceStatistiche = new com.pitchiq.bff.PitchIQ.statistiche.service.StatisticaService(statistiche, repository,
                mock(com.pitchiq.bff.PitchIQ.statistiche.service.ProiezioneService.class));
        when(repository.existsById(1L)).thenReturn(true);
        when(statistiche.findAllByGiocatoreIdOrderByPeriodoAsc(1L)).thenReturn(java.util.List.of());
        assertThat(serviceStatistiche.getStorico(1L)).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> serviceStatistiche.getStorico(99L))
                .isInstanceOf(com.pitchiq.bff.PitchIQ.common.exception.ResourceNotFoundException.class);
    }

        @Test
        void apiPatchRende400PerTipoErratoEValidazione() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(profilo()));
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new com.pitchiq.bff.PitchIQ.squadre.controller.GiocatoreController(service))
            .setControllerAdvice(new com.pitchiq.bff.PitchIQ.common.exception.GlobalExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/squadre/giocatori/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"numeroMaglia\":1.5}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/squadre/giocatori/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"numeroMaglia\":null}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.errors.numeroMaglia").exists());
        }

        @Test
        void apiCreateRifiutaCodiciRuoloSecondariNonValidi() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new com.pitchiq.bff.PitchIQ.squadre.controller.GiocatoreController(service))
            .setControllerAdvice(new com.pitchiq.bff.PitchIQ.common.exception.GlobalExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/squadre/giocatori")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Marco\",\"cognome\":\"Rossi\",\"ruolo\":\"DC\",\"numeroMaglia\":5,\"ruoliSecondari\":\"ALTRO\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        void salvaMovimentiEConservaIlProfiloPreesistente() throws Exception {
            Giocatore giocatore = profilo();
            when(repository.findById(1L)).thenReturn(Optional.of(giocatore));
            when(repository.save(giocatore)).thenReturn(giocatore);
            var aggiornato = service.patch(1L, objectMapper.readTree("{\"movimentiConsentiti\":[\"SOVRAPPOSIZIONE\",\"RIPIEGAMENTO\"]}"));
            assertThat(aggiornato.movimentiConsentiti()).containsExactly(
                com.pitchiq.bff.PitchIQ.squadre.model.MovimentoTattico.SOVRAPPOSIZIONE,
                com.pitchiq.bff.PitchIQ.squadre.model.MovimentoTattico.RIPIEGAMENTO);
            assertThat(aggiornato.ruolo()).isEqualTo(RuoloGiocatore.DC);
            assertThat(service.patch(1L, objectMapper.readTree("{\"nome\":\"Nuovo\"}")).movimentiConsentiti()).hasSize(2);
            assertThat(service.patch(1L, objectMapper.readTree("{\"movimentiConsentiti\":[]}")).movimentiConsentiti()).isEmpty();
        }

        @Test
        void rifiutaMovimentiIgnotiDuplicatiONulli() throws Exception {
            when(repository.findById(1L)).thenReturn(Optional.of(profilo()));
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L,
                objectMapper.readTree("{\"movimentiConsentiti\":[\"IGNOTO\"]}")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L,
                objectMapper.readTree("{\"movimentiConsentiti\":[\"INSERIMENTO\",\"INSERIMENTO\"]}")))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L,
                objectMapper.readTree("{\"movimentiConsentiti\":[null]}")))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.patch(1L,
                objectMapper.readTree("{\"movimentiConsentiti\":[\"SOVRAPPOSIZIONE\",\"SOTTOPOSIZIONE\",\"INSERIMENTO\",\"TAGLIO_INTERNO\",\"AMPIEZZA\",\"COPERTURA\",\"USCITA_PALLA\"]}")))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        }

        private java.util.Set<String> errori(String json) throws Exception {
        GiocatoreCreateRequest richiesta = new ObjectMapper().findAndRegisterModules().readValue(json, GiocatoreCreateRequest.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(richiesta).stream()
                    .map(errore -> errore.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());
        }
    }
}
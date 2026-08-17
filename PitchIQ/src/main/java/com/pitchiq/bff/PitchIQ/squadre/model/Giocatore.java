package com.pitchiq.bff.PitchIQ.squadre.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="giocatori")
public class Giocatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cognome;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuoloGiocatore ruolo;

    @Column(name = "numero_maglia")
    private Integer numeroMaglia;

    // "DESTRO" | "SINISTRO" | "AMBIDESTRO"
    @Column(name = "piede_preferito", length = 20)
    private String piedePreferito;

    @Column(name = "contratto_inizio")
    private LocalDate contrattoInizio;

    @Column(name = "contratto_fine")
    private LocalDate contrattoFine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoGiocatore stato = StatoGiocatore.ATTIVO;

    @Column(length = 50)
    private String nazionalita;

    @Column(name = "luogo_nascita", length = 100)
    private String luogoNascita;

    @Column(name = "altezza_cm")
    private Integer altezzaCm;

    @Column(name = "peso_kg")
    private Integer pesoKg;

    // Ruoli secondari separati da virgola es. "DIFENSORE,CENTROCAMPISTA"
    @Column(name = "ruoli_secondari", length = 100)
    private String ruoliSecondari;

    @Column(name = "stipendio_annuo", precision = 12, scale = 2)
    private java.math.BigDecimal stipendioAnnuo;

    @Column(name = "valore_mercato", precision = 12, scale = 2)
    private java.math.BigDecimal valoreMercato;

    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal clausola;

    @Column(name = "note_mediche", length = 500)
    private String noteMediche;

    @Column(length = 100)
    private String agente;
}

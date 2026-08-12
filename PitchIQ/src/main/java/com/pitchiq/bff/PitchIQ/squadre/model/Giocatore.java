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
}

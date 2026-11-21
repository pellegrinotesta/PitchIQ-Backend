package com.pitchiq.bff.PitchIQ.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Etichetta data dall'utente a una clip dell'analisi video: un esempio per il classificatore eventi. */
@Entity
@Table(name = "etichette_clip")
@Getter
@Setter
@NoArgsConstructor
public class EtichettaClip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analisi_id", nullable = false, length = 36)
    private String analisiId;

    @Column(name = "clip_id", nullable = false, length = 16)
    private String clipId;

    @Column(nullable = false, length = 32)
    private String etichetta;

    /** Caratteristiche numeriche della clip calcolate da pitchiq-analytics, es. {"velocita_max_palla": 18.2}. */
    @Column(name = "caratteristiche_json", nullable = false, columnDefinition = "json")
    private String caratteristicheJson;

    @Column(name = "inizio_secondi", nullable = false)
    private double inizioSecondi;

    @Column(name = "fine_secondi", nullable = false)
    private double fineSecondi;

    @Column(name = "creata_il", insertable = false, updatable = false)
    private LocalDateTime creataIl;

    @Column(name = "aggiornata_il", insertable = false, updatable = false)
    private LocalDateTime aggiornataIl;
}

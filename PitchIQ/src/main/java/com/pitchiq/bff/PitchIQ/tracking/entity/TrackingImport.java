package com.pitchiq.bff.PitchIQ.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Un caricamento di file dall'app. Il BFF la crea in IN_CODA,
 * il servizio pitchiq-analytics aggiorna stato, messaggio e matchId.
 */
@Entity
@Table(name = "tracking_import")
@Getter
@Setter
@NoArgsConstructor
public class TrackingImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoImport stato;

    @Column(length = 500)
    private String messaggio;

    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "con_tracking", nullable = false, columnDefinition = "tinyint(1)")
    private boolean conTracking;

    @Column(name = "nome_file_eventi", nullable = false)
    private String nomeFileEventi;

    /** Valorizzati dal database (DEFAULT / ON UPDATE CURRENT_TIMESTAMP). */
    @Column(name = "creato_il", insertable = false, updatable = false)
    private LocalDateTime creatoIl;

    @Column(name = "aggiornato_il", insertable = false, updatable = false)
    private LocalDateTime aggiornatoIl;
}

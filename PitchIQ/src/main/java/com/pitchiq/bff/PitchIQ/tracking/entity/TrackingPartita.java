package com.pitchiq.bff.PitchIQ.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Scritta dal servizio pitchiq-analytics: il BFF la legge soltanto. */
@Entity
@Immutable
@Table(name = "tracking_partita")
@Getter
@NoArgsConstructor
public class TrackingPartita {

    @Id
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "import_id", nullable = false)
    private Long importId;

    @Column(name = "data_partita", nullable = false)
    private LocalDateTime dataPartita;

    @Column(name = "squadra_casa", nullable = false)
    private String squadraCasa;

    @Column(name = "squadra_trasferta", nullable = false)
    private String squadraTrasferta;

    @Column(name = "gol_casa", nullable = false)
    private int golCasa;

    @Column(name = "gol_trasferta", nullable = false)
    private int golTrasferta;

    private String competizione;

    /** {"playerId": {"nome": "...", "teamId": 123, "numero": 10}} */
    @Column(name = "giocatori_json", nullable = false, columnDefinition = "json")
    private String giocatoriJson;

    @Column(name = "elaborata_il", nullable = false)
    private LocalDateTime elaborataIl;
}

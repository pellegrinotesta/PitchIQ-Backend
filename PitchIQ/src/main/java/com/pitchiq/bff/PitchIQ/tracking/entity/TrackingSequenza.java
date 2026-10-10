package com.pitchiq.bff.PitchIQ.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/** Un possesso pressato. Scritta dal servizio pitchiq-analytics. */
@Entity
@Immutable
@Table(name = "tracking_sequenza")
@Getter
@NoArgsConstructor
public class TrackingSequenza {

    @Id
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "possesso_id", nullable = false)
    private String possessoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EsitoPressione esito;

    private int periodo;
    private int minuto;
    private int secondo;

    @Column(name = "team_id")
    private Long teamId;

    private String squadra;

    @Column(name = "portatore_id")
    private Long portatoreId;

    private String portatore;
    private String terzo;

    @Column(name = "n_pressori")
    private int nPressori;

    @Column(name = "n_opzioni")
    private int nOpzioni;

    @Column(name = "valore_portatore")
    private BigDecimal valorePortatore;

    @Column(name = "valore_medio")
    private BigDecimal valoreMedio;

    @Column(name = "valore_migliore")
    private BigDecimal valoreMigliore;

    @Column(name = "esito_possesso")
    private String esitoPossesso;

    @Column(name = "porta_al_tiro", columnDefinition = "tinyint(1)")
    private boolean portaAlTiro;

    @Column(name = "ha_frame", columnDefinition = "tinyint(1)")
    private boolean haFrame;
}

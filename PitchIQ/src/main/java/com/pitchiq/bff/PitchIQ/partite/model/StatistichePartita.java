package com.pitchiq.bff.PitchIQ.partite.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "statistiche_partita")
@Getter @Setter @NoArgsConstructor
public class StatistichePartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partita_id", nullable = false)
    private Partita partita;

    @Column(name = "possesso_pct")
    private Integer possessoPct;

    @Column(name = "tiri_totali")
    private Integer tiriTotali;

    @Column(name = "tiri_in_porta")
    private Integer tiriInPorta;

    @Column
    private Integer passaggi;

    @Column(name = "passaggi_riusciti")
    private Integer passaggiRiusciti;

    @Column(name = "duelli_vinti")
    private Integer duelliVinti;

    @Column(name = "duelli_totali")
    private Integer duelliTotali;

    @Column
    private Integer corner;

    @Column
    private Integer falli;

    @Column
    private Integer fuorigioco;

    @Column(precision = 4, scale = 2)
    private BigDecimal xg;
}

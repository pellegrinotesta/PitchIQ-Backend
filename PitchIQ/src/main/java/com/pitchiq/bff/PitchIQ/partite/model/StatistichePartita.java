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

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer possessoPct;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer tiriTotali;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer tiriInPorta;

    @Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer passaggi;

    @Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer passaggiRiusciti;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer duelliVinti;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer duelliTotali;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer corner;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer falli;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer fuorigioco;

    @Column(precision = 4, scale = 2)
    private BigDecimal xg;
}

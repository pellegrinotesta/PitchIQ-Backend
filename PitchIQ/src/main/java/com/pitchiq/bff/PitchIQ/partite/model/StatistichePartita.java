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

    @Column(precision = 4, scale = 1)
    private BigDecimal possessoPct;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer tiriTotali;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer tiriInPorta;

    @Column(columnDefinition = "SMALLINT UNSIGNED")
    private Integer passaggi;

    @Column(columnDefinition = "SMALLINT UNSIGNED")
    private Integer passaggiRiusciti;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer duelliVinti;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer duelliTotali;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer corner;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer falli;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer fuorigioco;

    @Column(precision = 4, scale = 2)
    private BigDecimal xg;

    @Column(length = 255)
    private String fonte;
}

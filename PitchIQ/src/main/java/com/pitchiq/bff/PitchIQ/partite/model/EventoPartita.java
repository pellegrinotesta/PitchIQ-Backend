package com.pitchiq.bff.PitchIQ.partite.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "eventi_partita")
@Getter @Setter @NoArgsConstructor
public class EventoPartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partita_id", nullable = false)
    private Partita partita;

    @Column(nullable = false)
    private Integer minuto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipo;

    @Column(name = "giocatore_id")
    private Long giocatoreId;

    @Column(length = 200)
    private String nota;

    @Column(name = "coord_x", precision = 5, scale = 2)
    private BigDecimal coordX;

    @Column(name = "coord_y", precision = 5, scale = 2)
    private BigDecimal coordY;

    @Column(precision = 4, scale = 2)
    private BigDecimal xg;
}



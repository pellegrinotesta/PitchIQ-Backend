package com.pitchiq.bff.PitchIQ.statistiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "statistiche_periodiche",
        uniqueConstraints = @UniqueConstraint(columnNames = {"giocatore_id", "periodo", "categoria"}))
@Getter @Setter @NoArgsConstructor
public class StatisticaPeriodica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giocatore_id", nullable = false)
    private Long giocatoreId;

    // Formato "YYYY-MM" — YearMonth serializzato come stringa
    @Column(nullable = false, length = 7)
    private String periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaMetrica categoria;

    @Column(nullable = false)
    private Double valore;
}
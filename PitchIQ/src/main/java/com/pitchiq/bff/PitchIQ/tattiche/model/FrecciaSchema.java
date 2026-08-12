package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "frecce_schema")
@Getter
@Setter
@NoArgsConstructor
public class FrecciaSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_id", nullable = false)
    private SchemaFormazione schema;

    // Punto di partenza
    @Column(name = "start_x", nullable = false)
    private Double startX;

    @Column(name = "start_y", nullable = false)
    private Double startY;

    // Punto di arrivo
    @Column(name = "end_x", nullable = false)
    private Double endX;

    @Column(name = "end_y", nullable = false)
    private Double endY;

    // Colore freccia (es. "#00ff87", "#ff6464")
    @Column(length = 10)
    private String colore = "#00ff87";

    // Etichetta opzionale sulla freccia
    @Column(length = 100)
    private String etichetta;
}

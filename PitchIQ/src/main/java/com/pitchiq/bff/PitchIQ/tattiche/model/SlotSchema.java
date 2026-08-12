package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slot_schema")
@Getter @Setter @NoArgsConstructor
public class SlotSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_id", nullable = false)
    private SchemaFormazione schema;

    // Es. "GK", "CB1", "CB2", "LB", "RB", "CM1"...
    @Column(name = "slot_ruolo", nullable = false, length = 20)
    private String slotRuolo;

    // Ruolo generico (PORTIERE, DIFENSORE, CENTROCAMPISTA, ATTACCANTE)
    @Column(nullable = false, length = 20)
    private String ruolo;

    @Column(name = "coord_x", nullable = false)
    private Double coordX;

    @Column(name = "coord_y", nullable = false)
    private Double coordY;

    @Column(length = 500)
    private String note;
}
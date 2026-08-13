package com.pitchiq.bff.PitchIQ.allenamenti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "presenze")
@Getter @Setter @NoArgsConstructor
public class Presenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seduta_id", nullable = false)
    private SedutaAllenamento seduta;

    @Column(name = "giocatore_id", nullable = false)
    private Long giocatoreId;

    @Column(nullable = false)
    private Boolean presente = true;

    @Column(name = "motivo_assenza", length = 200)
    private String motivoAssenza;

    // Valutazione 1-10, null se assente
    @Column
    private Integer valutazione;
}

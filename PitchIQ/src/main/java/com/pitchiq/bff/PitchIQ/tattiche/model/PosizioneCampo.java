package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posizioni_campo")
@Getter @Setter @NoArgsConstructor
public class PosizioneCampo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formazione_id", nullable = false)
    private Formazione formazione;

    // ID del giocatore dalla tabella giocatori
    @Column(name = "giocatore_id", nullable = false)
    private Long giocatoreId;

    // Coordinate sul campo (0-100 percentuale)
    @Column(name = "coord_x", nullable = false)
    private Double coordX;

    @Column(name = "coord_y", nullable = false)
    private Double coordY;

    // Slot ruolo (es. "CB1", "ST", "GK")
    @Column(name = "slot_ruolo", length = 20)
    private String slotRuolo;

    @Column(nullable = false)
    private Boolean titolare = true;
}

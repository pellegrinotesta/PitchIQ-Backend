package com.pitchiq.bff.PitchIQ.partite.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scouting_avversari")
@Getter @Setter @NoArgsConstructor
public class ScoutingAvversario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 20)
    private String modulo;

    @Column(name = "punti_forza", length = 500)
    private String puntiForza;

    @Column(length = 500)
    private String debolezze;

    @Column(name = "giocatori_chiave", length = 300)
    private String giocatoriChiave;

    @Column(length = 500)
    private String note;
}

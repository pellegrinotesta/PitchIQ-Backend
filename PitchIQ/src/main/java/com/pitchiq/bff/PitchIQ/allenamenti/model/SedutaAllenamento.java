package com.pitchiq.bff.PitchIQ.allenamenti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sedute_allenamento")
@Getter @Setter @NoArgsConstructor
public class SedutaAllenamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSeduta tipo;

    @Column(length = 100)
    private String luogo;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "seduta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Presenza> presenze = new ArrayList<>();
}

package com.pitchiq.bff.PitchIQ.partite.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partite")
@Getter @Setter @NoArgsConstructor
public class Partita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 100)
    private String avversario;

    @Column(length = 100)
    private String competizione;

    @Enumerated(EnumType.STRING)
    @Column(name = "casa_trasferta", nullable = false, length = 10)
    private CasaTrasferta casaTrasferta = CasaTrasferta.CASA;

    @Column(name = "gol_fatti", nullable = false)
    private Integer golFatti = 0;

    @Column(name = "gol_subiti", nullable = false)
    private Integer golSubiti = 0;

    @Column(length = 20)
    private String modulo;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "partita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoPartita> eventi = new ArrayList<>();

    @OneToOne(mappedBy = "partita", cascade = CascadeType.ALL, orphanRemoval = true)
    private StatistichePartita statistiche;
}
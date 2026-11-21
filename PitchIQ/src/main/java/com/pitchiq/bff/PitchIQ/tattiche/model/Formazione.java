package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "formazioni")
@Getter @Setter @NoArgsConstructor
public class Formazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    // Es. "4-3-3", "4-4-2", "3-5-2"
    @Column(nullable = false, length = 20)
    private String modulo;

    @Column
    private LocalDate data;

    @Column(length = 500)
    private String note;

    /** Partita del calendario per cui è preparata; null per le formazioni generiche. */
    @Column(name = "partita_id")
    private Long partitaId;

    /** Disposizione dei titolari per fase di gioco; null o vuoto se non ancora definita. */
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private java.util.Map<FaseGioco, FaseFormazione> fasi;

    @OneToMany(mappedBy = "formazione", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosizioneCampo> posizioni = new ArrayList<>();
}

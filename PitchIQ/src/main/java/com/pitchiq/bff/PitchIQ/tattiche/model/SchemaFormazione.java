package com.pitchiq.bff.PitchIQ.tattiche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schemi_formazione")
@Getter @Setter @NoArgsConstructor
public class SchemaFormazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 20)
    private String modulo;

    @Column(length = 500)
    private String descrizione;

    @OneToMany(mappedBy = "schema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlotSchema> slot = new ArrayList<>();

    @OneToMany(mappedBy = "schema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FrecciaSchema> frecce = new ArrayList<>();
}

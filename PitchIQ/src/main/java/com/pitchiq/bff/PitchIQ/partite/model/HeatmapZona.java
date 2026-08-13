package com.pitchiq.bff.PitchIQ.partite.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "heatmap_zone")
@Getter @Setter @NoArgsConstructor
public class HeatmapZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partita_id", nullable = false)
    private Long partitaId;

    @Column(name = "giocatore_id", nullable = false)
    private Long giocatoreId;

    @Column(name = "coord_x", nullable = false, precision = 5, scale = 2)
    private BigDecimal coordX;

    @Column(name = "coord_y", nullable = false, precision = 5, scale = 2)
    private BigDecimal coordY;

    @Column(nullable = false)
    private Integer intensita = 1;
}
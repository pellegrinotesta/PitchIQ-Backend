package com.pitchiq.bff.PitchIQ.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Animazione di una sequenza: posizioni a 10 fps in JSON. Scritta da pitchiq-analytics. */
@Entity
@Immutable
@Table(name = "tracking_frame")
@Getter
@NoArgsConstructor
public class TrackingFrame {

    @Id
    @Column(name = "sequenza_id")
    private Long sequenzaId;

    @Column(name = "pressori_json", nullable = false, columnDefinition = "json")
    private String pressoriJson;

    @Column(name = "press_inizio", nullable = false)
    private int pressInizio;

    @Column(name = "press_fine", nullable = false)
    private int pressFine;

    /** [{"i":0,"b":[x,y],"c":carrierId,"p":[[id,att,x,y]],"o":{"id":valore}}] */
    @Column(name = "frames_json", nullable = false, columnDefinition = "json")
    private String framesJson;
}

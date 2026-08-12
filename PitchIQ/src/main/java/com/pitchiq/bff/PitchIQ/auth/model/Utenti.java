package com.pitchiq.bff.PitchIQ.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "utenti")
public class Utenti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // bcrypt hash — mai la password in chiaro
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // ALLENATORE | DIRIGENTE | ADMIN
    @Column(nullable = false, length = 30)
    private String ruolo;
}

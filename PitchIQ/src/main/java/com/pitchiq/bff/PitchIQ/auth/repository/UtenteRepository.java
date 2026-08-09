package com.pitchiq.bff.PitchIQ.auth.repository;

import com.pitchiq.bff.PitchIQ.auth.model.Utenti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utenti, Long> {
    Optional<Utenti> findByUsername(String username);
}

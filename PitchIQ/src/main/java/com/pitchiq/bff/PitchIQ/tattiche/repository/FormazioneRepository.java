package com.pitchiq.bff.PitchIQ.tattiche.repository;

import com.pitchiq.bff.PitchIQ.tattiche.model.Formazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FormazioneRepository extends JpaRepository<Formazione, Long> {

    // Lista leggera senza posizioni per la sidebar
    @Query("SELECT f FROM Formazione f ORDER BY f.data DESC")
    List<Formazione> findAllOrderByDataDesc();

    // Con posizioni per l'editor
    @Query("SELECT f FROM Formazione f LEFT JOIN FETCH f.posizioni WHERE f.id = :id")
    Optional<Formazione> findByIdWithPosizioni(Long id);
}

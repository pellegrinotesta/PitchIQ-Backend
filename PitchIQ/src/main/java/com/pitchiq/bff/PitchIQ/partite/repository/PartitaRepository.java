package com.pitchiq.bff.PitchIQ.partite.repository;

import com.pitchiq.bff.PitchIQ.partite.model.Partita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PartitaRepository extends JpaRepository<Partita, Long> {

    @Query("SELECT p FROM Partita p ORDER BY p.data DESC")
    List<Partita> findAllOrdered();

    @Query("SELECT p FROM Partita p LEFT JOIN FETCH p.eventi WHERE p.id = :id")
    Optional<Partita> findByIdWithEventi(Long id);
}
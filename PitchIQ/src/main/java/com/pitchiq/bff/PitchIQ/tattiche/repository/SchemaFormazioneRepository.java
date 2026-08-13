package com.pitchiq.bff.PitchIQ.tattiche.repository;

import com.pitchiq.bff.PitchIQ.tattiche.model.SchemaFormazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SchemaFormazioneRepository extends JpaRepository<SchemaFormazione, Long> {

    @Query("SELECT s FROM SchemaFormazione s ORDER BY s.nome")
    List<SchemaFormazione> findAllOrdered();

    // Prima query: fetch slot
    @Query("SELECT s FROM SchemaFormazione s LEFT JOIN FETCH s.slot WHERE s.id = :id")
    Optional<SchemaFormazione> findByIdWithSlot(Long id);

    // Seconda query: fetch frecce
    @Query("SELECT s FROM SchemaFormazione s LEFT JOIN FETCH s.frecce WHERE s.id = :id")
    Optional<SchemaFormazione> findByIdWithFrecce(Long id);
}
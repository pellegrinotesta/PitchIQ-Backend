package com.pitchiq.bff.PitchIQ.tattiche.repository;

import com.pitchiq.bff.PitchIQ.tattiche.model.SchemaFormazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SchemaFormazioneRepository extends JpaRepository<SchemaFormazione, Long> {

    @Query("SELECT s FROM SchemaFormazione s ORDER BY s.nome")
    List<SchemaFormazione> findAllOrdered();

    @Query("SELECT s FROM SchemaFormazione s LEFT JOIN FETCH s.slot LEFT JOIN FETCH s.frecce WHERE s.id = :id")
    Optional<SchemaFormazione> findByIdWithDetails(Long id);
}
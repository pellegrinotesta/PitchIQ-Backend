package com.pitchiq.bff.PitchIQ.allenamenti.repository;

import com.pitchiq.bff.PitchIQ.allenamenti.model.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

import java.util.List;

public interface PresenzaRepository extends JpaRepository<Presenza, Long> {
    List<Presenza> findAllByGiocatoreId(Long giocatoreId);

    @Query("SELECT p FROM Presenza p JOIN FETCH p.seduta s WHERE p.presente = true AND s.data BETWEEN :dal AND :al")
    List<Presenza> findPresentiTra(LocalDate dal, LocalDate al);
}
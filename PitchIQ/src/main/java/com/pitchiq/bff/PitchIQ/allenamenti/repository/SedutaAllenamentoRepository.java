package com.pitchiq.bff.PitchIQ.allenamenti.repository;

import com.pitchiq.bff.PitchIQ.allenamenti.model.SedutaAllenamento;
import com.pitchiq.bff.PitchIQ.allenamenti.model.TipoSeduta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SedutaAllenamentoRepository extends JpaRepository<SedutaAllenamento, Long> {

    @Query("SELECT s FROM SedutaAllenamento s ORDER BY s.data DESC, s.oraInizio DESC")
    List<SedutaAllenamento> findAllOrdered();

    List<SedutaAllenamento> findAllByDataBetweenOrderByDataAsc(LocalDate from, LocalDate to);

    List<SedutaAllenamento> findAllByTipo(TipoSeduta tipo);

    @Query("SELECT s FROM SedutaAllenamento s LEFT JOIN FETCH s.presenze WHERE s.id = :id")
    Optional<SedutaAllenamento> findByIdWithPresenze(Long id);
}
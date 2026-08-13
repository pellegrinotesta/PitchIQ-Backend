package com.pitchiq.bff.PitchIQ.allenamenti.repository;

import com.pitchiq.bff.PitchIQ.allenamenti.model.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresenzaRepository extends JpaRepository<Presenza, Long> {
    List<Presenza> findAllByGiocatoreId(Long giocatoreId);
}
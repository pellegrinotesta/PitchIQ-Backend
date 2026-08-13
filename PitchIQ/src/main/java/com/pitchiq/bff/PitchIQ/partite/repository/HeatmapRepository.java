package com.pitchiq.bff.PitchIQ.partite.repository;

import com.pitchiq.bff.PitchIQ.partite.model.HeatmapZona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeatmapRepository extends JpaRepository<HeatmapZona, Long> {
    List<HeatmapZona> findAllByPartitaIdAndGiocatoreId(Long partitaId, Long giocatoreId);
    List<HeatmapZona> findAllByPartitaId(Long partitaId);
    void deleteAllByPartitaIdAndGiocatoreId(Long partitaId, Long giocatoreId);
}

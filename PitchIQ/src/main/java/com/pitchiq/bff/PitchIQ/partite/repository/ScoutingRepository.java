package com.pitchiq.bff.PitchIQ.partite.repository;

import com.pitchiq.bff.PitchIQ.partite.model.ScoutingAvversario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoutingRepository extends JpaRepository<ScoutingAvversario, Long> {
    List<ScoutingAvversario> findAllByOrderByNomeAsc();
}

package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingPartita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrackingPartitaRepository extends JpaRepository<TrackingPartita, Long> {

    List<TrackingPartita> findAllByOrderByDataPartitaDesc();

    /** Sequenze e frame vengono eliminati in cascata dalle foreign key. */
    @Modifying
    @Query(value = "DELETE FROM tracking_partita WHERE match_id = :matchId", nativeQuery = true)
    int eliminaPartita(Long matchId);
}

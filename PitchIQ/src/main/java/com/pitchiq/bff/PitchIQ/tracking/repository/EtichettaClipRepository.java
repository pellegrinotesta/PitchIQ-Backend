package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.entity.EtichettaClip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtichettaClipRepository extends JpaRepository<EtichettaClip, Long> {

    List<EtichettaClip> findByAnalisiId(String analisiId);

    Optional<EtichettaClip> findByAnalisiIdAndClipId(String analisiId, String clipId);
}

package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingFrame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingFrameRepository extends JpaRepository<TrackingFrame, Long> {
}

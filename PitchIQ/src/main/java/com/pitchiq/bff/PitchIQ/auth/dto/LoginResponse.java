package com.pitchiq.bff.PitchIQ.auth.dto;

public record LoginResponse(
        String token,
        long expiresIn,
        String ruolo
) {
}

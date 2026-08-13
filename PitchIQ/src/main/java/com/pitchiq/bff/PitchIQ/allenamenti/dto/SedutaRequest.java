package com.pitchiq.bff.PitchIQ.allenamenti.dto;

import com.pitchiq.bff.PitchIQ.allenamenti.model.TipoSeduta;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SedutaRequest(
        @NotNull LocalDate data,
        @NotNull LocalTime oraInizio,
        @NotNull LocalTime oraFine,
        @NotNull TipoSeduta tipo,
        String luogo,
        String note,
        List<PresenzaRequest> presenze
) {}

package com.pitchiq.bff.PitchIQ.partite.dto;

import com.pitchiq.bff.PitchIQ.partite.model.CasaTrasferta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PartitaRequest(
        @NotNull LocalDate data,
        @NotBlank String avversario,
        String competizione,
        CasaTrasferta casaTrasferta,
        Integer golFatti,
        Integer golSubiti,
        String modulo,
        String note
) {}

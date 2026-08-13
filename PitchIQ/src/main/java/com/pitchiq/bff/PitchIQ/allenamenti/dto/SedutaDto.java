package com.pitchiq.bff.PitchIQ.allenamenti.dto;

import com.pitchiq.bff.PitchIQ.allenamenti.model.TipoSeduta;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record SedutaDto(
        Long id,
        LocalDate data,
        LocalTime oraInizio,
        LocalTime oraFine,
        TipoSeduta tipo,
        String luogo,
        String note,
        List<PresenzaDto> presenze,
        int totalePresenti,
        int totaleAssenti
) {}

package com.pitchiq.bff.PitchIQ.partite.dto;

import com.pitchiq.bff.PitchIQ.partite.model.CasaTrasferta;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record PartitaDto(
        Long id,
        LocalDate data,
        String avversario,
        String competizione,
        CasaTrasferta casaTrasferta,
        Integer golFatti,
        Integer golSubiti,
        String risultato,     // "V" | "P" | "N"
        String modulo,
        String note,
        List<EventoDto> eventi,
        StatistichePartitaDto statistiche
) {}

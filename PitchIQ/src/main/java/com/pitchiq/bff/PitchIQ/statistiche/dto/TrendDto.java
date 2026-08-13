package com.pitchiq.bff.PitchIQ.statistiche.dto;

import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import lombok.Builder;
import java.util.List;

@Builder
public record TrendDto(
        Long giocatoreId,
        String nomeGiocatore,
        CategoriaMetrica categoria,
        List<PuntoTrendDto> storico,     // dati reali ordinati
        List<PuntoTrendDto> proiezione,  // estrapolazione lineare futura
        Double indiceCrescita,           // variazione % ultimi 3 mesi vs 3 precedenti
        String tendenza                  // "CRESCITA" | "STABILE" | "CALO"
) {}

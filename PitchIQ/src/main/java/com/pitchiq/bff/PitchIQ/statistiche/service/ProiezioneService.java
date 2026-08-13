package com.pitchiq.bff.PitchIQ.statistiche.service;

import com.pitchiq.bff.PitchIQ.statistiche.dto.PuntoTrendDto;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProiezioneService {

    // Regressione lineare semplice (minimi quadrati) sui dati storici.
    // Restituisce N mesi di proiezione futura.
    public List<PuntoTrendDto> calcola(List<PuntoTrendDto> storico, int mesiAvanti) {
        if (storico.size() < 2) return List.of();

        int n = storico.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX  += i;
            sumY  += storico.get(i).valore();
            sumXY += i * storico.get(i).valore();
            sumX2 += (double) i * i;
        }

        // Coefficiente angolare e intercetta
        double den = n * sumX2 - sumX * sumX;
        if (den == 0) return List.of();

        double m = (n * sumXY - sumX * sumY) / den;
        double b = (sumY - m * sumX) / n;

        // Ricava il periodo dell'ultimo punto storico
        YearMonth ultimo = YearMonth.parse(storico.getLast().periodo());

        List<PuntoTrendDto> proiezione = new ArrayList<>();
        for (int i = 1; i <= mesiAvanti; i++) {
            double valoreStimato = m * (n - 1 + i) + b;
            proiezione.add(PuntoTrendDto.builder()
                    .periodo(ultimo.plusMonths(i).toString())
                    .valore(Math.max(0, Math.round(valoreStimato * 100.0) / 100.0))
                    .build());
        }

        return proiezione;
    }

    // Calcola indice di crescita: variazione % media ultimi 3 mesi vs 3 precedenti
    public double calcolaIndiceCrescita(List<PuntoTrendDto> storico) {
        if (storico.size() < 4) return 0.0;

        int n = storico.size();
        double mediaRecente = storico.subList(n - 3, n).stream()
                .mapToDouble(PuntoTrendDto::valore).average().orElse(0);
        double mediaPrecedente = storico.subList(Math.max(0, n - 6), n - 3).stream()
                .mapToDouble(PuntoTrendDto::valore).average().orElse(0);

        if (mediaPrecedente == 0) return 0.0;
        return Math.round(((mediaRecente - mediaPrecedente) / mediaPrecedente) * 10000.0) / 100.0;
    }

    public String calcolaTendenza(double indiceCrescita) {
        if (indiceCrescita > 5)  return "CRESCITA";
        if (indiceCrescita < -5) return "CALO";
        return "STABILE";
    }
}

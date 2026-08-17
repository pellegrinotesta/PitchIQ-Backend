package com.pitchiq.bff.PitchIQ.partite.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class XgCalcolatoreService {

    // Coordinate porta avversaria (campo normalizzato 0-100)
    // La porta è in cima al campo (Y=0), centrata (X=50)
    private static final double PORTA_X = 50.0;
    private static final double PORTA_Y = 5.0;

    // Dimensioni reali campo standard (m) per scala coordinate
    private static final double CAMPO_LARGHEZZA = 68.0;
    private static final double CAMPO_LUNGHEZZA = 105.0;

    /**
     * Calcola xG per un singolo tiro dato la posizione sul campo.
     * coordX e coordY sono percentuali (0-100) rispetto al campo.
     * Il modello usa distanza e angolo — semplificato ma trasparente.
     */
    public double calcolaXgTiro(double coordX, double coordY) {
        // Converti coordinate % in metri reali
        double xMetri = (coordX / 100.0) * CAMPO_LARGHEZZA;
        double yMetri = (coordY / 100.0) * CAMPO_LUNGHEZZA;

        double portaXMetri = (PORTA_X / 100.0) * CAMPO_LARGHEZZA;
        double portaYMetri = (PORTA_Y / 100.0) * CAMPO_LUNGHEZZA;

        // Distanza dalla porta in metri
        double distanza = Math.sqrt(
                Math.pow(xMetri - portaXMetri, 2) +
                        Math.pow(yMetri - portaYMetri, 2)
        );

        // Angolo visuale verso la porta (larghezza porta = 7.32m)
        double larghezzaPorta = 7.32;
        double angolo = Math.atan(larghezzaPorta * distanza /
                (distanza * distanza - Math.pow(larghezzaPorta / 2, 2)));
        angolo = Math.max(0, angolo);

        // Formula logistica semplificata calibrata su dati pubblici
        // xG = 1 / (1 + e^-(a + b*distanza + c*angolo))
        double a =  0.2;
        double b = -0.08;
        double c =  1.5;

        double logit = a + b * distanza + c * angolo;
        double xg = 1.0 / (1.0 + Math.exp(-logit));

        // Cap a 0.95 (nessun tiro è certezza assoluta)
        xg = Math.min(0.95, Math.max(0.01, xg));

        return Math.round(xg * 100.0) / 100.0;
    }

    /**
     * Somma xG di tutti i tiri di una partita.
     */
    public BigDecimal sommaXg(java.util.List<Double> xgTiri) {
        double totale = xgTiri.stream().mapToDouble(Double::doubleValue).sum();
        return BigDecimal.valueOf(totale).setScale(2, RoundingMode.HALF_UP);
    }
}

package com.pitchiq.bff.PitchIQ.squadre.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Chi può leggere e modificare i dati riservati di un giocatore.
 * Unico punto in cui è definita la matrice ruolo → dato: senza un ruolo
 * abilitato il dato non esce dal backend e non può essere sovrascritto.
 */
@Component
public class AccessoDatiRiservati {

    public static final Set<String> CAMPI_ECONOMICI = Set.of("stipendioAnnuo", "clausola", "agente");
    public static final Set<String> CAMPI_MEDICI = Set.of("noteMediche");

    private static final Set<String> RUOLI_ECONOMICI = Set.of("ROLE_DIRIGENTE", "ROLE_ADMIN");
    private static final Set<String> RUOLI_MEDICI = Set.of("ROLE_MEDICO", "ROLE_ADMIN");

    public boolean economici() {
        return haUnRuoloTra(RUOLI_ECONOMICI);
    }

    public boolean medici() {
        return haUnRuoloTra(RUOLI_MEDICI);
    }

    /** true se il campo è riservato e l'utente corrente non può toccarlo. */
    public boolean vietato(String campo) {
        return (CAMPI_ECONOMICI.contains(campo) && !economici())
                || (CAMPI_MEDICI.contains(campo) && !medici());
    }

    private boolean haUnRuoloTra(Set<String> ruoli) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(ruoli::contains);
    }
}

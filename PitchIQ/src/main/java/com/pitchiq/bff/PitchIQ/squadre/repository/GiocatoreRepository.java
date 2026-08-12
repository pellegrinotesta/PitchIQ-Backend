package com.pitchiq.bff.PitchIQ.squadre.repository;

import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiocatoreRepository extends JpaRepository<Giocatore, Long> {

    // Filtraggio per ruolo (es. "mostrami solo i portieri")
    List<Giocatore> findAllByRuolo(RuoloGiocatore ruolo);

    // Filtraggio per stato (es. "rosa disponibile, escludi infortunati")
    List<Giocatore> findAllByStato(StatoGiocatore stato);

    // Ricerca per cognome (case-insensitive) usata dalla searchbar
    List<Giocatore> findAllByCognomeContainingIgnoreCase(String cognome);
}

package com.pitchiq.bff.PitchIQ.squadre.repository;

import com.pitchiq.bff.PitchIQ.squadre.model.Giocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.RuoloGiocatore;
import com.pitchiq.bff.PitchIQ.squadre.model.StatoGiocatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
            SELECT g.id AS id, g.nome AS nome, g.cognome AS cognome,
                   pc.posizione_partita AS posizione, pc.numero_maglia_partita AS numeroMaglia,
                   pc.modulo_fonte AS moduloFonte
            FROM giocatori g
            JOIN partita_convocati pc ON pc.giocatore_id = g.id
            WHERE pc.partita_id = :partitaId AND pc.titolare = TRUE
            ORDER BY g.cognome, g.nome, g.id
            """, nativeQuery = true)
    List<TitolareProjection> findTitolariByPartitaId(@Param("partitaId") Long partitaId);
}

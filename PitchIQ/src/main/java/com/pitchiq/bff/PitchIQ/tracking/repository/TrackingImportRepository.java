package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrackingImportRepository extends JpaRepository<TrackingImport, Long> {

    @Query(value = """
                SELECT * FROM tracking_import i
                 WHERE :query = '' OR LOWER(CONCAT_WS(' ', i.stato, i.messaggio, i.nome_file_eventi,
                         CAST(i.match_id AS CHAR), CAST(i.id AS CHAR))) LIKE CONCAT('%', LOWER(:query), '%')
                 ORDER BY i.creato_il DESC
                """, nativeQuery = true)
    List<TrackingImport> cercaTutti(@Param("query") String query);

    /** Lettura sempre dal database, mai dall'istanza già in memoria. */
    @Query("""
            select new com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto(
                   i.id, i.stato, i.messaggio, i.matchId, i.conTracking, i.nomeFileEventi, i.creatoIl, i.aggiornatoIl)
              from TrackingImport i where i.id = :id
            """)
    Optional<ImportDto> leggi(Long id);

    /**
     * Chiude gli import rimasti appesi (es. servizio Python riavviato a metà lavoro).
     * Usa l'orologio del database, lo stesso che valorizza aggiornato_il.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE tracking_import
               SET stato = 'ERRORE', messaggio = :messaggio
             WHERE stato IN ('IN_CODA', 'IN_ELABORAZIONE')
               AND aggiornato_il < NOW(6) - INTERVAL :minuti MINUTE
            """, nativeQuery = true)
    int chiudiScaduti(long minuti, String messaggio);
}

package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrackingImportRepository extends JpaRepository<TrackingImport, Long> {

    List<TrackingImport> findTop10ByOrderByCreatoIlDesc();

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

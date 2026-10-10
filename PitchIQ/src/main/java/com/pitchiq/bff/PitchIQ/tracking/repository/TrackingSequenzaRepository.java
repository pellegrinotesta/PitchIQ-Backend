package com.pitchiq.bff.PitchIQ.tracking.repository;

import com.pitchiq.bff.PitchIQ.tracking.entity.EsitoPressione;
import com.pitchiq.bff.PitchIQ.tracking.entity.TrackingSequenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrackingSequenzaRepository extends JpaRepository<TrackingSequenza, Long> {

    @Query("""
            select s from TrackingSequenza s
             where s.matchId = :matchId
               and (:esito is null or s.esito = :esito)
               and (:teamId is null or s.teamId = :teamId)
               and (:soloAnimate = false or s.haFrame = true)
             order by s.periodo, s.minuto, s.secondo
            """)
    List<TrackingSequenza> cerca(Long matchId, EsitoPressione esito, Long teamId, boolean soloAnimate);

    /** Righe: [esito, possessi, valoreMedio, valoreMigliore, quotaTiro]. matchId null = tutte le partite. */
    @Query("""
            select s.esito, count(s), avg(s.valoreMedio), avg(s.valoreMigliore),
                   avg(case when s.portaAlTiro = true then 1.0 else 0.0 end)
              from TrackingSequenza s
             where (:matchId is null or s.matchId = :matchId)
             group by s.esito
            """)
    List<Object[]> aggregatiPerEsito(Long matchId);

    @Query("""
            select s.matchId, s.esito, count(s) from TrackingSequenza s
             group by s.matchId, s.esito
            """)
    List<Object[]> conteggiPerPartita();
}

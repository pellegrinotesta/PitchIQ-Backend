package com.pitchiq.bff.PitchIQ.statistiche.repository;

import com.pitchiq.bff.PitchIQ.statistiche.model.CategoriaMetrica;
import com.pitchiq.bff.PitchIQ.statistiche.model.StatisticaPeriodica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StatisticaPeriodicaRepository extends JpaRepository<StatisticaPeriodica, Long> {

    List<StatisticaPeriodica> findAllByGiocatoreIdOrderByPeriodoAsc(Long giocatoreId);

    List<StatisticaPeriodica> findAllByGiocatoreIdAndCategoriaOrderByPeriodoAsc(
            Long giocatoreId, CategoriaMetrica categoria);

    Optional<StatisticaPeriodica> findByGiocatoreIdAndPeriodoAndCategoria(
            Long giocatoreId, String periodo, CategoriaMetrica categoria);

    @Query("SELECT DISTINCT s.giocatoreId FROM StatisticaPeriodica s")
    List<Long> findDistinctGiocatoreIds();
}

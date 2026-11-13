-- Fonte: ESPN, tabellini squadra delle singole partite.
-- I tackle non sono duelli complessivi; duelli e xG restano NULL.
-- Le statistiche gia presenti non vengono sovrascritte.
START TRANSACTION;

INSERT INTO statistiche_partita
    (partita_id, possesso_pct, tiri_totali, tiri_in_porta, passaggi,
     passaggi_riusciti, corner, falli, fuorigioco, fonte)
SELECT p.id, s.possesso, s.tiri, s.tiri_porta, s.passaggi,
       s.passaggi_riusciti, s.corner, s.falli, s.fuorigioco,
       CONCAT('ESPN: https://www.espn.com/soccer/team-stats/_/gameId/', s.espn_id)
FROM (
    SELECT '2026-08-14' data_partita, 'Monza' avversario, 'Coppa Italia 2026/27' competizione, 'TRASFERTA' casa, 0 fatti, 3 subiti, 401881686 espn_id, 43.7 possesso, 6 tiri, 2 tiri_porta, 410 passaggi, 358 passaggi_riusciti, 1 corner, 15 falli, 0 fuorigioco
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'CASA', 1, 2, 401899659, 60.4, 11, 3, 552, 488, 4, 11, 0
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'CASA', 3, 0, 401899647, 41.4, 10, 8, 350, 286, 2, 14, 0
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'TRASFERTA', 1, 0, 401899633, 37.8, 6, 2, 317, 243, 2, 12, 0
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'CASA', 1, 1, 401899629, 47.2, 15, 5, 417, 348, 4, 8, 1
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'TRASFERTA', 1, 3, 401899618, 37.3, 12, 3, 294, 231, 1, 13, 0
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'CASA', 4, 1, 401899608, 50.7, 9, 4, 482, 409, 0, 11, 3
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'TRASFERTA', 1, 1, 401899596, 62.0, 20, 4, 537, 432, 5, 6, 1
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'CASA', 0, 1, 401899588, 68.9, 28, 7, 598, 530, 11, 6, 1
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'TRASFERTA', 1, 1, 401899570, 64.1, 17, 2, 556, 480, 4, 15, 2
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'CASA', 2, 0, 401899568, 34.1, 18, 6, 318, 247, 8, 9, 0
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'TRASFERTA', 1, 1, 401899558, 44.7, 8, 5, 368, 301, 3, 10, 5
) s
JOIN partite p ON p.data = s.data_partita AND p.avversario = s.avversario
    AND p.competizione = s.competizione AND p.casa_trasferta = s.casa
    AND p.gol_fatti = s.fatti AND p.gol_subiti = s.subiti
WHERE NOT EXISTS (SELECT 1 FROM statistiche_partita esistente WHERE esistente.partita_id = p.id);

COMMIT;

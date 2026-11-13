-- Snapshot 2026-11-11: https://www.transfermarkt.it/us-avellino-1912/kader/verein/2331/saison_id/2026/plus/1
-- Risultati: https://www.transfermarkt.it/us-avellino-1912/spielplan/verein/2331/saison_id/2026
-- ATTIVO is the schema default, not a verified medical or disciplinary status.
START TRANSACTION;

INSERT INTO giocatori (nome, cognome, data_nascita, ruolo, numero_maglia, piede_preferito, altezza_cm, nazionalita, valore_mercato)
SELECT p.nome, p.cognome, p.nascita, p.ruolo, p.maglia, p.piede, p.altezza, p.nazionalita, p.valore
FROM (
    SELECT 'Tommaso' nome, 'Martinelli' cognome, '2006-01-06' nascita, 'POR' ruolo, 50 maglia, 'DESTRO' piede, 194 altezza, 'Italia' nazionalita, 5000000 valore
    UNION ALL SELECT 'Jacopo', 'Sassi', '2003-07-24', 'POR', 22, 'DESTRO', 194, 'Italia', 250000
    UNION ALL SELECT 'Antony', 'Iannarilli', '1990-12-18', 'POR', 1, 'DESTRO', 186, 'Italia', 200000
    UNION ALL SELECT 'Armando', 'Izzo', '1992-03-02', 'DC', 4, 'DESTRO', 183, 'Italia', 1000000
    UNION ALL SELECT 'Patrick', 'Enrici', '2001-05-02', 'DC', 56, 'DESTRO', 183, 'Italia', 900000
    UNION ALL SELECT 'Lorenco', 'Simic', '1996-07-15', 'DC', 44, 'DESTRO', 195, 'Croazia', 700000
    UNION ALL SELECT 'Michael', 'Venturi', '1999-01-23', 'DC', 23, 'DESTRO', 193, 'Italia', 350000
    UNION ALL SELECT 'Alessandro', 'Di Bitonto', '2005-10-08', 'DC', 5, 'DESTRO', 190, 'Italia', 300000
    UNION ALL SELECT 'Brando', 'Moruzzi', '2004-07-20', 'TSS', 27, 'SINISTRO', 184, 'Italia', 1500000
    UNION ALL SELECT 'Marco', 'Sala', '1999-06-04', 'TSS', 3, 'SINISTRO', 182, 'Italia', 400000
    UNION ALL SELECT 'Filippo', 'Missori', '2004-03-24', 'TSD', 2, 'DESTRO', 182, 'Italia', 2000000
    UNION ALL SELECT 'Tommaso', 'Cancellotti', '1992-05-22', 'TSD', 29, 'DESTRO', 185, 'Italia', 225000
    UNION ALL SELECT 'Giacomo', 'Faticanti', '2004-07-31', 'CDC', 16, 'DESTRO', 186, 'Italia', 4500000
    UNION ALL SELECT 'Luca', 'Palmiero', '1996-05-01', 'CDC', 6, 'DESTRO', 177, 'Italia', 500000
    UNION ALL SELECT 'Dimitrios', 'Sounas', '1994-08-12', 'CDC', 24, 'DESTRO', 179, 'Grecia', 500000
    UNION ALL SELECT 'Luca', 'Di Maggio', '2005-03-31', 'CC', 8, 'DESTRO', 175, 'Italia', 1500000
    UNION ALL SELECT 'Michele', 'Besaggio', '2002-04-28', 'CC', 39, 'DESTRO', 181, 'Italia', 1400000
    UNION ALL SELECT 'Thomas', 'Berenbruch', '2005-05-31', 'CC', 18, 'SINISTRO', 177, 'Italia', 1000000
    UNION ALL SELECT 'Emanuele', 'Adamo', '1998-07-19', 'ALD', 30, 'DESTRO', 177, 'Italia', 400000
    UNION ALL SELECT 'Martin', 'Palumbo', '2002-03-05', 'MOC', 20, 'SINISTRO', 183, 'Norvegia', 2000000
    UNION ALL SELECT 'Raffaele', 'Russo', '1999-02-25', 'ALS', 10, 'DESTRO', 171, 'Italia', 650000
    UNION ALL SELECT 'Sebastiano', 'Di Paolo', '2006-10-09', 'ALD', 77, 'SINISTRO', 180, 'Italia', 400000
    UNION ALL SELECT 'Tommaso', 'Biasci', '1994-11-10', 'SP', 14, 'DESTRO', 182, 'Italia', 700000
    UNION ALL SELECT 'Walid', 'Cheddira', '1998-01-22', 'PC', 9, 'DESTRO', 187, 'Marocco', 2500000
    UNION ALL SELECT 'Daniel', 'Fila', '2002-08-21', 'PC', 11, 'SINISTRO', 190, 'Repubblica Ceca', 1600000
    UNION ALL SELECT 'Luca', 'Pandolfi', '1998-03-14', 'PC', 7, 'DESTRO', 186, 'Italia', 400000
    UNION ALL SELECT 'Andrea', 'Favilli', '1997-05-17', 'PC', 99, 'DESTRO', 192, 'Italia', 200000
) p
WHERE NOT EXISTS (SELECT 1 FROM giocatori g WHERE g.nome = p.nome AND g.cognome = p.cognome AND g.data_nascita = p.nascita);

INSERT INTO partite (data, avversario, competizione, casa_trasferta, gol_fatti, gol_subiti, modulo, note)
SELECT p.data_partita, p.avversario, p.competizione, p.casa, p.fatti, p.subiti, p.modulo, 'Fonte: Transfermarkt, stagione 2026/27'
FROM (
    SELECT '2026-08-14' data_partita, 'Monza' avversario, 'Coppa Italia 2026/27' competizione, 'TRASFERTA' casa, 0 fatti, 3 subiti, '4-3-1-2' modulo
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'CASA', 1, 2, '4-3-1-2'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'CASA', 3, 0, '3-5-2'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'TRASFERTA', 1, 0, '3-5-2'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'CASA', 1, 1, '3-5-2'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'TRASFERTA', 1, 3, '3-5-2'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'CASA', 4, 1, '3-5-2'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'TRASFERTA', 1, 1, '3-5-2'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'CASA', 0, 1, '3-5-2'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'TRASFERTA', 1, 1, '3-5-2'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'CASA', 2, 0, '3-5-2'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'TRASFERTA', 1, 1, '3-5-2'
) p
WHERE NOT EXISTS (SELECT 1 FROM partite esistente WHERE esistente.data = p.data_partita AND esistente.avversario = p.avversario AND esistente.competizione = p.competizione);

-- Undici titolari per partita (Transfermarkt). Panchina esclusa.
INSERT INTO partita_convocati (partita_id, giocatore_id, titolare)
SELECT p.id, g.id, TRUE
FROM (
    SELECT '2026-08-14' data_partita, 'Monza' avversario, 'Coppa Italia 2026/27' competizione, 'Tommaso' nome, 'Martinelli' cognome
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Lorenco', 'Simic'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Tommaso', 'Cancellotti'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Tommaso', 'Biasci'
    UNION ALL SELECT '2026-08-14', 'Monza', 'Coppa Italia 2026/27', 'Daniel', 'Fila'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Lorenco', 'Simic'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Tommaso', 'Cancellotti'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Tommaso', 'Biasci'
    UNION ALL SELECT '2026-08-22', 'Arezzo', 'Serie B 2026/27', 'Daniel', 'Fila'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Lorenco', 'Simic'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Tommaso', 'Cancellotti'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-08-29', 'Vicenza', 'Serie B 2026/27', 'Andrea', 'Favilli'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Lorenco', 'Simic'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-09-06', 'Modena', 'Serie B 2026/27', 'Andrea', 'Favilli'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Tommaso', 'Cancellotti'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-09-13', 'Palermo', 'Serie B 2026/27', 'Luca', 'Pandolfi'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-09-19', 'Ascoli', 'Serie B 2026/27', 'Walid', 'Cheddira'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-10-09', 'Sampdoria', 'Serie B 2026/27', 'Walid', 'Cheddira'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-10-18', 'Cesena', 'Serie B 2026/27', 'Walid', 'Cheddira'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Alessandro', 'Di Bitonto'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Luca', 'Palmiero'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-10-24', 'Padova', 'Serie B 2026/27', 'Walid', 'Cheddira'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Giacomo', 'Faticanti'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Dimitrios', 'Sounas'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Tommaso', 'Biasci'
    UNION ALL SELECT '2026-10-27', 'Virtus Entella', 'Serie B 2026/27', 'Andrea', 'Favilli'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-11-01', 'Pisa', 'Serie B 2026/27', 'Walid', 'Cheddira'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Tommaso', 'Martinelli'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Patrick', 'Enrici'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Michael', 'Venturi'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Armando', 'Izzo'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Brando', 'Moruzzi'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Filippo', 'Missori'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Michele', 'Besaggio'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Luca', 'Di Maggio'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Martin', 'Palumbo'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Raffaele', 'Russo'
    UNION ALL SELECT '2026-11-07', 'Benevento', 'Serie B 2026/27', 'Walid', 'Cheddira'
) x
JOIN partite p ON p.data = x.data_partita AND p.avversario = x.avversario AND p.competizione = x.competizione
JOIN giocatori g ON g.nome = x.nome AND g.cognome = x.cognome
ON DUPLICATE KEY UPDATE titolare = VALUES(titolare);

-- Statistiche di stagione (Transfermarkt, periodo 2026-10, totali fino al 2026-11-07).
-- Cheddira: assist e ammonizioni non verificati, quindi solo gol e minuti.
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore)
SELECT t.giocatore_id, '2026-10', t.categoria, t.valore
FROM (
    SELECT g.id giocatore_id, c.categoria,
           CASE c.categoria
               WHEN 'GOL' THEN s.gol
               WHEN 'ASSIST' THEN s.assist
               WHEN 'AMMONIZIONI' THEN s.ammonizioni
               WHEN 'MINUTI_GIOCATI' THEN s.minuti
           END valore
    FROM (
        SELECT 'Tommaso' nome, 'Martinelli' cognome, 1080 minuti, 0 gol, 0 assist, 1 ammonizioni
        UNION ALL SELECT 'Armando', 'Izzo', 1018, 1, 1, 2
        UNION ALL SELECT 'Patrick', 'Enrici', 890, 0, 0, 1
        UNION ALL SELECT 'Lorenco', 'Simic', 298, 0, 0, 2
        UNION ALL SELECT 'Marco', 'Sala', 80, 0, 0, 0
        UNION ALL SELECT 'Michael', 'Venturi', 782, 0, 0, 3
        UNION ALL SELECT 'Alessandro', 'Di Bitonto', 59, 0, 0, 0
        UNION ALL SELECT 'Tommaso', 'Cancellotti', 334, 0, 0, 0
        UNION ALL SELECT 'Giacomo', 'Faticanti', 129, 0, 0, 1
        UNION ALL SELECT 'Luca', 'Palmiero', 400, 0, 1, 1
        UNION ALL SELECT 'Dimitrios', 'Sounas', 249, 0, 0, 1
        UNION ALL SELECT 'Martin', 'Palumbo', 878, 1, 3, 2
        UNION ALL SELECT 'Luca', 'Di Maggio', 632, 1, 0, 2
        UNION ALL SELECT 'Michele', 'Besaggio', 912, 3, 0, 4
        UNION ALL SELECT 'Thomas', 'Berenbruch', 13, 0, 0, 0
        UNION ALL SELECT 'Emanuele', 'Adamo', 23, 1, 0, 0
        UNION ALL SELECT 'Filippo', 'Missori', 730, 0, 0, 1
        UNION ALL SELECT 'Brando', 'Moruzzi', 992, 1, 2, 2
        UNION ALL SELECT 'Luca', 'Pandolfi', 139, 0, 0, 0
        UNION ALL SELECT 'Andrea', 'Favilli', 366, 0, 3, 1
        UNION ALL SELECT 'Tommaso', 'Biasci', 261, 0, 0, 0
        UNION ALL SELECT 'Daniel', 'Fila', 67, 0, 0, 0
        UNION ALL SELECT 'Raffaele', 'Russo', 983, 5, 2, 0
    ) s
    JOIN giocatori g ON g.nome = s.nome AND g.cognome = s.cognome
    CROSS JOIN (
        SELECT 'GOL' categoria UNION ALL SELECT 'ASSIST' UNION ALL SELECT 'AMMONIZIONI' UNION ALL SELECT 'MINUTI_GIOCATI'
    ) c
) t
ON DUPLICATE KEY UPDATE valore = VALUES(valore);

INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore)
SELECT g.id, '2026-10', 'GOL', 3
FROM giocatori g
WHERE g.nome = 'Walid' AND g.cognome = 'Cheddira'
ON DUPLICATE KEY UPDATE valore = VALUES(valore);

INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore)
SELECT g.id, '2026-10', 'MINUTI_GIOCATI', 557
FROM giocatori g
WHERE g.nome = 'Walid' AND g.cognome = 'Cheddira'
ON DUPLICATE KEY UPDATE valore = VALUES(valore);

COMMIT;
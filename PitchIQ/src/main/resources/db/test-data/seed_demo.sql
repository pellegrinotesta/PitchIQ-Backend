START TRANSACTION;

INSERT INTO giocatori (nome, cognome, data_nascita, ruolo, numero_maglia, piede_preferito, contratto_inizio, contratto_fine, stato, nazionalita)
SELECT demo.nome, demo.cognome, demo.data_nascita, demo.ruolo, demo.numero_maglia, demo.piede, '2025-07-01', '2028-06-30', 'ATTIVO', 'Italiana'
FROM (
    SELECT 'Andrea' nome, 'Rinaldi' cognome, '1998-04-12' data_nascita, 'POR' ruolo, 1 numero_maglia, 'DESTRO' piede
    UNION ALL SELECT 'Luca', 'Monti', '2001-02-20', 'POR', 22, 'DESTRO'
    UNION ALL SELECT 'Marco', 'Bellini', '1999-06-15', 'TSS', 3, 'SINISTRO'
    UNION ALL SELECT 'Davide', 'Ferrara', '1997-03-08', 'DC', 4, 'DESTRO'
    UNION ALL SELECT 'Matteo', 'Galli', '2000-09-11', 'DC', 5, 'SINISTRO'
    UNION ALL SELECT 'Simone', 'Moretti', '1998-11-23', 'TSD', 2, 'DESTRO'
    UNION ALL SELECT 'Pietro', 'Leoni', '2002-05-04', 'DC', 15, 'DESTRO'
    UNION ALL SELECT 'Alessio', 'Marini', '1998-07-18', 'CDC', 6, 'DESTRO'
    UNION ALL SELECT 'Nicola', 'Greco', '2000-01-30', 'CC', 8, 'DESTRO'
    UNION ALL SELECT 'Tommaso', 'Villa', '1999-12-09', 'CC', 10, 'SINISTRO'
    UNION ALL SELECT 'Riccardo', 'Riva', '2002-04-16', 'MOC', 16, 'DESTRO'
    UNION ALL SELECT 'Federico', 'Serra', '2001-08-03', 'ALS', 11, 'DESTRO'
    UNION ALL SELECT 'Gabriele', 'Fontana', '1997-10-25', 'PC', 9, 'DESTRO'
    UNION ALL SELECT 'Leonardo', 'Colombo', '2000-02-14', 'ALD', 7, 'SINISTRO'
    UNION ALL SELECT 'Filippo', 'Costa', '2003-06-21', 'SP', 18, 'DESTRO'
    UNION ALL SELECT 'Edoardo', 'Pellegrini', '2002-03-12', 'TSD', 19, 'DESTRO'
) demo
WHERE NOT EXISTS (SELECT 1 FROM giocatori g WHERE g.nome = demo.nome AND g.cognome = demo.cognome);

INSERT INTO formazioni (nome, modulo, data, note)
SELECT 'Demo - Formazione titolare', '4-3-3', CURDATE(), 'Rosa dimostrativa: costruzione dal basso e pressing alto'
WHERE NOT EXISTS (SELECT 1 FROM formazioni WHERE nome = 'Demo - Formazione titolare');

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, p.x, p.y, p.slot_ruolo, 1
FROM formazioni f
JOIN JSON_TABLE('[
  {"nome":"Andrea","cognome":"Rinaldi","x":50,"y":90,"slot":"GK"},
  {"nome":"Marco","cognome":"Bellini","x":15,"y":72,"slot":"LB"},
  {"nome":"Matteo","cognome":"Galli","x":38,"y":76,"slot":"CB1"},
  {"nome":"Davide","cognome":"Ferrara","x":62,"y":76,"slot":"CB2"},
  {"nome":"Simone","cognome":"Moretti","x":85,"y":72,"slot":"RB"},
  {"nome":"Alessio","cognome":"Marini","x":50,"y":57,"slot":"CDM"},
  {"nome":"Tommaso","cognome":"Villa","x":30,"y":45,"slot":"CM1"},
  {"nome":"Nicola","cognome":"Greco","x":70,"y":45,"slot":"CM2"},
  {"nome":"Federico","cognome":"Serra","x":18,"y":23,"slot":"LW"},
  {"nome":"Gabriele","cognome":"Fontana","x":50,"y":16,"slot":"ST"},
  {"nome":"Leonardo","cognome":"Colombo","x":82,"y":23,"slot":"RW"}
]', '$[*]' COLUMNS (nome VARCHAR(100) PATH '$.nome', cognome VARCHAR(100) PATH '$.cognome', x DOUBLE PATH '$.x', y DOUBLE PATH '$.y', slot_ruolo VARCHAR(20) PATH '$.slot')) p
JOIN giocatori g ON g.nome = p.nome AND g.cognome = p.cognome
WHERE f.nome = 'Demo - Formazione titolare'
  AND NOT EXISTS (SELECT 1 FROM posizioni_campo esistente WHERE esistente.formazione_id = f.id AND esistente.giocatore_id = g.id);

INSERT INTO schemi_formazione (nome, modulo, descrizione)
SELECT 'Demo - Pressing alto', '4-3-3', 'Pressione coordinata e copertura preventiva'
WHERE NOT EXISTS (SELECT 1 FROM schemi_formazione WHERE nome = 'Demo - Pressing alto');

INSERT INTO slot_schema (schema_id, slot_ruolo, ruolo, coord_x, coord_y, note)
SELECT s.id, p.slot_ruolo, p.ruolo, p.x, p.y, p.nota
FROM schemi_formazione s
JOIN JSON_TABLE('[
  {"slot":"GK","ruolo":"PORTIERE","x":50,"y":90,"nota":"Sostegno alla costruzione"},
  {"slot":"LB","ruolo":"DIFENSORE","x":15,"y":72,"nota":"Copertura della fascia"},
  {"slot":"CB1","ruolo":"DIFENSORE","x":38,"y":76,"nota":"Copertura centrale"},
  {"slot":"CB2","ruolo":"DIFENSORE","x":62,"y":76,"nota":"Guida della linea"},
  {"slot":"RB","ruolo":"DIFENSORE","x":85,"y":72,"nota":"Sostegno sulla fascia"},
  {"slot":"CDM","ruolo":"CENTROCAMPISTA","x":50,"y":57,"nota":"Protezione della difesa"},
  {"slot":"CM1","ruolo":"CENTROCAMPISTA","x":30,"y":45,"nota":"Supporto al pressing"},
  {"slot":"CM2","ruolo":"CENTROCAMPISTA","x":70,"y":45,"nota":"Copertura delle linee di passaggio"},
  {"slot":"LW","ruolo":"ATTACCANTE","x":18,"y":23,"nota":"Pressione sul terzino"},
  {"slot":"ST","ruolo":"ATTACCANTE","x":50,"y":16,"nota":"Pressione sul centrale"},
  {"slot":"RW","ruolo":"ATTACCANTE","x":82,"y":23,"nota":"Pressione sul terzino"}
]', '$[*]' COLUMNS (slot_ruolo VARCHAR(20) PATH '$.slot', ruolo VARCHAR(20) PATH '$.ruolo', x DOUBLE PATH '$.x', y DOUBLE PATH '$.y', nota VARCHAR(500) PATH '$.nota')) p
WHERE s.nome = 'Demo - Pressing alto'
  AND NOT EXISTS (SELECT 1 FROM slot_schema esistente WHERE esistente.schema_id = s.id AND esistente.slot_ruolo = p.slot_ruolo);

INSERT INTO frecce_schema (schema_id, start_x, start_y, end_x, end_y, colore, etichetta)
SELECT s.id, p.start_x, p.start_y, p.end_x, p.end_y, p.colore, p.etichetta
FROM schemi_formazione s
JOIN JSON_TABLE('[
  {"sx":18,"sy":23,"ex":12,"ey":12,"colore":"#00ff87","etichetta":"Pressing sinistro"},
  {"sx":50,"sy":16,"ex":50,"ey":7,"colore":"#00ff87","etichetta":"Pressione centrale"},
  {"sx":82,"sy":23,"ex":88,"ey":12,"colore":"#00ff87","etichetta":"Pressing destro"},
  {"sx":50,"sy":57,"ex":50,"ey":43,"colore":"#60a5fa","etichetta":"Copertura"}
]', '$[*]' COLUMNS (start_x DOUBLE PATH '$.sx', start_y DOUBLE PATH '$.sy', end_x DOUBLE PATH '$.ex', end_y DOUBLE PATH '$.ey', colore VARCHAR(10) PATH '$.colore', etichetta VARCHAR(100) PATH '$.etichetta')) p
WHERE s.nome = 'Demo - Pressing alto'
  AND NOT EXISTS (SELECT 1 FROM frecce_schema esistente WHERE esistente.schema_id = s.id AND esistente.etichetta = p.etichetta);

INSERT INTO partite (data, avversario, competizione, casa_trasferta, gol_fatti, gol_subiti, modulo, note)
SELECT p.data, p.avversario, 'Campionato demo', p.casa, p.fatti, p.subiti, '4-3-3', 'Dati dimostrativi'
FROM (
    SELECT DATE_SUB(CURDATE(), INTERVAL 21 DAY) data, 'Atletico Aurora' avversario, 'CASA' casa, 2 fatti, 1 subiti
    UNION ALL SELECT DATE_SUB(CURDATE(), INTERVAL 14 DAY), 'Sporting Riviera', 'TRASFERTA', 1, 1
    UNION ALL SELECT DATE_SUB(CURDATE(), INTERVAL 7 DAY), 'Unione Collina', 'CASA', 3, 0
) p
WHERE NOT EXISTS (SELECT 1 FROM partite esistente WHERE esistente.competizione = 'Campionato demo' AND esistente.avversario = p.avversario);

INSERT INTO statistiche_partita (partita_id, possesso_pct, tiri_totali, tiri_in_porta, passaggi, passaggi_riusciti, duelli_vinti, duelli_totali, corner, falli, fuorigioco, xg)
SELECT p.id, 56, 14, 6, 480, 402, 48, 90, 5, 11, 2, 1.65
FROM partite p
WHERE p.competizione = 'Campionato demo'
  AND NOT EXISTS (SELECT 1 FROM statistiche_partita esistente WHERE esistente.partita_id = p.id);

INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore)
SELECT g.id, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL mesi.mese MONTH), '%Y-%m'), 'VALUTAZIONE_MEDIA', 6.5 + (g.numero_maglia % 5) * 0.1 + mesi.mese * 0.1
FROM giocatori g
JOIN (SELECT 0 mese UNION ALL SELECT 1 UNION ALL SELECT 2) mesi
WHERE (g.nome, g.cognome) IN (('Andrea','Rinaldi'), ('Marco','Bellini'), ('Davide','Ferrara'), ('Matteo','Galli'), ('Simone','Moretti'), ('Alessio','Marini'), ('Nicola','Greco'), ('Tommaso','Villa'), ('Federico','Serra'), ('Gabriele','Fontana'), ('Leonardo','Colombo'))
  AND NOT EXISTS (SELECT 1 FROM statistiche_periodiche esistente WHERE esistente.giocatore_id = g.id AND esistente.periodo = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL mesi.mese MONTH), '%Y-%m') AND esistente.categoria = 'VALUTAZIONE_MEDIA');

COMMIT;
-- =============================================
-- UTENTI
-- =============================================
INSERT INTO utenti (username, password_hash, ruolo) VALUES
    ('allenatore', '$2a$12$7QJ8n3Fz1mKpLgWxR2hOBuDvE9sNcXtYaI5oP0kMjHqSeVbZwA1UC', 'ALLENATORE'),
    ('dirigente',  '$2a$12$7QJ8n3Fz1mKpLgWxR2hOBuDvE9sNcXtYaI5oP0kMjHqSeVbZwA1UC', 'DIRIGENTE')
ON DUPLICATE KEY UPDATE username = username;

-- =============================================
-- GIOCATORI (rosa completa 16 giocatori)
-- =============================================
INSERT INTO giocatori (nome, cognome, data_nascita, ruolo, numero_maglia, piede_preferito, contratto_inizio, contratto_fine, stato) VALUES
-- Portieri
('Marco',      'Donnarumma',  '1999-02-25', 'PORTIERE',        1,  'DESTRO',    '2023-07-01', '2027-06-30', 'ATTIVO'),
('Luca',       'Serafini',    '2001-05-14', 'PORTIERE',        22, 'DESTRO',    '2024-07-01', '2027-06-30', 'ATTIVO'),
-- Difensori
('Alessandro', 'Bastoni',     '1999-04-13', 'DIFENSORE',       5,  'SINISTRO',  '2022-07-01', '2027-06-30', 'ATTIVO'),
('Giovanni',   'Di Lorenzo',  '1993-08-04', 'DIFENSORE',       2,  'DESTRO',    '2021-07-01', '2026-06-30', 'ATTIVO'),
('Federico',   'Gatti',       '1998-06-22', 'DIFENSORE',       6,  'DESTRO',    '2023-01-01', '2027-06-30', 'ATTIVO'),
('Davide',     'Calabria',    '1996-12-06', 'DIFENSORE',       4,  'DESTRO',    '2022-07-01', '2026-06-30', 'INFORTUNATO'),
-- Centrocampisti
('Nicolo',     'Barella',     '1997-02-07', 'CENTROCAMPISTA',  8,  'DESTRO',    '2021-07-01', '2026-06-30', 'ATTIVO'),
('Sandro',     'Tonali',      '2000-05-08', 'CENTROCAMPISTA',  14, 'DESTRO',    '2023-07-01', '2027-06-30', 'ATTIVO'),
('Matteo',     'Pessina',     '1997-04-21', 'CENTROCAMPISTA',  7,  'DESTRO',    '2022-07-01', '2026-06-30', 'ATTIVO'),
('Lorenzo',    'Pellegrini',  '1996-06-19', 'CENTROCAMPISTA',  10, 'DESTRO',    '2021-07-01', '2026-06-30', 'SQUALIFICATO'),
('Manuel',     'Locatelli',   '1998-01-08', 'CENTROCAMPISTA',  16, 'DESTRO',    '2023-07-01', '2027-06-30', 'ATTIVO'),
-- Attaccanti
('Lautaro',    'Martinez',    '1997-08-22', 'ATTACCANTE',      9,  'DESTRO',    '2023-07-01', '2028-06-30', 'ATTIVO'),
('Federico',   'Chiesa',      '1997-10-25', 'ATTACCANTE',      11, 'SINISTRO',  '2022-07-01', '2027-06-30', 'ATTIVO'),
('Giacomo',    'Raspadori',   '2000-02-18', 'ATTACCANTE',      23, 'DESTRO',    '2024-07-01', '2028-06-30', 'ATTIVO'),
('Moise',      'Kean',        '2000-02-28', 'ATTACCANTE',      18, 'DESTRO',    '2024-07-01', '2028-06-30', 'ATTIVO'),
('Matteo',     'Politano',    '1993-08-03', 'ATTACCANTE',      21, 'SINISTRO',  '2021-07-01', '2026-06-30', 'ATTIVO')
ON DUPLICATE KEY UPDATE nome = nome;

-- =============================================
-- FORMAZIONI
-- =============================================
INSERT INTO formazioni (nome, modulo, data, note) VALUES
('Formazione tipo A', '4-3-3',   '2026-08-01', 'Formazione principale stagione 2026/27'),
('Formazione tipo B', '4-4-2',   '2026-08-05', 'Alternativa difensiva'),
('Formazione tipo C', '3-5-2',   '2026-08-10', 'Modulo aggressivo con ali offensive');

-- =============================================
-- POSIZIONI CAMPO - 4-3-3 (Formazione tipo A)
-- =============================================
INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 90.0, 'GK', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Donnarumma'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 30.0, 72.0, 'CB1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Bastoni'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 75.0, 'CB2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Gatti'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 70.0, 72.0, 'RB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Di Lorenzo'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 15.0, 68.0, 'LB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Calabria'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 30.0, 50.0, 'CM1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Barella'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 45.0, 'CM2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Tonali'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 70.0, 50.0, 'CM3', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Pessina'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 20.0, 22.0, 'LW', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Chiesa'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 15.0, 'ST', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Martinez'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 80.0, 22.0, 'RW', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo A' AND g.cognome = 'Raspadori'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

-- =============================================
-- POSIZIONI CAMPO - 4-4-2 (Formazione tipo B)
-- =============================================
INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 90.0, 'GK', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Donnarumma'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 25.0, 72.0, 'CB1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Bastoni'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 45.0, 75.0, 'CB2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Gatti'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 65.0, 72.0, 'RB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Di Lorenzo'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 15.0, 68.0, 'LB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Calabria'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 20.0, 48.0, 'LM', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Barella'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 38.0, 52.0, 'CM1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Tonali'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 62.0, 52.0, 'CM2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Pessina'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 80.0, 48.0, 'RM', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Locatelli'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 38.0, 18.0, 'ST1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Martinez'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 62.0, 18.0, 'ST2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo B' AND g.cognome = 'Kean'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

-- =============================================
-- POSIZIONI CAMPO - 3-5-2 (Formazione tipo C)
-- =============================================
INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 90.0, 'GK', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Donnarumma'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 25.0, 72.0, 'CB1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Bastoni'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 75.0, 'CB2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Gatti'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 75.0, 72.0, 'CB3', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Di Lorenzo'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 12.0, 52.0, 'LWB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Politano'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 30.0, 48.0, 'CM1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Barella'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 50.0, 43.0, 'CM2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Tonali'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 70.0, 48.0, 'CM3', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Locatelli'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 88.0, 52.0, 'RWB', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Pellegrini'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 38.0, 18.0, 'ST1', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Martinez'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

INSERT INTO posizioni_campo (formazione_id, giocatore_id, coord_x, coord_y, slot_ruolo, titolare)
SELECT f.id, g.id, 62.0, 18.0, 'ST2', 1
FROM formazioni f, giocatori g WHERE f.nome = 'Formazione tipo C' AND g.cognome = 'Chiesa'
ON DUPLICATE KEY UPDATE coord_x = VALUES(coord_x), coord_y = VALUES(coord_y);

-- =============================================
-- SCHEMI
-- =============================================
INSERT INTO schemi_formazione (nome, modulo, descrizione) VALUES
('Pressing Alto 4-3-3',   '4-3-3', 'Schema offensivo con pressing alto su tutto il campo'),
('Contropiede 4-4-2',     '4-4-2', 'Schema difensivo con ripartenze veloci'),
('Possesso Palla 3-5-2',  '3-5-2', 'Schema di possesso con costruzione dal basso');

-- Slot schema 4-3-3 pressing (id=1)
INSERT INTO slot_schema (schema_id, slot_ruolo, ruolo, coord_x, coord_y, note) VALUES
(1, 'GK',  'PORTIERE',        50.0, 88.0, 'Portiere sweeper, partecipa alla costruzione'),
(1, 'CB1', 'DIFENSORE',       30.0, 72.0, 'Copre la zona centrale sinistra'),
(1, 'CB2', 'DIFENSORE',       50.0, 75.0, 'Difensore centrale, gestisce la linea'),
(1, 'RB',  'DIFENSORE',       70.0, 72.0, 'Terzino destro con spinta offensiva'),
(1, 'LB',  'DIFENSORE',       15.0, 68.0, 'Terzino sinistro con spinta offensiva'),
(1, 'CM1', 'CENTROCAMPISTA',  30.0, 50.0, 'Mezzala con inserimenti'),
(1, 'CM2', 'CENTROCAMPISTA',  50.0, 44.0, 'Regista, gestisce i tempi'),
(1, 'CM3', 'CENTROCAMPISTA',  70.0, 50.0, 'Mezzala con inserimenti'),
(1, 'LW',  'ATTACCANTE',      18.0, 22.0, 'Ala sinistra, pressing alto sul terzino avversario'),
(1, 'ST',  'ATTACCANTE',      50.0, 14.0, 'Prima punta, riferimento offensivo'),
(1, 'RW',  'ATTACCANTE',      82.0, 22.0, 'Ala destra, pressing alto sul terzino avversario');

-- Frecce schema 4-3-3 pressing
INSERT INTO frecce_schema (schema_id, start_x, start_y, end_x, end_y, colore, etichetta) VALUES
(1, 18.0, 22.0, 10.0, 12.0, '#00ff87', 'Pressing'),
(1, 82.0, 22.0, 90.0, 12.0, '#00ff87', 'Pressing'),
(1, 50.0, 14.0, 50.0, 8.0,  '#00ff87', 'Press GK'),
(1, 50.0, 44.0, 50.0, 30.0, '#60a5fa', 'Lanci'),
(1, 15.0, 68.0, 15.0, 35.0, '#ffb400', 'Sovrap.'),
(1, 70.0, 72.0, 85.0, 35.0, '#ffb400', 'Sovrap.');

-- Slot schema 4-4-2 contropiede (id=2)
INSERT INTO slot_schema (schema_id, slot_ruolo, ruolo, coord_x, coord_y, note) VALUES
(2, 'GK',  'PORTIERE',        50.0, 90.0, 'Portiere classico'),
(2, 'CB1', 'DIFENSORE',       28.0, 75.0, 'Difensore centrale, copertura'),
(2, 'CB2', 'DIFENSORE',       72.0, 75.0, 'Difensore centrale, copertura'),
(2, 'RB',  'DIFENSORE',       85.0, 70.0, 'Terzino destro, fase difensiva'),
(2, 'LB',  'DIFENSORE',       15.0, 70.0, 'Terzino sinistro, fase difensiva'),
(2, 'LM',  'CENTROCAMPISTA',  15.0, 50.0, 'Centrocampista largo, ripiega in difesa'),
(2, 'CM1', 'CENTROCAMPISTA',  35.0, 55.0, 'Mediano di rottura'),
(2, 'CM2', 'CENTROCAMPISTA',  65.0, 55.0, 'Mediano di rottura'),
(2, 'RM',  'CENTROCAMPISTA',  85.0, 50.0, 'Centrocampista largo, ripiega in difesa'),
(2, 'ST1', 'ATTACCANTE',      35.0, 20.0, 'Prima punta, riferimento per il lancio'),
(2, 'ST2', 'ATTACCANTE',      65.0, 20.0, 'Seconda punta, velocità in profondità');

-- Frecce contropiede
INSERT INTO frecce_schema (schema_id, start_x, start_y, end_x, end_y, colore, etichetta) VALUES
(2, 35.0, 55.0, 35.0, 25.0, '#00ff87', 'Lancio'),
(2, 65.0, 55.0, 65.0, 25.0, '#00ff87', 'Lancio'),
(2, 15.0, 50.0, 15.0, 25.0, '#ffb400', 'Corsa'),
(2, 85.0, 50.0, 85.0, 25.0, '#ffb400', 'Corsa');

-- =============================================
-- STATISTICHE (tutti i giocatori, 8 mesi)
-- =============================================

-- VALUTAZIONE MEDIA
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
-- Donnarumma (id=1)
(1, '2025-09', 'VALUTAZIONE_MEDIA', 7.2), (1, '2025-10', 'VALUTAZIONE_MEDIA', 7.0),
(1, '2025-11', 'VALUTAZIONE_MEDIA', 7.5), (1, '2025-12', 'VALUTAZIONE_MEDIA', 7.3),
(1, '2026-01', 'VALUTAZIONE_MEDIA', 7.6), (1, '2026-02', 'VALUTAZIONE_MEDIA', 7.8),
(1, '2026-03', 'VALUTAZIONE_MEDIA', 7.7), (1, '2026-04', 'VALUTAZIONE_MEDIA', 8.0),
-- Serafini (id=2)
(2, '2025-09', 'VALUTAZIONE_MEDIA', 6.0), (2, '2025-10', 'VALUTAZIONE_MEDIA', 6.2),
(2, '2025-11', 'VALUTAZIONE_MEDIA', 6.1), (2, '2025-12', 'VALUTAZIONE_MEDIA', 6.4),
(2, '2026-01', 'VALUTAZIONE_MEDIA', 6.3), (2, '2026-02', 'VALUTAZIONE_MEDIA', 6.5),
(2, '2026-03', 'VALUTAZIONE_MEDIA', 6.6), (2, '2026-04', 'VALUTAZIONE_MEDIA', 6.8),
-- Bastoni (id=3)
(3, '2025-09', 'VALUTAZIONE_MEDIA', 7.0), (3, '2025-10', 'VALUTAZIONE_MEDIA', 7.2),
(3, '2025-11', 'VALUTAZIONE_MEDIA', 7.1), (3, '2025-12', 'VALUTAZIONE_MEDIA', 7.4),
(3, '2026-01', 'VALUTAZIONE_MEDIA', 7.5), (3, '2026-02', 'VALUTAZIONE_MEDIA', 7.3),
(3, '2026-03', 'VALUTAZIONE_MEDIA', 7.6), (3, '2026-04', 'VALUTAZIONE_MEDIA', 7.8),
-- Di Lorenzo (id=4)
(4, '2025-09', 'VALUTAZIONE_MEDIA', 6.8), (4, '2025-10', 'VALUTAZIONE_MEDIA', 7.0),
(4, '2025-11', 'VALUTAZIONE_MEDIA', 6.9), (4, '2025-12', 'VALUTAZIONE_MEDIA', 7.1),
(4, '2026-01', 'VALUTAZIONE_MEDIA', 7.2), (4, '2026-02', 'VALUTAZIONE_MEDIA', 7.0),
(4, '2026-03', 'VALUTAZIONE_MEDIA', 7.3), (4, '2026-04', 'VALUTAZIONE_MEDIA', 7.5),
-- Barella (id=8)
(8, '2025-09', 'VALUTAZIONE_MEDIA', 7.5), (8, '2025-10', 'VALUTAZIONE_MEDIA', 7.8),
(8, '2025-11', 'VALUTAZIONE_MEDIA', 7.6), (8, '2025-12', 'VALUTAZIONE_MEDIA', 8.0),
(8, '2026-01', 'VALUTAZIONE_MEDIA', 7.9), (8, '2026-02', 'VALUTAZIONE_MEDIA', 8.1),
(8, '2026-03', 'VALUTAZIONE_MEDIA', 8.2), (8, '2026-04', 'VALUTAZIONE_MEDIA', 8.4),
-- Martinez (id=12)
(12, '2025-09', 'VALUTAZIONE_MEDIA', 7.8), (12, '2025-10', 'VALUTAZIONE_MEDIA', 8.0),
(12, '2025-11', 'VALUTAZIONE_MEDIA', 7.9), (12, '2025-12', 'VALUTAZIONE_MEDIA', 8.2),
(12, '2026-01', 'VALUTAZIONE_MEDIA', 8.1), (12, '2026-02', 'VALUTAZIONE_MEDIA', 8.3),
(12, '2026-03', 'VALUTAZIONE_MEDIA', 8.5), (12, '2026-04', 'VALUTAZIONE_MEDIA', 8.7),
-- Chiesa (id=13)
(13, '2025-09', 'VALUTAZIONE_MEDIA', 6.5), (13, '2025-10', 'VALUTAZIONE_MEDIA', 6.8),
(13, '2025-11', 'VALUTAZIONE_MEDIA', 7.0), (13, '2025-12', 'VALUTAZIONE_MEDIA', 6.9),
(13, '2026-01', 'VALUTAZIONE_MEDIA', 7.2), (13, '2026-02', 'VALUTAZIONE_MEDIA', 7.1),
(13, '2026-03', 'VALUTAZIONE_MEDIA', 7.4), (13, '2026-04', 'VALUTAZIONE_MEDIA', 7.6);

-- GOL (solo attaccanti e centrocampisti)
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
(12, '2025-09', 'GOL', 2), (12, '2025-10', 'GOL', 3), (12, '2025-11', 'GOL', 2),
(12, '2025-12', 'GOL', 4), (12, '2026-01', 'GOL', 3), (12, '2026-02', 'GOL', 5),
(12, '2026-03', 'GOL', 4), (12, '2026-04', 'GOL', 6),
(13, '2025-09', 'GOL', 1), (13, '2025-10', 'GOL', 2), (13, '2025-11', 'GOL', 1),
(13, '2025-12', 'GOL', 2), (13, '2026-01', 'GOL', 3), (13, '2026-02', 'GOL', 2),
(13, '2026-03', 'GOL', 3), (13, '2026-04', 'GOL', 4),
(8,  '2025-09', 'GOL', 0), (8,  '2025-10', 'GOL', 1), (8,  '2025-11', 'GOL', 1),
(8,  '2025-12', 'GOL', 2), (8,  '2026-01', 'GOL', 1), (8,  '2026-02', 'GOL', 2),
(8,  '2026-03', 'GOL', 2), (8,  '2026-04', 'GOL', 3);

-- ASSIST
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
(8,  '2025-09', 'ASSIST', 1), (8,  '2025-10', 'ASSIST', 2), (8,  '2025-11', 'ASSIST', 2),
(8,  '2025-12', 'ASSIST', 3), (8,  '2026-01', 'ASSIST', 2), (8,  '2026-02', 'ASSIST', 4),
(8,  '2026-03', 'ASSIST', 3), (8,  '2026-04', 'ASSIST', 5),
(12, '2025-09', 'ASSIST', 1), (12, '2025-10', 'ASSIST', 1), (12, '2025-11', 'ASSIST', 2),
(12, '2025-12', 'ASSIST', 1), (12, '2026-01', 'ASSIST', 2), (12, '2026-02', 'ASSIST', 3),
(12, '2026-03', 'ASSIST', 2), (12, '2026-04', 'ASSIST', 3),
(13, '2025-09', 'ASSIST', 2), (13, '2025-10', 'ASSIST', 3), (13, '2025-11', 'ASSIST', 2),
(13, '2025-12', 'ASSIST', 4), (13, '2026-01', 'ASSIST', 3), (13, '2026-02', 'ASSIST', 4),
(13, '2026-03', 'ASSIST', 5), (13, '2026-04', 'ASSIST', 5);

-- PRESENZE %
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
(1,  '2025-09', 'PRESENZE_PCT', 100), (1,  '2025-10', 'PRESENZE_PCT', 100),
(1,  '2025-11', 'PRESENZE_PCT', 100), (1,  '2025-12', 'PRESENZE_PCT', 100),
(1,  '2026-01', 'PRESENZE_PCT', 100), (1,  '2026-02', 'PRESENZE_PCT', 100),
(1,  '2026-03', 'PRESENZE_PCT', 100), (1,  '2026-04', 'PRESENZE_PCT', 100),
(8,  '2025-09', 'PRESENZE_PCT', 90),  (8,  '2025-10', 'PRESENZE_PCT', 85),
(8,  '2025-11', 'PRESENZE_PCT', 95),  (8,  '2025-12', 'PRESENZE_PCT', 90),
(8,  '2026-01', 'PRESENZE_PCT', 100), (8,  '2026-02', 'PRESENZE_PCT', 95),
(8,  '2026-03', 'PRESENZE_PCT', 100), (8,  '2026-04', 'PRESENZE_PCT', 100),
(12, '2025-09', 'PRESENZE_PCT', 85),  (12, '2025-10', 'PRESENZE_PCT', 90),
(12, '2025-11', 'PRESENZE_PCT', 88),  (12, '2025-12', 'PRESENZE_PCT', 92),
(12, '2026-01', 'PRESENZE_PCT', 95),  (12, '2026-02', 'PRESENZE_PCT', 90),
(12, '2026-03', 'PRESENZE_PCT', 95),  (12, '2026-04', 'PRESENZE_PCT', 100);

-- VELOCITA
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
(12, '2025-09', 'VELOCITA', 33.2), (12, '2025-10', 'VELOCITA', 33.5),
(12, '2025-11', 'VELOCITA', 33.8), (12, '2025-12', 'VELOCITA', 34.0),
(12, '2026-01', 'VELOCITA', 33.9), (12, '2026-02', 'VELOCITA', 34.2),
(12, '2026-03', 'VELOCITA', 34.5), (12, '2026-04', 'VELOCITA', 34.8),
(13, '2025-09', 'VELOCITA', 35.1), (13, '2025-10', 'VELOCITA', 35.4),
(13, '2025-11', 'VELOCITA', 35.2), (13, '2025-12', 'VELOCITA', 35.6),
(13, '2026-01', 'VELOCITA', 35.8), (13, '2026-02', 'VELOCITA', 35.5),
(13, '2026-03', 'VELOCITA', 35.9), (13, '2026-04', 'VELOCITA', 36.2);

-- MINUTI GIOCATI
INSERT INTO statistiche_periodiche (giocatore_id, periodo, categoria, valore) VALUES
(1,  '2025-09', 'MINUTI_GIOCATI', 360), (1,  '2025-10', 'MINUTI_GIOCATI', 360),
(1,  '2025-11', 'MINUTI_GIOCATI', 360), (1,  '2025-12', 'MINUTI_GIOCATI', 360),
(1,  '2026-01', 'MINUTI_GIOCATI', 360), (1,  '2026-02', 'MINUTI_GIOCATI', 360),
(1,  '2026-03', 'MINUTI_GIOCATI', 360), (1,  '2026-04', 'MINUTI_GIOCATI', 360),
(8,  '2025-09', 'MINUTI_GIOCATI', 310), (8,  '2025-10', 'MINUTI_GIOCATI', 290),
(8,  '2025-11', 'MINUTI_GIOCATI', 340), (8,  '2025-12', 'MINUTI_GIOCATI', 320),
(8,  '2026-01', 'MINUTI_GIOCATI', 360), (8,  '2026-02', 'MINUTI_GIOCATI', 345),
(8,  '2026-03', 'MINUTI_GIOCATI', 360), (8,  '2026-04', 'MINUTI_GIOCATI', 360),
(12, '2025-09', 'MINUTI_GIOCATI', 290), (12, '2025-10', 'MINUTI_GIOCATI', 320),
(12, '2025-11', 'MINUTI_GIOCATI', 300), (12, '2025-12', 'MINUTI_GIOCATI', 340),
(12, '2026-01', 'MINUTI_GIOCATI', 355), (12, '2026-02', 'MINUTI_GIOCATI', 330),
(12, '2026-03', 'MINUTI_GIOCATI', 350), (12, '2026-04', 'MINUTI_GIOCATI', 360);

-- Aggiorna ruoli con valori specifici
UPDATE giocatori SET ruolo = 'POR' WHERE cognome IN ('Donnarumma', 'Serafini');
UPDATE giocatori SET ruolo = 'DC'  WHERE cognome IN ('Bastoni', 'Gatti');
UPDATE giocatori SET ruolo = 'TSD' WHERE cognome IN ('Di Lorenzo');
UPDATE giocatori SET ruolo = 'TSS' WHERE cognome IN ('Calabria');
UPDATE giocatori SET ruolo = 'CDC' WHERE cognome IN ('Tonali', 'Locatelli');
UPDATE giocatori SET ruolo = 'CC'  WHERE cognome IN ('Barella', 'Pessina');
UPDATE giocatori SET ruolo = 'MOC' WHERE cognome IN ('Pellegrini');
UPDATE giocatori SET ruolo = 'ALD' WHERE cognome IN ('Politano');
UPDATE giocatori SET ruolo = 'ALS' WHERE cognome IN ('Chiesa');
UPDATE giocatori SET ruolo = 'PC'  WHERE cognome IN ('Martinez', 'Kean');
UPDATE giocatori SET ruolo = 'SP'  WHERE cognome IN ('Raspadori');
UPDATE giocatori SET ruolo = 'FW'  WHERE cognome IN ('Politano');
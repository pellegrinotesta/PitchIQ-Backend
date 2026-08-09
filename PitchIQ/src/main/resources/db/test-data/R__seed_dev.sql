INSERT INTO utenti (username, password_hash, ruolo) VALUES
    ('allenatore', '$2a$12$7QJ8n3Fz1mKpLgWxR2hOBuDvE9sNcXtYaI5oP0kMjHqSeVbZwA1UC', 'ALLENATORE'),
    ('dirigente',  '$2a$12$7QJ8n3Fz1mKpLgWxR2hOBuDvE9sNcXtYaI5oP0kMjHqSeVbZwA1UC', 'DIRIGENTE')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO giocatori (nome, cognome, data_nascita, ruolo, numero_maglia, piede_preferito, contratto_inizio, contratto_fine, stato) VALUES
    ('Marco',     'Rossi',     '1995-03-15', 'PORTIERE',        1,  'DESTRO',     '2023-07-01', '2026-06-30', 'ATTIVO'),
    ('Luca',      'Ferrari',   '1998-06-22', 'DIFENSORE',       5,  'DESTRO',     '2022-07-01', '2025-06-30', 'ATTIVO'),
    ('Andrea',    'Bianchi',   '1997-11-08', 'DIFENSORE',       6,  'SINISTRO',   '2023-01-01', '2026-12-31', 'ATTIVO'),
    ('Stefano',   'Romano',    '2000-04-17', 'DIFENSORE',       3,  'DESTRO',     '2024-07-01', '2027-06-30', 'ATTIVO'),
    ('Giorgio',   'Conti',     '1996-09-30', 'DIFENSORE',       4,  'DESTRO',     '2022-07-01', '2025-06-30', 'INFORTUNATO'),
    ('Paolo',     'Ricci',     '1994-01-25', 'CENTROCAMPISTA',  8,  'DESTRO',     '2021-07-01', '2024-06-30', 'ATTIVO'),
    ('Francesco', 'Marino',    '1999-07-12', 'CENTROCAMPISTA',  10, 'DESTRO',     '2023-07-01', '2026-06-30', 'ATTIVO'),
    ('Antonio',   'Greco',     '2001-02-28', 'CENTROCAMPISTA',  7,  'SINISTRO',   '2024-01-01', '2027-06-30', 'ATTIVO'),
    ('Roberto',   'Bruno',     '1993-05-19', 'CENTROCAMPISTA',  14, 'AMBIDESTRO', '2020-07-01', '2025-06-30', 'SQUALIFICATO'),
    ('Davide',    'Lombardi',  '2002-08-03', 'ATTACCANTE',      9,  'DESTRO',     '2024-07-01', '2027-06-30', 'ATTIVO'),
    ('Matteo',    'Esposito',  '1997-12-11', 'ATTACCANTE',      11, 'SINISTRO',   '2023-01-01', '2026-06-30', 'ATTIVO'),
    ('Simone',    'Gallo',     '1995-10-07', 'ATTACCANTE',      22, 'DESTRO',     '2022-07-01', '2025-06-30', 'ATTIVO');
CREATE TABLE giocatori
(
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    cognome          VARCHAR(100) NOT NULL,
    data_nascita     DATE,
    ruolo            ENUM('PORTIERE','DIFENSORE','CENTROCAMPISTA','ATTACCANTE') NOT NULL,
    numero_maglia    TINYINT UNSIGNED CHECK (numero_maglia BETWEEN 1 AND 99),
    piede_preferito  VARCHAR(20),
    contratto_inizio DATE,
    contratto_fine   DATE,
    stato            ENUM('ATTIVO','INFORTUNATO','SQUALIFICATO') NOT NULL DEFAULT 'ATTIVO'
);

CREATE INDEX idx_giocatori_ruolo   ON giocatori (ruolo);
CREATE INDEX idx_giocatori_stato   ON giocatori (stato);
CREATE INDEX idx_giocatori_cognome ON giocatori (cognome);
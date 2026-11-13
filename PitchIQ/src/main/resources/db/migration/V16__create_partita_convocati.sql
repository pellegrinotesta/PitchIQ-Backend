CREATE TABLE partita_convocati (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partita_id BIGINT NOT NULL,
    giocatore_id BIGINT NOT NULL,
    titolare BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_partita_convocato (partita_id, giocatore_id),
    CONSTRAINT fk_convocati_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE CASCADE,
    CONSTRAINT fk_convocati_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE
);

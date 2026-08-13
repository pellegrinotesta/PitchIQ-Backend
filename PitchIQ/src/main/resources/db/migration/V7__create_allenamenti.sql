CREATE TABLE sedute_allenamento
(
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data        DATE        NOT NULL,
    ora_inizio  TIME        NOT NULL,
    ora_fine    TIME        NOT NULL,
    tipo        ENUM('TECNICO','TATTICO','ATLETICO','PARTITELLA') NOT NULL,
    luogo       VARCHAR(100),
    note        VARCHAR(500)
);

CREATE TABLE presenze
(
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    seduta_id        BIGINT      NOT NULL,
    giocatore_id     BIGINT      NOT NULL,
    presente         TINYINT(1)  NOT NULL DEFAULT 1,
    motivo_assenza   VARCHAR(200),
    valutazione      TINYINT     CHECK (valutazione BETWEEN 1 AND 10),
    CONSTRAINT uq_presenza UNIQUE (seduta_id, giocatore_id),
    CONSTRAINT fk_presenza_seduta    FOREIGN KEY (seduta_id)    REFERENCES sedute_allenamento (id) ON DELETE CASCADE,
    CONSTRAINT fk_presenza_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id)           ON DELETE CASCADE
);

CREATE INDEX idx_presenze_seduta    ON presenze (seduta_id);
CREATE INDEX idx_presenze_giocatore ON presenze (giocatore_id);
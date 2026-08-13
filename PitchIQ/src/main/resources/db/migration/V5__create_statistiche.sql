CREATE TABLE statistiche_periodiche
(
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    giocatore_id  BIGINT      NOT NULL,
    periodo       VARCHAR(7)  NOT NULL,
    categoria     VARCHAR(30) NOT NULL,
    valore        DOUBLE      NOT NULL,
    CONSTRAINT uq_statistica UNIQUE (giocatore_id, periodo, categoria),
    CONSTRAINT fk_stat_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE
);

CREATE INDEX idx_stat_giocatore ON statistiche_periodiche (giocatore_id);
CREATE INDEX idx_stat_periodo   ON statistiche_periodiche (periodo);
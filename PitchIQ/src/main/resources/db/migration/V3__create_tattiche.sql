CREATE TABLE formazioni
(
    id     BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    modulo VARCHAR(20)  NOT NULL,
    data   DATE,
    note   VARCHAR(500)
);

CREATE TABLE posizioni_campo
(
    id             BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    formazione_id  BIGINT         NOT NULL,
    giocatore_id   BIGINT         NOT NULL,
    coord_x        DOUBLE         NOT NULL,
    coord_y        DOUBLE         NOT NULL,
    slot_ruolo     VARCHAR(20),
    titolare       TINYINT(1)     NOT NULL DEFAULT 1,
    CONSTRAINT fk_posizione_formazione FOREIGN KEY (formazione_id) REFERENCES formazioni (id) ON DELETE CASCADE,
    CONSTRAINT fk_posizione_giocatore  FOREIGN KEY (giocatore_id)  REFERENCES giocatori (id)  ON DELETE CASCADE,
    CONSTRAINT uq_formazione_giocatore UNIQUE (formazione_id, giocatore_id)
);

CREATE INDEX idx_posizioni_formazione ON posizioni_campo (formazione_id);
CREATE TABLE schemi_formazione
(
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    modulo      VARCHAR(20)  NOT NULL,
    descrizione VARCHAR(500)
);

CREATE TABLE slot_schema
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    schema_id  BIGINT      NOT NULL,
    slot_ruolo VARCHAR(20) NOT NULL,
    ruolo      VARCHAR(20) NOT NULL,
    coord_x    DOUBLE      NOT NULL,
    coord_y    DOUBLE      NOT NULL,
    note       VARCHAR(500),
    CONSTRAINT fk_slot_schema FOREIGN KEY (schema_id) REFERENCES schemi_formazione (id) ON DELETE CASCADE
);

CREATE TABLE frecce_schema
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    schema_id  BIGINT      NOT NULL,
    start_x    DOUBLE      NOT NULL,
    start_y    DOUBLE      NOT NULL,
    end_x      DOUBLE      NOT NULL,
    end_y      DOUBLE      NOT NULL,
    colore     VARCHAR(10) DEFAULT '#00ff87',
    etichetta  VARCHAR(100),
    CONSTRAINT fk_freccia_schema FOREIGN KEY (schema_id) REFERENCES schemi_formazione (id) ON DELETE CASCADE
);

CREATE INDEX idx_slot_schema    ON slot_schema (schema_id);
CREATE INDEX idx_frecce_schema  ON frecce_schema (schema_id);
-- Rinominare con il prossimo numero libero, es. V12__tracking_schema.sql
-- Lo schema è di Flyway. Il servizio Python scrive solo righe (INSERT/UPDATE/DELETE).

-- Ogni caricamento dall'app crea una riga qui. Il BFF la crea (IN_CODA),
-- il servizio Python la porta a IN_ELABORAZIONE e poi COMPLETATO o ERRORE.
CREATE TABLE tracking_import (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stato            VARCHAR(20)   NOT NULL,
    messaggio        VARCHAR(500),
    match_id         BIGINT,
    con_tracking     BOOLEAN       NOT NULL,
    nome_file_eventi VARCHAR(255)  NOT NULL,
    -- orari gestiti dal database, così BFF e servizio Python usano lo stesso orologio
    creato_il        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    aggiornato_il    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_import_stato (stato)
);

CREATE TABLE tracking_partita (
    match_id           BIGINT        NOT NULL PRIMARY KEY,
    import_id          BIGINT        NOT NULL,
    data_partita       DATETIME      NOT NULL,
    squadra_casa       VARCHAR(100)  NOT NULL,
    squadra_trasferta  VARCHAR(100)  NOT NULL,
    gol_casa           INT           NOT NULL,
    gol_trasferta      INT           NOT NULL,
    competizione       VARCHAR(100),
    giocatori_json     JSON          NOT NULL,   -- {"<playerId>": {"nome": "...", "teamId": 123, "numero": 10}}
    elaborata_il       DATETIME(6)   NOT NULL,
    CONSTRAINT fk_partita_import FOREIGN KEY (import_id) REFERENCES tracking_import (id)
);

CREATE TABLE tracking_sequenza (
    id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    match_id          BIGINT        NOT NULL,
    possesso_id       VARCHAR(20)   NOT NULL,
    esito             VARCHAR(20)   NOT NULL,   -- SUPERATA | NEUTRALIZZATA | CONTENUTA
    periodo           INT           NOT NULL,
    minuto            INT           NOT NULL,
    secondo           INT           NOT NULL,
    team_id           BIGINT        NOT NULL,
    squadra           VARCHAR(100)  NOT NULL,
    portatore_id      BIGINT        NOT NULL,
    portatore         VARCHAR(100)  NOT NULL,
    terzo             VARCHAR(30),
    n_pressori        INT           NOT NULL,
    n_opzioni         INT           NOT NULL,
    valore_portatore  DECIMAL(7,4),
    valore_medio      DECIMAL(7,4)  NOT NULL,
    valore_migliore   DECIMAL(7,4)  NOT NULL,
    esito_possesso    VARCHAR(30),
    porta_al_tiro     BOOLEAN       NOT NULL,
    ha_frame          BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_sequenza UNIQUE (match_id, possesso_id),
    CONSTRAINT fk_sequenza_partita FOREIGN KEY (match_id)
        REFERENCES tracking_partita (match_id) ON DELETE CASCADE,
    INDEX idx_sequenza_esito (esito),
    INDEX idx_sequenza_team (team_id)
);

-- Solo per le sequenze salvate con animazione (tutte le SUPERATE + un campione delle altre)
CREATE TABLE tracking_frame (
    sequenza_id    BIGINT  NOT NULL PRIMARY KEY,
    pressori_json  JSON    NOT NULL,   -- [playerId, ...]
    press_inizio   INT     NOT NULL,   -- indice del frame (relativo) in cui inizia la pressione
    press_fine     INT     NOT NULL,
    frames_json    JSON    NOT NULL,   -- [{"i":0,"b":[x,y],"c":carrierId,"p":[[id,att,x,y]],"o":{"id":valore}}]
    CONSTRAINT fk_frame_sequenza FOREIGN KEY (sequenza_id)
        REFERENCES tracking_sequenza (id) ON DELETE CASCADE
);

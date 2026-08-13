CREATE TABLE partite
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data          DATE         NOT NULL,
    avversario    VARCHAR(100) NOT NULL,
    competizione  VARCHAR(100),
    casa_trasferta ENUM('CASA','TRASFERTA','NEUTRO') NOT NULL DEFAULT 'CASA',
    gol_fatti     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    gol_subiti    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    modulo        VARCHAR(20),
    note          VARCHAR(500)
);

CREATE TABLE eventi_partita
(
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    partita_id    BIGINT      NOT NULL,
    minuto        TINYINT UNSIGNED NOT NULL,
    tipo          ENUM('GOL','ASSIST','AMMONIZIONE','ESPULSIONE','SOSTITUZIONE_IN',
                       'SOSTITUZIONE_OUT','TIRO','PARATA','RECUPERO','FALLO') NOT NULL,
    giocatore_id  BIGINT,
    nota          VARCHAR(200),
    CONSTRAINT fk_evento_partita   FOREIGN KEY (partita_id)   REFERENCES partite (id) ON DELETE CASCADE,
    CONSTRAINT fk_evento_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE SET NULL
);

CREATE TABLE statistiche_partita
(
    id              BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    partita_id      BIGINT   NOT NULL UNIQUE,
    possesso_pct    TINYINT UNSIGNED,
    tiri_totali     TINYINT UNSIGNED,
    tiri_in_porta   TINYINT UNSIGNED,
    passaggi        SMALLINT UNSIGNED,
    passaggi_riusciti SMALLINT UNSIGNED,
    duelli_vinti    TINYINT UNSIGNED,
    duelli_totali   TINYINT UNSIGNED,
    corner          TINYINT UNSIGNED,
    falli           TINYINT UNSIGNED,
    fuorigioco      TINYINT UNSIGNED,
    xg              DECIMAL(4,2),
    CONSTRAINT fk_stat_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE CASCADE
);

CREATE TABLE heatmap_zone
(
    id           BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    partita_id   BIGINT         NOT NULL,
    giocatore_id BIGINT         NOT NULL,
    coord_x      DECIMAL(5,2)   NOT NULL,
    coord_y      DECIMAL(5,2)   NOT NULL,
    intensita    TINYINT UNSIGNED NOT NULL DEFAULT 1,
    CONSTRAINT fk_heatmap_partita   FOREIGN KEY (partita_id)   REFERENCES partite (id)   ON DELETE CASCADE,
    CONSTRAINT fk_heatmap_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE
);

CREATE TABLE scouting_avversari
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    modulo       VARCHAR(20),
    punti_forza  VARCHAR(500),
    debolezze    VARCHAR(500),
    giocatori_chiave VARCHAR(300),
    note         VARCHAR(500)
);

CREATE INDEX idx_eventi_partita   ON eventi_partita (partita_id);
CREATE INDEX idx_heatmap_partita  ON heatmap_zone (partita_id, giocatore_id);
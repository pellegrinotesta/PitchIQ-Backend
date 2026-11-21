-- Etichette date dall'utente alle clip dell'analisi video: sono gli esempi del classificatore eventi.
-- Le caratteristiche sono copiate qui al momento dell'etichetta: il modello si può riaddestrare
-- anche se i file dell'analisi vengono cancellati.
CREATE TABLE etichette_clip (
    id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    analisi_id            VARCHAR(36)  NOT NULL,
    clip_id               VARCHAR(16)  NOT NULL,
    etichetta             VARCHAR(32)  NOT NULL,
    caratteristiche_json  JSON         NOT NULL,
    inizio_secondi        DOUBLE       NOT NULL,
    fine_secondi          DOUBLE       NOT NULL,
    creata_il             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    aggiornata_il         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_etichetta_clip UNIQUE (analisi_id, clip_id)
);
